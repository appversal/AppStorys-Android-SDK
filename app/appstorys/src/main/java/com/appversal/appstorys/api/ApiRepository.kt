package com.appversal.appstorys.api

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

internal class ApiRepository(
    context: Context,
    private val apiService: ApiService,
    private val mqttApiService: ApiService,
    private val getScreen: () -> String,
) {

    private var mqttClient: MqttWebSocketClient? = null
    private var mqttConfig: MqttConfig? = null
    private var campaignResponseChannel = Channel<CampaignResponse?>(Channel.UNLIMITED)

    private val sharedPreferences = context.getSharedPreferences("appstorys_sdk_prefs", Context.MODE_PRIVATE)
    private var lastProcessedMessageId: String?
        get() = sharedPreferences.getString("last_message_id", null)
        set(value) {
            sharedPreferences.edit().putString("last_message_id", value).apply()
        }

    init {
        mqttClient = MqttWebSocketClient(context)

        CoroutineScope(Dispatchers.IO).launch {
            mqttClient?.messageFlow?.collect { message ->
                try {
                    val gson = com.google.gson.GsonBuilder()
                        .registerTypeAdapter(
                            CampaignResponse::class.java,
                            CampaignResponseDeserializer()
                        )
                        .create()
                    val campaignResponse = gson.fromJson(message, CampaignResponse::class.java)

                    if (campaignResponse.messageId == lastProcessedMessageId) {
                        Log.d("ApiRepository", "Duplicate MQTT message skipped: ${campaignResponse.messageId}")
                        return@collect
                    }
                    lastProcessedMessageId = campaignResponse.messageId

                    val campaign = campaignResponse.campaigns?.firstOrNull()
                    val campaignId = campaign?.id
                    val campaignScreen = campaign?.screen

                    if (
                        campaign != null &&
                        getScreen().equals(campaignScreen, ignoreCase = true)
                    ) {
                        campaignResponseChannel.send(campaignResponse)
                        Log.d("ApiRepository", "New campaign processed: ${campaignId}")
                    } else {
                        Log.d("ApiRepository", "Campaign skipped: $campaignId")
                    }
                } catch (e: Exception) {
                    Log.e("ApiRepository", "Error parsing MQTT message: ${e.message}")
                    campaignResponseChannel.send(null)
                }
            }
        }
    }

    suspend fun getAccessToken(app_id: String, account_id: String): String? {
        return withContext(Dispatchers.IO) {
            when (val result = safeApiCall {
                apiService.validateAccount(
                    ValidateAccountRequest(app_id = app_id, account_id = account_id)
                ).access_token
            }) {
                is ApiResult.Success -> result.data
                is ApiResult.Error -> {
                    Log.e("ApiRepository", "Error getting access token: ${result.message}")
                    null
                }
            }
        }
    }

    suspend fun sendWidgetPositions(accessToken: String,screenName: String, positionList: List<String>) {
        return withContext(Dispatchers.IO) {
            when (val result = safeApiCall {
                apiService.identifyPositions(
                    token = "Bearer $accessToken",
                    IdentifyPositionsRequest(screen_name = screenName, position_list = positionList)
                )
            }) {
                is ApiResult.Success -> {
                    Log.i("ApiRepository", "Widgets Positions sent successfully.: ${result.data}")
                    null
                }
                is ApiResult.Error -> {
                    Log.e("ApiRepository", "Error getting access token: ${result.message}")
                    null
                }
            }
        }
    }

    suspend fun initializeMqttConnection(
        accessToken: String,
        screenName: String,
        userId: String,
        attributes: Map<String, Any>?
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = TrackUserMqttRequest(
                    screenName = screenName,
                    user_id = userId,
                    attributes = attributes ?: emptyMap()
                )
                when (val result = safeApiCall {
                    mqttApiService.getMqttConnectionDetails(
                        token = "Bearer $accessToken",
                        request = requestBody
                    )
                }) {
                    is ApiResult.Success -> {
                        result.data.mqtt.let { config ->
                            mqttConfig = config
                            mqttClient?.connectWithConfig(config) ?: false
                        }
                    }

                    is ApiResult.Error -> {
                        Log.e("ApiRepository", "Error getting MQTT config: ${result.message}")
                        false
                    }
                }
            } catch (e: Exception) {
                Log.e("ApiRepository", "Error initializing MQTT connection: ${e.message}")
                false
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun triggerScreenData(
        accessToken: String,
        screenName: String,
        userId: String,
        attributes: Map<String, Any>?,
        timeoutMs: Long = 20000
    ): Pair<CampaignResponse?, MqttConnectionResponse?> {
        return withContext(Dispatchers.IO) {
            try {
                var mqttResponse: MqttConnectionResponse? = null

                while (!campaignResponseChannel.isEmpty) {
                    campaignResponseChannel.tryReceive()
                }

                if (!mqttClient!!.isConnected()) {
                    val reconnected =
                        initializeMqttConnection(accessToken, screenName, userId, attributes)
                    if (!reconnected) {
                        Log.e("ApiRepository", "Failed to reconnect MQTT")
                        return@withContext Pair(null, null)
                    }

                }

                    val requestBody = TrackUserMqttRequest(
                        screenName = screenName,
                        user_id = userId,
                        attributes = attributes ?: emptyMap()
                    )

                    when (val result = safeApiCall {
                        mqttApiService.getMqttConnectionDetails(
                            token = "Bearer $accessToken",
                            request = requestBody
                        )
                    }) {
                        is ApiResult.Success -> {
                            Log.i("ApiRepository", "Parsed MQTT response: ${result.data}")
                            mqttResponse = result.data
                        }

                        is ApiResult.Error -> {
                            Log.e(
                                "ApiRepository",
                                "Error sending track-user request: ${result.message}"
                            )
                            return@withContext Pair(null, null)
                        }
                    }

                val campaignResponse = withTimeoutOrNull(timeoutMs) {
                    campaignResponseChannel.receive()
                }
                return@withContext Pair(campaignResponse, mqttResponse)
            } catch (e: Exception) {
                Log.e(
                    "ApiRepository",
                    "Error getting campaign data for screen $screenName: ${e.message}"
                )
                Pair(null, null)
            }
        }
    }

    suspend fun trackActions(accessToken: String, actions: TrackAction) {
        withContext(Dispatchers.IO) {
            when (val result = safeApiCall {
                apiService.trackAction(
                    token = "Bearer $accessToken",
                    request = actions
                )
            }) {
                is ApiResult.Error -> println("Error tracking actions: ${result.message}")
                else -> Unit
            }
        }
    }

    suspend fun captureCSATResponse(accessToken: String, actions: CsatFeedbackPostRequest) {
        withContext(Dispatchers.IO) {
            when (val result = safeApiCall {
                apiService.sendCSATResponse(
                    token = "Bearer $accessToken",
                    request = actions
                )
            }) {
                is ApiResult.Error -> println("Error capturing CSAT response: ${result.message}")
                else -> Unit
            }
        }
    }

    suspend fun captureSurveyResponse(accessToken: String, actions: SurveyFeedbackPostRequest) {
        withContext(Dispatchers.IO) {
            when (val result = safeApiCall {
                apiService.sendSurveyResponse(
                    token = "Bearer $accessToken",
                    request = actions
                )
            }) {
                is ApiResult.Error -> println("Error capturing Survey response: ${result.message}")
                else -> Unit
            }
        }
    }

    suspend fun trackReelActions(accessToken: String, actions: ReelActionRequest) {
        withContext(Dispatchers.IO) {
            when (val result = safeApiCall {
                apiService.trackReelAction(
                    token = "Bearer $accessToken",
                    request = actions
                )
            }) {
                is ApiResult.Error -> println("Error tracking actions: ${result.message}")
                else -> Unit
            }
        }
    }

    suspend fun sendReelLikeStatus(accessToken: String, actions: ReelStatusRequest) {
        withContext(Dispatchers.IO) {
            when (val result = safeApiCall {
                apiService.sendReelLikeStatus(
                    token = "Bearer $accessToken",
                    request = actions
                )
            }) {
                is ApiResult.Error -> println("Error tracking actions: ${result.message}")
                else -> Unit
            }
        }
    }

    suspend fun trackStoriesActions(accessToken: String, actions: TrackActionStories) {
        withContext(Dispatchers.IO) {
            when (val result = safeApiCall {
                apiService.trackStoriesAction(
                    token = "Bearer $accessToken",
                    request = actions
                )
            }) {
                is ApiResult.Error -> println("Error tracking actions: ${result.message}")
                else -> Unit
            }
        }
    }

    suspend fun trackTooltipsActions(accessToken: String, actions: TrackActionTooltips) {
        withContext(Dispatchers.IO) {
            when (val result = safeApiCall {
                apiService.trackTooltipsAction(
                    token = "Bearer $accessToken",
                    request = actions
                )
            }) {
                is ApiResult.Error -> println("Error tracking actions: ${result.message}")
                else -> Unit
            }
        }
    }

    suspend fun tooltipIdentify(
        accessToken: String,
        screenName: String,
        user_id: String,
        childrenJson: String,
        screenshotFile: File
    ) {
        withContext(Dispatchers.IO) {
            try {

                val mediaType = "text/plain".toMediaTypeOrNull()
                val jsonMediaType = "application/json".toMediaTypeOrNull()

                val screenNamePart = screenName.toRequestBody(mediaType)
                val userIdPart = user_id.toRequestBody(mediaType)
                val childrenPart = childrenJson.toRequestBody(jsonMediaType)

                val requestFile = screenshotFile.asRequestBody("image/png".toMediaTypeOrNull())
                val screenshotPart = MultipartBody.Part.createFormData(
                    "screenshot",
                    screenshotFile.name,
                    requestFile
                )

                val result = safeApiCall {
                    apiService.identifyTooltips(
                        token = "Bearer $accessToken",
                        user_id = userIdPart,
                        screenName = screenNamePart,
                        children = childrenPart,
                        screenshot = screenshotPart
                    )
                }

                when (result) {
                    is ApiResult.Success -> println("Tooltip identified: ${result}")
                    is ApiResult.Error -> println("Tooltip Server error: ${result.code} ${result.message}")
                    else -> println("Unknown result")
                }
            } catch (e: Exception) {
                println("Exception in tooltipIdentify: ${e.message}")
            }
        }
    }

    fun disconnect() {
        mqttClient?.disconnect()
        campaignResponseChannel.close()
    }
}