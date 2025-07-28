package com.appversal.appstorys

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appversal.appstorys.api.ApiRepository
import com.appversal.appstorys.api.BannerDetails
import com.appversal.appstorys.api.BottomSheetDetails
import com.appversal.appstorys.api.CSATDetails
import com.appversal.appstorys.api.Campaign
import com.appversal.appstorys.api.CsatFeedbackPostRequest
import com.appversal.appstorys.api.FloaterDetails
import com.appversal.appstorys.api.ModalDetails
import com.appversal.appstorys.api.PipDetails
import com.appversal.appstorys.api.ReelActionRequest
import com.appversal.appstorys.api.ReelStatusRequest
import com.appversal.appstorys.api.ReelsDetails
import com.appversal.appstorys.api.RetrofitClient
import com.appversal.appstorys.api.StoryGroup
import com.appversal.appstorys.api.SurveyDetails
import com.appversal.appstorys.api.SurveyFeedbackPostRequest
import com.appversal.appstorys.api.Tooltip
import com.appversal.appstorys.api.TooltipsDetails
import com.appversal.appstorys.api.TrackAction
import com.appversal.appstorys.api.TrackActionStories
import com.appversal.appstorys.api.TrackActionTooltips
import com.appversal.appstorys.api.WidgetDetails
import com.appversal.appstorys.api.WidgetImage
import com.appversal.appstorys.ui.AutoSlidingCarousel
import com.appversal.appstorys.ui.BottomSheetComponent
import com.appversal.appstorys.ui.CarousalImage
import com.appversal.appstorys.ui.CsatDialog
import com.appversal.appstorys.ui.DoubleWidgets
import com.appversal.appstorys.ui.FullScreenVideoScreen
import com.appversal.appstorys.ui.ImageCard
import com.appversal.appstorys.ui.OverlayContainer
import com.appversal.appstorys.ui.OverlayFloater
import com.appversal.appstorys.ui.PipVideo
import com.appversal.appstorys.ui.PopupModal
import com.appversal.appstorys.ui.ReelsRow
import com.appversal.appstorys.ui.StoryAppMain
import com.appversal.appstorys.ui.SurveyBottomSheet
import com.appversal.appstorys.ui.getLikedReels
import com.appversal.appstorys.ui.saveLikedReels
import com.appversal.appstorys.utils.AppStorysSdkState
import com.appversal.appstorys.utils.ViewTreeAnalyzer
import com.appversal.appstorys.utils.toMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

interface AppStorysAPI {
    fun initialize(
        context: Application,
        appId: String,
        accountId: String,
        userId: String,
        attributes: Map<String, Any>? = null,
        navigateToScreen: (String) -> Unit
    )

    fun getScreenCampaigns(
        screenName: String,
        positionList: List<String>,
    )

    fun trackEvents(
        campaign_id: String? = null,
        event: String,
        metadata: Map<String, Any>? = null
    )

    @Composable
    fun overlayElements(
        bottomPadding: Dp,
        topPadding: Dp,
    )

    @Composable
    fun CSAT(bottomPadding: Dp)

    @Composable
    fun Floater(
        modifier: Modifier? = Modifier,
        bottomPadding: Dp
    )

    @Composable
    fun Pip(
        modifier: Modifier? = Modifier,
        bottomPadding: Dp,
        topPadding: Dp,
    )

    @Composable
    fun PinnedBanner(
        modifier: Modifier? = Modifier,
        placeHolder: Drawable? = null,
        placeholderContent: (@Composable () -> Unit)? = null,
        bottomPadding: Dp,
    )

    @Composable
    fun Widget(
        modifier: Modifier = Modifier,
        placeholder: Drawable? = null,
        placeholderContent: (@Composable () -> Unit)? = null,
        position: String? = null,
    )

    @Composable
    fun Stories()

    @Composable
    fun Reels(modifier: Modifier = Modifier)

    @Composable
    fun BottomSheet()

    @Composable
    fun Survey()

    @Composable
    fun Modals()

    @Composable
    fun TestUserButton(
        modifier: Modifier = Modifier,
        screenName: String? = null
    )

    @Composable
    fun getBannerHeight(): Dp

    @Composable
    fun getUserId(): String

    companion object {
        @JvmStatic
        fun getInstance(): AppStorysAPI = AppStorys
    }
}

object AppStorys : AppStorysAPI {
    private lateinit var context: Application
    private lateinit var appId: String
    private lateinit var accountId: String
    private lateinit var userId: String
    private var attributes: Map<String, Any>? = null
    private lateinit var navigateToScreen: (String) -> Unit

    private val apiService = RetrofitClient.apiService
    private val mqttService = RetrofitClient.mqttApiService
    private lateinit var repository: ApiRepository

    @RequiresApi(Build.VERSION_CODES.N)
    override fun initialize(
        context: Application,
        appId: String,
        accountId: String,
        userId: String,
        attributes: Map<String, Any>?,
        navigateToScreen: (String) -> Unit
    ) {
        this.context = context
        this.appId = appId
        this.accountId = accountId
        this.userId = userId
        this.attributes = attributes
        this.navigateToScreen = navigateToScreen

        this.repository = ApiRepository(context, apiService, mqttService) {
            currentScreen
        }

        initiateData()
    }

    private val _campaigns = MutableStateFlow<List<Campaign>>(emptyList())
    private val campaigns: StateFlow<List<Campaign>> get() = _campaigns

    private val _disabledCampaigns = MutableStateFlow<List<String>>(emptyList())
    private val disabledCampaigns: StateFlow<List<String>> get() = _disabledCampaigns

    private val _impressions = MutableStateFlow<List<String>>(emptyList())
    private val impressions: StateFlow<List<String>> get() = _impressions

    private val _viewsCoordinates = MutableStateFlow<Map<String, LayoutCoordinates>>(emptyMap())
    internal val viewsCoordinates: StateFlow<Map<String, LayoutCoordinates>> =
        _viewsCoordinates.asStateFlow()

    private val _tooltipTargetView = MutableStateFlow<Tooltip?>(null)
    internal val tooltipTargetView: StateFlow<Tooltip?> = _tooltipTargetView.asStateFlow()

    private val _tooltipViewed = MutableStateFlow<List<String>>(emptyList())
    private val tooltipViewed: StateFlow<List<String>> = _tooltipViewed.asStateFlow()

    private val _showcaseVisible = MutableStateFlow(false)
    internal val showcaseVisible: StateFlow<Boolean> = _showcaseVisible.asStateFlow()

    private val _selectedReelIndex = MutableStateFlow<Int>(0)
    private val selectedReelIndex: StateFlow<Int> = _selectedReelIndex.asStateFlow()

    private val _reelFullScreenVisible = MutableStateFlow(false)
    private val reelFullScreenVisible: StateFlow<Boolean> = _reelFullScreenVisible.asStateFlow()

    private var accessToken = ""
    private var currentScreen = ""

    private var isScreenCaptureEnabled by mutableStateOf(false)

    private var showCsat = false
    private var showModal = true

    internal var sdkState = AppStorysSdkState.Uninitialized
        private set
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val campaignsMutex = Mutex()

    val trackedEventNames = mutableStateListOf<String>()

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initiateData() {
        if (sdkState == AppStorysSdkState.Initialized || sdkState == AppStorysSdkState.Initializing) {
            return
        }
        sdkState = AppStorysSdkState.Initializing
        coroutineScope.launch {
            fetchData()
            showCaseInformation()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private suspend fun fetchData() {
        try {
            val accessToken = repository.getAccessToken(appId, accountId)
            if (!accessToken.isNullOrBlank()) {
                this.accessToken = accessToken
                getScreenCampaigns("Home Screen", emptyList())
                sdkState = AppStorysSdkState.Initialized
            }
        } catch (exception: Exception) {
            Log.e("AppStorys", exception.message ?: "Error Fetch Data")
            sdkState = AppStorysSdkState.Error
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun getScreenCampaigns(
        screenName: String,
        positionList: List<String>
    ) {
        coroutineScope.launch {
            repository.sendWidgetPositions(
                accessToken = accessToken,
                screenName = screenName,
                positionList = positionList
            )
        }
        if (campaignsMutex.isLocked) {
            Log.w("AppStorys", "getScreenCampaigns is already running, skipping new request")
            return
        }
        coroutineScope.launch {
            campaignsMutex.lock()
            while (sdkState == AppStorysSdkState.Initializing) {
                delay(100)
            }
            if (sdkState != AppStorysSdkState.Initialized || accessToken.isBlank()) {
                return@launch
            }
            try {
                if (currentScreen != screenName) {
                    _disabledCampaigns.emit(emptyList())
                    _impressions.emit(emptyList())
                    _campaigns.emit(emptyList())
                    currentScreen = screenName

                    delay(100)
                }

                val deviceInfo = getDeviceInfo(context)

                val mergedAttributes = (attributes ?: emptyMap()) + deviceInfo

                val (campaignResponse, mqttResponse) = repository.triggerScreenData(
                    accessToken = accessToken,
                    screenName = currentScreen,
                    userId = userId,
                    attributes = mergedAttributes
                )

                mqttResponse?.let { response ->
                    isScreenCaptureEnabled = response.screen_capture_enabled ?: false
                }

                campaignResponse?.campaigns?.let { _campaigns.emit(it) }
                Log.e("AppStorys", "Campaign: ${_campaigns.value}")
            } catch (exception: Exception) {
                Log.e("AppStorys", exception.message ?: "Error Fetch Data")
            }
        }.invokeOnCompletion {
            campaignsMutex.unlock()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getDeviceInfo(context: Context): Map<String, Any> {
        val packageManager = context.packageManager
        val packageName = context.packageName
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val appInfo = packageManager.getApplicationInfo(packageName, 0)
        val installTime = packageInfo.firstInstallTime
        val updateTime = packageInfo.lastUpdateTime

        val metrics = context.resources.displayMetrics
        val configuration = context.resources.configuration

        return mapOf(
            "manufacturer" to Build.MANUFACTURER,
            "model" to Build.MODEL,
            "os_version" to Build.VERSION.RELEASE,
            "api_level" to Build.VERSION.SDK_INT,
            "language" to configuration.locales[0].language,
            "locale" to configuration.locales[0].toString(),
            "timezone" to java.util.TimeZone.getDefault().id,
            "screen_width_px" to metrics.widthPixels,
            "screen_height_px" to metrics.heightPixels,
            "screen_density" to metrics.densityDpi,
            "orientation" to if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) "portrait" else "landscape",
            "app_version" to packageInfo.versionName,
            "package_name" to packageName,
            "install_time" to installTime,
            "update_time" to updateTime,
            "device_type" to "mobile",
            "platform" to "android"
        )
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun trackEvents(
        campaign_id: String?,
        event: String,
        metadata: Map<String, Any>?
    ) {
        coroutineScope.launch {
            if (accessToken.isNotEmpty()) {
                if (event != "viewed" && event != "clicked"){
                    trackedEventNames.add(event)
                }
                Log.i("Tracked", "$trackedEventNames")
                try {
                    val deviceInfo = getDeviceInfo(context)
                    val mergedMetadata = (metadata ?: emptyMap()) + deviceInfo
                    val requestBody = JSONObject().apply {
                        put("user_id", userId)
                        campaign_id?.let { put("campaign_id", it) }
                        put("event", event)
                        metadata?.let { put("metadata", JSONObject(mergedMetadata)) }
                    }
                    val client = OkHttpClient()
                    val request = Request.Builder()
                        .url("https://tracking.appstorys.com/capture-event")
                        .post(
                            RequestBody.create(
                                "application/json".toMediaTypeOrNull(),
                                requestBody.toString()
                            )
                        )
                        .addHeader("Authorization", "Bearer $accessToken")
                        .build()
                    val response = client.newCall(request).execute()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    @Composable
    override fun overlayElements(
        bottomPadding: Dp,
        topPadding: Dp,
    ) {
        OverlayContainer.Content(
            bottomPadding = bottomPadding,
            topPadding = topPadding,
        )
    }

    suspend fun analyzeViewRoot(
        root: View, screenName: String, activity: Activity
    ) {
        ViewTreeAnalyzer.analyzeViewRoot(
            root = root,
            screenName = screenName,
            user_id = userId,
            accessToken = accessToken,
            activity = activity,
            context = context
        )
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Composable
    override fun CSAT(
        bottomPadding: Dp
    ) {
        if (!showCsat) {
            val campaignsData = campaigns.collectAsStateWithLifecycle()

            val campaign = campaignsData.value.firstOrNull { it.campaignType == "CSAT" }
            val csatDetails = when (val details = campaign?.details) {
                is CSATDetails -> details
                else -> null
            }

            val shouldShowCSAT = campaign?.triggerEvent.isNullOrEmpty() ||
                    trackedEventNames.contains(campaign.triggerEvent)

            if (csatDetails != null && shouldShowCSAT) {
                val style = csatDetails.styling
                var isVisibleState by remember { mutableStateOf(false) }
                val updatedDelay by rememberUpdatedState(
                    style?.displayDelay?.takeIf { it.isNotBlank() }?.toLongOrNull() ?: 0L
                )

                LaunchedEffect(Unit) {
                    campaign?.id?.let {
                        trackCampaignActions(it, "IMP")
                        trackEvents(it, "viewed")
                    }
                    delay(updatedDelay?.times(1000) ?: 0)
                    isVisibleState = true
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = bottomPadding),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    AnimatedVisibility(
                        modifier = Modifier,
                        visible = isVisibleState,
                        enter = slideInVertically() { it },
                        exit = slideOutVertically { it }
                    ) {
                        CsatDialog(
                            onDismiss = {
                                isVisibleState = false
                                coroutineScope.launch {
                                    delay(500L)
                                    showCsat = true
                                }
                            },
                            onSubmitFeedback = { feedback ->
                                coroutineScope.launch {
                                    repository.captureCSATResponse(
                                        accessToken,
                                        CsatFeedbackPostRequest(
                                            user_id = userId,
                                            csat = csatDetails.id,
                                            rating = feedback.rating,
                                            additional_comments = feedback.additionalComments,
                                            feedback_option = feedback.feedbackOption
                                        )
                                    )
                                    trackEvents(csatDetails.campaign, "clicked")
                                }
                            },
                            csatDetails = csatDetails
                        )
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Composable
    override fun Floater(
        modifier: Modifier?,
        bottomPadding: Dp
    ) {
        val campaignsData = campaigns.collectAsStateWithLifecycle()

        val campaign =
            campaignsData.value.firstOrNull { it.campaignType == "FLT" && it.details is FloaterDetails }

        val floaterDetails = when (val details = campaign?.details) {
            is FloaterDetails -> details
            else -> null
        }

        val shouldShowFloater = campaign?.triggerEvent.isNullOrEmpty() ||
                trackedEventNames.contains(campaign.triggerEvent)

        if (floaterDetails != null && !floaterDetails.image.isNullOrEmpty() && shouldShowFloater) {
            LaunchedEffect(Unit) {
                campaign?.id?.let {
                    trackCampaignActions(it, "IMP")
                    trackEvents(it, "viewed")
                }
            }

            Box(modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomPadding)) {
                val alignmentModifier = when (floaterDetails.position) {
                    "right" -> Modifier.align(Alignment.BottomEnd)
                    "left" -> Modifier.align(Alignment.BottomStart)
                    else -> Modifier.align(Alignment.BottomStart)
                }

                OverlayFloater(
                    modifier = (modifier ?: Modifier).then(alignmentModifier),
                    onClick = {
                        if (campaign?.id != null && floaterDetails.link != null) {
                            clickEvent(link = floaterDetails.link, campaignId = campaign.id)
                            trackEvents(campaign.id, "clicked")
                        }
                    },
                    image = floaterDetails.image,
                    lottieUrl = floaterDetails.lottie_data,
                    height = floaterDetails.height?.dp ?: 60.dp,
                    width = floaterDetails.width?.dp ?: 60.dp,
                    borderRadiusValues = RoundedCornerShape(
                        topStart = (floaterDetails.styling?.topLeftRadius?.toFloatOrNull()
                            ?: 0f).dp,
                        topEnd = (floaterDetails.styling?.topRightRadius?.toFloatOrNull() ?: 0f).dp,
                        bottomStart = (floaterDetails.styling?.bottomLeftRadius?.toFloatOrNull()
                            ?: 0f).dp,
                        bottomEnd = (floaterDetails.styling?.bottomRightRadius?.toFloatOrNull()
                            ?: 0f).dp
                    )
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Composable
    override fun Pip(
        modifier: Modifier?,
        bottomPadding: Dp,
        topPadding: Dp,
    ) {
        val campaignsData = campaigns.collectAsStateWithLifecycle()

        val campaign =
            campaignsData.value.firstOrNull { it.campaignType == "PIP" && it.details is PipDetails }

        val pipDetails = when (val details = campaign?.details) {
            is PipDetails -> details
            else -> null
        }

        val shouldShowPip = campaign?.triggerEvent.isNullOrEmpty() ||
                trackedEventNames.contains(campaign.triggerEvent)

        if (pipDetails != null && !pipDetails.small_video.isNullOrEmpty() && shouldShowPip) {
            key (
                campaign?.id, campaign?.triggerEvent, trackedEventNames.size
            ) {

                var showPip by remember { mutableStateOf(true) }
                LaunchedEffect(Unit) {
                    campaign?.id?.let {
                        trackCampaignActions(it, "IMP")
                        trackEvents(it, "viewed")
                    }
                }

                Box(modifier = modifier?.fillMaxWidth() ?: Modifier.fillMaxWidth()) {
                    if (showPip) {
                        PipVideo(
                            videoUri = pipDetails.small_video,
                            fullScreenVideoUri = if (!pipDetails.large_video.isNullOrEmpty()) {
                                pipDetails.large_video
                            } else {
                                null
                            },
                            onClose = {
                                showPip = false
                                trackedEventNames.remove(campaign?.triggerEvent)
                            },
                            height = pipDetails.height?.dp ?: 180.dp,
                            width = pipDetails.width?.dp ?: 120.dp,
                            button_text = pipDetails.button_text.toString(),
                            link = pipDetails.link.toString(),
                            position = pipDetails.position.toString(),
                            bottomPadding = bottomPadding,
                            topPadding = topPadding,
                            isMovable = pipDetails.styling?.isMovable!!,
                            pipStyling = pipDetails.styling,
                            onButtonClick = {
                                campaign?.id?.let { campaignId ->
                                    trackCampaignActions(campaignId, "CLK")
                                    trackEvents(campaignId, "clicked")
                                }
                                if (!isValidUrl(pipDetails.link)) {
                                    navigateToScreen(pipDetails.link.toString())
                                } else {
                                    openUrl(pipDetails.link.toString())
                                }
                            },
                            onExpandClick = {
                                campaign?.id?.let { campaignId ->
                                    trackCampaignActions(campaignId, "IMP")
                                    trackEvents(campaignId, "viewed")
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun showCaseInformation() {
        coroutineScope.launch {
            combine(
                campaigns,
                viewsCoordinates
            ) { campaignList, coordinates -> campaignList to coordinates }.collectLatest { (campaignList, coordinates) ->
                val campaign =
                    campaignList.firstOrNull { it.campaignType == "TTP" && it.details is TooltipsDetails }
                val tooltipsDetails = campaign?.details as? TooltipsDetails

                val shouldShowTooltip = campaign?.triggerEvent.isNullOrEmpty() ||
                        trackedEventNames.contains(campaign.triggerEvent)
                if (tooltipsDetails != null) {
                    for (tooltip in tooltipsDetails.tooltips?.sortedBy { it.order }
                        ?: emptyList()) {
                        if (tooltip.target != null && !_tooltipViewed.value.contains(tooltip.target)) {
                            while (_tooltipTargetView.value != null) {
                                delay(500L)
                            }
                            _tooltipTargetView.emit(tooltip)
                            _showcaseVisible.emit(true)
                            _tooltipViewed.update {
                                it + tooltip.target
                            }
                        }
                    }
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.N)
    @Composable
    override fun Stories() {
        val campaignsData = campaigns.collectAsStateWithLifecycle()
        val campaign = campaignsData.value.firstOrNull { it.campaignType == "STR" }
        val storiesDetails = (campaign?.details as? List<*>)?.filterIsInstance<StoryGroup>()

        val shouldShowStories = campaign?.triggerEvent.isNullOrEmpty() ||
                trackedEventNames.contains(campaign.triggerEvent)

        if (!storiesDetails.isNullOrEmpty() && shouldShowStories) {
            StoryAppMain(
                apiStoryGroups = storiesDetails,
                sendEvent = {
                    coroutineScope.launch {
                        repository.trackStoriesActions(
                            accessToken, TrackActionStories(
                                campaign_id = campaign.id,
                                user_id = userId,
                                story_slide = it.first.id,
                                event_type = it.second
                            )
                        )
                        trackEvents(campaign.id, "viewed", mapOf("story_slide" to it.first.id!!))
                    }
                },
                sendClickEvent = {
                    trackEvents(campaign.id, it.second, mapOf("story_slide" to it.first.id!!))
                }
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Composable
    override fun Reels(modifier: Modifier) {
        val campaignsData = campaigns.collectAsStateWithLifecycle()
        val campaign =
            campaignsData.value.firstOrNull { it.campaignType == "REL" && it.details is ReelsDetails }
        val reelsDetails = campaign?.details as? ReelsDetails
        val selectedReelIndex by selectedReelIndex.collectAsStateWithLifecycle()
        val visibility by reelFullScreenVisible.collectAsStateWithLifecycle()

        val shouldShowReels = campaign?.triggerEvent.isNullOrEmpty() ||
                trackedEventNames.contains(campaign.triggerEvent)

        if (reelsDetails?.reels != null && reelsDetails.reels.isNotEmpty() && shouldShowReels) {
            Box(modifier = Modifier.fillMaxSize()) {

                ReelsRow(
                    modifier = modifier,
                    reels = reelsDetails.reels,
                    onReelClick = { index ->
                        coroutineScope.launch {
                            _selectedReelIndex.emit(index)
                            _reelFullScreenVisible.emit(true)
                        }
                    },
                    height = reelsDetails.styling?.thumbnailHeight?.toIntOrNull()?.dp ?: 180.dp,
                    width = reelsDetails.styling?.thumbnailWidth?.toIntOrNull()?.dp ?: 120.dp,
                    cornerRadius = reelsDetails.styling?.cornerRadius?.toIntOrNull()?.dp ?: 12.dp
                )

                if (visibility) {
                    ReelFullScreen(
                        campaignId = campaign.id,
                        reelsDetails = reelsDetails,
                        selectedReelIndex = selectedReelIndex
                    ) {
                        coroutineScope.launch {
                            _selectedReelIndex.emit(0)
                            _reelFullScreenVisible.emit(false)
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Composable
    private fun ReelFullScreen(
        campaignId: String?,
        reelsDetails: ReelsDetails,
        selectedReelIndex: Int,
        onDismiss: () -> Unit
    ) {
        if (!reelsDetails.reels.isNullOrEmpty()) {

            var likedReels by remember {
                mutableStateOf(
                    getLikedReels(
                        context.getSharedPreferences(
                            "AppStory",
                            Context.MODE_PRIVATE
                        )
                    )
                )
            }

            Dialog(
                onDismissRequest = onDismiss,
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = false,
                    usePlatformDefaultWidth = false
                )
            ) {

                BackHandler {
                    coroutineScope.launch {
                        _selectedReelIndex.emit(0)
                        _reelFullScreenVisible.emit(false)
                    }
                }

                FullScreenVideoScreen(
                    reelsDetails = reelsDetails,
                    reels = reelsDetails.reels,
                    likedReels = likedReels,
                    startIndex = selectedReelIndex,
                    sendLikesStatus = {
                        coroutineScope.launch {
                            if (it.second == "like") {
                                val list = ArrayList(likedReels)
                                list.add(it.first.id)
                                likedReels = list.distinct()
                                saveLikedReels(
                                    idList = list.distinct(),
                                    sharedPreferences = context.getSharedPreferences(
                                        "AppStory",
                                        Context.MODE_PRIVATE
                                    )
                                )
                            } else {
                                val list = ArrayList(likedReels)
                                list.remove(it.first.id)
                                likedReels = list.distinct()
                                saveLikedReels(
                                    idList = list.distinct(),
                                    sharedPreferences = context.getSharedPreferences(
                                        "AppStory",
                                        Context.MODE_PRIVATE
                                    )
                                )
                            }

                            repository.sendReelLikeStatus(
                                accessToken = accessToken,
                                actions = ReelStatusRequest(
                                    user_id = userId,
                                    action = it.second,
                                    reel = it.first.id
                                )
                            )
                        }
                    },
                    sendEvents = {
                        if (it.second == "IMP") {
                            if (!_impressions.value.contains(it.first.id)) {
                                coroutineScope.launch {
                                    val impressions = ArrayList(impressions.value)
                                    impressions.add(it.first.id)
                                    _impressions.emit(impressions)
                                    repository.trackReelActions(
                                        accessToken = accessToken,
                                        actions = ReelActionRequest(
                                            user_id = userId,
                                            reel_id = it.first.id,
                                            event_type = it.second,
                                            campaign_id = campaignId
                                        )
                                    )
                                    trackEvents(
                                        campaignId,
                                        "viewed",
                                        mapOf("reel_id" to it.first.id!!)
                                    )
                                }
                            }
                        } else {
                            coroutineScope.launch {
                                repository.trackReelActions(
                                    accessToken = accessToken,
                                    actions = ReelActionRequest(
                                        user_id = userId,
                                        reel_id = it.first.id,
                                        event_type = it.second,
                                        campaign_id = campaignId
                                    )
                                )
                                trackEvents(
                                    campaignId,
                                    "clicked",
                                    mapOf("reel_id" to it.first.id!!)
                                )
                            }
                        }

                    },
                    onBack = {
                        coroutineScope.launch {
                            _selectedReelIndex.emit(0)
                            _reelFullScreenVisible.emit(false)
                        }
                    }
                )
            }
        }
    }

    @Composable
    override fun getBannerHeight(): Dp {
        val campaignsData = campaigns.collectAsStateWithLifecycle()
        val defaultHeight = 100.dp

        val campaign =
            campaignsData.value.firstOrNull { it.campaignType == "BAN" && it.details is BannerDetails }
        val bannerDetails = campaign?.details as? BannerDetails

        return bannerDetails?.height?.dp ?: defaultHeight
    }

    @Composable
    override fun getUserId(): String {
        return userId
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Composable
    override fun PinnedBanner(
        modifier: Modifier?,
        placeholder: Drawable?,
        placeholderContent: (@Composable () -> Unit)?,
        bottomPadding: Dp,
    ) {
        val campaignsData = campaigns.collectAsStateWithLifecycle()
        val disabledCampaigns = disabledCampaigns.collectAsStateWithLifecycle()

        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp

        val campaign = campaignsData.value.firstOrNull {
            it.campaignType == "BAN" && it.details is BannerDetails
        }

        val shouldShowBanner = campaign?.triggerEvent.isNullOrEmpty() ||
                trackedEventNames.contains(campaign.triggerEvent)
        val bannerDetails = campaign?.details as? BannerDetails
        if (bannerDetails != null && !disabledCampaigns.value.contains(campaign.id) && shouldShowBanner) {
            val style = bannerDetails.styling
            val bannerUrl = bannerDetails.image

            val calculatedHeight =
                if (bannerDetails.width != null && bannerDetails.height != null) {
                    val aspectRatio = bannerDetails.height.toFloat() / bannerDetails.width.toFloat()

                    val marginLeft = style?.marginLeft?.dp ?: 0.dp
                    val marginRight = style?.marginRight?.dp ?: 0.dp

                    val actualWidth = screenWidth - marginLeft - marginRight

                    (actualWidth.value * aspectRatio).dp
                } else {
                    bannerDetails.height?.dp
                }

            LaunchedEffect(Unit) {
                campaign.id?.let {
                    trackCampaignActions(it, "IMP")
                    trackEvents(it, "viewed")
                }
            }

            Box(modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomPadding)) {
                com.appversal.appstorys.ui.PinnedBanner(
                    modifier = (modifier ?: Modifier)
                        .align(Alignment.BottomCenter)
                        .clickable {
                            campaign.id?.let {
                                clickEvent(link = bannerDetails.link, campaignId = it)
                                trackEvents(it, "clicked")
                            }
                        },
                    imageUrl = bannerUrl ?: "",
                    lottieUrl = bannerDetails.lottie_data,
                    width = bannerDetails.width?.dp ?: screenWidth,
                    exitIcon = style?.enableCloseButton ?: false,
                    exitUnit = {
                        val ids: ArrayList<String> = ArrayList(_disabledCampaigns.value)
                        campaign.id?.let {
                            ids.add(it)
                            coroutineScope.launch {
                                _disabledCampaigns.emit(ids.toList())
                            }
                        }
                    },
                    shape = RoundedCornerShape(
                        topStart = style?.topLeftRadius?.dp ?: 0.dp,
                        topEnd = style?.topRightRadius?.dp ?: 0.dp,
                        bottomEnd = style?.bottomRightRadius?.dp ?: 0.dp,
                        bottomStart = style?.bottomLeftRadius?.dp ?: 0.dp
                    ),
                    bottomMargin = style?.marginBottom?.dp ?: 0.dp,
                    leftMargin = style?.marginLeft?.dp ?: 0.dp,
                    rightMargin = style?.marginRight?.dp ?: 0.dp,
                    contentScale = ContentScale.Fit,
                    height = calculatedHeight,
                    placeHolder = placeholder,
                    placeholderContent = placeholderContent
                )
            }
        }
    }

    @Composable
    override fun Widget(
        modifier: Modifier,
        placeholder: Drawable?,
        placeholderContent: (@Composable () -> Unit)?,
        position: String?
    ) {
        val campaignsData = campaigns.collectAsStateWithLifecycle()
        val campaign =
            campaignsData.value.filter { it.campaignType == "WID" && it.details is WidgetDetails }
                .firstOrNull {
                    if (position == null) {
                        it.position == null
                    } else {
                        it.position == position
                    }
                }
        val widgetDetails = campaign?.details as? WidgetDetails

        val shouldShowWidget = campaign?.triggerEvent.isNullOrEmpty() ||
                trackedEventNames.contains(campaign.triggerEvent)

        if (widgetDetails != null && shouldShowWidget) {

            if (widgetDetails.type == "full") {

                FullWidget(
                    modifier = modifier,
                    staticWidth = LocalConfiguration.current.screenWidthDp.dp,
                    placeHolder = placeholder,
                    contentScale = ContentScale.FillWidth,
                    position = position,
                    placeholderContent = placeholderContent
                )

            } else if (widgetDetails.type == "half") {
                DoubleWidget(
                    modifier = modifier,
                    staticWidth = LocalConfiguration.current.screenWidthDp.dp,
                    position = position,
                    placeHolder = placeholder,
                    placeholderContent = placeholderContent
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Composable
    private fun FullWidget(
        modifier: Modifier = Modifier,
        contentScale: ContentScale = ContentScale.FillWidth,
        staticWidth: Dp? = null,
        placeHolder: Drawable?,
        placeholderContent: (@Composable () -> Unit)? = null,
        position: String?
    ) {
        val campaignsData = campaigns.collectAsStateWithLifecycle()
        val disabledCampaigns = disabledCampaigns.collectAsStateWithLifecycle()
        val campaign = campaignsData.value
            .filter { it.campaignType == "WID" && it.details is WidgetDetails && it.position == position }
            .firstOrNull { (it.details as WidgetDetails).type == "full" }

        val widgetDetails = (campaign?.details as? WidgetDetails)

        var isVisible by remember { mutableStateOf(false) }
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp


        if (widgetDetails?.widgetImages != null && widgetDetails.widgetImages.isNotEmpty() && campaign.id != null && !disabledCampaigns.value.contains(
                campaign.id
            ) && widgetDetails.type == "full"
        ) {
            val pagerState = rememberPagerState(pageCount = {
                widgetDetails.widgetImages.count()
            })
            val widthInDp: Dp? = widgetDetails.width?.dp

            val calculatedHeight =
                if (widgetDetails.width != null && widgetDetails.height != null) {
                    val aspectRatio = widgetDetails.height.toFloat() / widgetDetails.width.toFloat()

                    val marginLeft = widgetDetails.styling?.leftMargin?.toFloatOrNull()?.dp ?: 0.dp
                    val marginRight =
                        widgetDetails.styling?.rightMargin?.toFloatOrNull()?.dp ?: 0.dp

                    val actualWidth = (staticWidth ?: screenWidth) - marginLeft - marginRight
                    (actualWidth.value.minus(32) * aspectRatio).dp
                } else {
                    widgetDetails.height?.dp
                }

            LaunchedEffect(pagerState.currentPage, isVisible) {
                if (isVisible) {
                    campaign?.id?.let {
                        val currentWidgetId = widgetDetails.widgetImages[pagerState.currentPage].id
                        trackCampaignActions(
                            it,
                            "IMP",
                            widgetDetails.widgetImages[pagerState.currentPage].id
                        )

                        if (currentWidgetId != null && !impressions.value.contains(currentWidgetId)) {
                            val impressions = ArrayList(impressions.value)
                            impressions.add(currentWidgetId)
                            _impressions.emit(impressions)
                            trackEvents(
                                it,
                                "viewed",
                                mapOf("widget_image" to currentWidgetId)
                            )

                        } else if (!impressions.value.contains(it)) {
                            val impressions = ArrayList(impressions.value)
                            impressions.add(it)
                            _impressions.emit(impressions)
                            trackEvents(
                                it,
                                "viewed",
                                mapOf("widget_image" to currentWidgetId!!)
                            )
                        }
                    }
                }
            }

            AutoSlidingCarousel(
                modifier = modifier
                    .padding(
                        top = (widgetDetails.styling?.topMargin?.toFloatOrNull() ?: 0f).dp,
                        bottom = (widgetDetails.styling?.bottomMargin?.toFloatOrNull() ?: 0f).dp,
                        start = (widgetDetails.styling?.leftMargin?.toFloatOrNull() ?: 0f).dp,
                        end = (widgetDetails.styling?.rightMargin?.toFloatOrNull() ?: 0f).dp,
                    )
                    .onGloballyPositioned { layoutCoordinates ->
                        val visibilityRect = layoutCoordinates.boundsInWindow()
                        val parentHeight =
                            layoutCoordinates.parentLayoutCoordinates?.size?.height ?: 0
                        val widgetHeight = layoutCoordinates.size.height
                        val isAtLeastHalfVisible = visibilityRect.top < parentHeight &&
                                visibilityRect.bottom > 0 &&
                                (visibilityRect.height >= widgetHeight * 0.5f)

                        isVisible = isAtLeastHalfVisible
                    },
                widgetDetails = widgetDetails,
                pagerState = pagerState,
                itemsCount = widgetDetails.widgetImages.count(),
                width = staticWidth,
                itemContent = { index ->
                    widgetDetails.widgetImages[index].image?.let {
                        CarousalImage(
                            modifier = modifier.clickable {
                                clickEvent(
                                    link = widgetDetails.widgetImages[index].link,
                                    campaignId = campaign.id,
                                    widgetImageId = widgetDetails.widgetImages[index].id
                                )

                                trackEvents(
                                    campaign.id,
                                    "clicked",
                                    mapOf("widget_image" to widgetDetails.widgetImages[index].id!!)
                                )
                            },
                            contentScale = contentScale,
                            imageUrl = widgetDetails.widgetImages[index].image ?: "",
                            lottieUrl = widgetDetails.widgetImages[index].lottie_data ?: "",
                            placeHolder = placeHolder,
                            height = calculatedHeight,
                            width = widthInDp ?: staticWidth,
                            placeholderContent = placeholderContent
                        )
                    }
                }
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Composable
    private fun DoubleWidget(
        modifier: Modifier = Modifier,
        staticWidth: Dp? = null,
        position: String?,
        placeHolder: Drawable?,
        placeholderContent: (@Composable () -> Unit)? = null,
    ) {
        val campaignsData = campaigns.collectAsStateWithLifecycle()
        val disabledCampaigns = disabledCampaigns.collectAsStateWithLifecycle()

        val campaign = campaignsData.value
            .filter { it.campaignType == "WID" && it.details is WidgetDetails && it.position == position }
            .firstOrNull { (it.details as WidgetDetails).type == "half" }

        val widgetDetails = (campaign?.details as? WidgetDetails)

        var isVisible by remember { mutableStateOf(false) }
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp

        if (widgetDetails != null && campaign.id != null &&
            !disabledCampaigns.value.contains(campaign.id) && widgetDetails.widgetImages != null && widgetDetails.type == "half"
        ) {
            val widthInDp: Dp? = widgetDetails.width?.dp

            val calculatedHeight =
                if (widgetDetails.width != null && widgetDetails.height != null) {
                    val aspectRatio = widgetDetails.height.toFloat() / widgetDetails.width.toFloat()

                    val marginLeft = widgetDetails.styling?.leftMargin?.toFloatOrNull()?.dp ?: 0.dp
                    val marginRight =
                        widgetDetails.styling?.rightMargin?.toFloatOrNull()?.dp ?: 0.dp

                    val horizontalMargin = marginLeft + marginRight

                    val actualWidth = (staticWidth ?: screenWidth) - horizontalMargin
                    ((actualWidth.value.minus(12) * aspectRatio).div(2)).dp
                } else {
                    (widgetDetails.height?.minus(12))?.div(2)?.dp
                }

            val widgetImagesPairs = widgetDetails.widgetImages.turnToPair()
            val pagerState = rememberPagerState(pageCount = {
                widgetImagesPairs.count()
            })

            LaunchedEffect(pagerState.currentPage, isVisible) {
                if (isVisible) {
                    campaign?.id?.let {
                        trackCampaignActions(
                            it,
                            "IMP",
                            widgetImagesPairs[pagerState.currentPage].first.id
                        )

                        if (widgetImagesPairs[pagerState.currentPage].first.id != null && !impressions.value.contains(
                                widgetImagesPairs[pagerState.currentPage].first.id
                            )
                        ) {
                            val impressions = ArrayList(impressions.value)
                            impressions.add(widgetImagesPairs[pagerState.currentPage].first.id)
                            _impressions.emit(impressions)
                            trackEvents(
                                it,
                                "viewed",
                                mapOf("widget_image" to widgetImagesPairs[pagerState.currentPage].first.id!!)
                            )

                        } else if (!impressions.value.contains(it)) {
                            val impressions = ArrayList(impressions.value)
                            impressions.add(it)
                            _impressions.emit(impressions)
                            trackEvents(
                                it,
                                "viewed",
                                mapOf("widget_image" to widgetImagesPairs[pagerState.currentPage].first.id!!)
                            )
                        }

                        trackCampaignActions(
                            it,
                            "IMP",
                            widgetImagesPairs[pagerState.currentPage].second.id
                        )

                        if (widgetImagesPairs[pagerState.currentPage].second.id != null && !impressions.value.contains(
                                widgetImagesPairs[pagerState.currentPage].second.id
                            )
                        ) {
                            val impressions = ArrayList(impressions.value)
                            impressions.add(widgetImagesPairs[pagerState.currentPage].second.id)
                            _impressions.emit(impressions)
                            trackEvents(
                                it,
                                "viewed",
                                mapOf("widget_image" to widgetImagesPairs[pagerState.currentPage].second.id!!)
                            )

                        } else if (!impressions.value.contains(it)) {
                            val impressions = ArrayList(impressions.value)
                            impressions.add(it)
                            _impressions.emit(impressions)
                            trackEvents(
                                it,
                                "viewed",
                                mapOf("widget_image" to widgetImagesPairs[pagerState.currentPage].second.id!!)
                            )
                        }
                    }
                }
            }

            DoubleWidgets(
                modifier = modifier
                    .padding(
                        top = (widgetDetails.styling?.topMargin?.toFloatOrNull() ?: 0f).dp,
                        bottom = (widgetDetails.styling?.bottomMargin?.toFloatOrNull() ?: 0f).dp,
                        start = (widgetDetails.styling?.leftMargin?.toFloatOrNull() ?: 0f).dp,
                        end = (widgetDetails.styling?.rightMargin?.toFloatOrNull() ?: 0f).dp,
                    )
                    .onGloballyPositioned { layoutCoordinates ->
                        val visibilityRect = layoutCoordinates.boundsInWindow()
                        val parentHeight =
                            layoutCoordinates.parentLayoutCoordinates?.size?.height ?: 0
                        val widgetHeight = layoutCoordinates.size.height
                        val isAtLeastHalfVisible = visibilityRect.top < parentHeight &&
                                visibilityRect.bottom > 0 &&
                                (visibilityRect.height >= widgetHeight * 0.5f)

                        isVisible = isAtLeastHalfVisible
                    },
                pagerState = pagerState,
                itemsCount = widgetImagesPairs.count(),
                width = widthInDp ?: staticWidth,
                itemContent = { index ->
                    val (leftImage, rightImage) = widgetImagesPairs[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (leftImage.image != null) {
                            ImageCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        if (leftImage.link != null) {
                                            clickEvent(
                                                link = leftImage.link,
                                                campaignId = campaign.id,
                                                widgetImageId = leftImage.id
                                            )

                                            trackEvents(
                                                campaign.id,
                                                "clicked",
                                                mapOf("widget_image" to leftImage.id!!)
                                            )
                                        }

                                    },
                                imageUrl = leftImage.image,
                                widgetDetails = widgetDetails,
                                height = calculatedHeight,
                                placeHolder = placeHolder,
                                placeholderContent = placeholderContent
                            )
                        }
                        if (rightImage.image != null) {
                            ImageCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        if (rightImage.link != null) {
                                            clickEvent(
                                                link = rightImage.link,
                                                campaignId = campaign.id,
                                                widgetImageId = rightImage.id
                                            )

                                            trackEvents(
                                                campaign.id,
                                                "clicked",
                                                mapOf("widget_image" to rightImage.id!!)
                                            )
                                        }
                                    },
                                imageUrl = rightImage.image,
                                widgetDetails = widgetDetails,
                                height = calculatedHeight,
                                placeHolder = placeHolder,
                                placeholderContent = placeholderContent
                            )
                        }
                    }
                }
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Composable
    override fun BottomSheet() {
        var showBottomSheet by remember { mutableStateOf(true) }

        val campaignsData = campaigns.collectAsStateWithLifecycle()

        val campaign =
            campaignsData.value.firstOrNull { it.campaignType == "BTS" && it.details is BottomSheetDetails }

        val bottomSheetDetails = when (val details = campaign?.details) {
            is BottomSheetDetails -> details
            else -> null
        }

        val shouldShowBottomSheet = campaign?.triggerEvent.isNullOrEmpty() ||
                trackedEventNames.contains(campaign.triggerEvent)

        if (bottomSheetDetails != null && showBottomSheet && shouldShowBottomSheet) {

            LaunchedEffect(Unit) {
                campaign?.id?.let {
                    trackCampaignActions(it, "IMP")
                    trackEvents(it, "viewed")
                }
            }

            BottomSheetComponent(
                onDismissRequest = {
                    showBottomSheet = false
                },
                bottomSheetDetails = bottomSheetDetails,
                onClick = { ctaLink ->
                    if (!ctaLink.isNullOrEmpty()) {
                        campaign?.id?.let { campaignId ->
                            clickEvent(link = ctaLink, campaignId = campaignId)
                            trackEvents(campaignId, "clicked")
                        }
                    }
                },
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Composable
    override fun Survey() {
        var showSurvey by remember { mutableStateOf(true) }

        val campaignsData = campaigns.collectAsStateWithLifecycle()

        val campaign =
            campaignsData.value.firstOrNull { it.campaignType == "SUR" && it.details is SurveyDetails }

        val surveyDetails = when (val details = campaign?.details) {
            is SurveyDetails -> details
            else -> null
        }

        val shouldShowSurvey = campaign?.triggerEvent.isNullOrEmpty() ||
                trackedEventNames.contains(campaign.triggerEvent)

        if (surveyDetails != null && showSurvey && shouldShowSurvey) {

            LaunchedEffect(Unit) {
                campaign?.id?.let {
                    trackCampaignActions(it, "IMP")
                    trackEvents(it, "viewed")
                }
            }

            SurveyBottomSheet(
                onDismissRequest = {
                    showSurvey = false
                },
                surveyDetails = surveyDetails,
                onSubmitFeedback = { feedback ->
                    coroutineScope.launch {
                        repository.captureSurveyResponse(
                            accessToken,
                            SurveyFeedbackPostRequest(
                                user_id = userId,
                                survey = surveyDetails.id,
                                responseOptions = feedback.responseOptions,
                                comment = feedback.comment
                            )
                        )
                        trackEvents(surveyDetails.campaign, "clicked")
                    }
                },
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Composable
    override fun Modals() {
        val campaignsData = campaigns.collectAsStateWithLifecycle()

        var showPopupModal by remember { mutableStateOf(showModal) }

        val campaign =
            campaignsData.value.firstOrNull { it.campaignType == "MOD" && it.details is ModalDetails }

        val modalDetails = when (val details = campaign?.details) {
            is ModalDetails -> details
            else -> null
        }

        val shouldShowModals = campaign?.triggerEvent.isNullOrEmpty() ||
                trackedEventNames.contains(campaign.triggerEvent)

        if (modalDetails != null && showPopupModal && shouldShowModals) {

            LaunchedEffect(Unit) {
                campaign?.id?.let {
                    trackCampaignActions(it, "IMP")
                    trackEvents(it, "viewed")
                }
            }

            PopupModal(
                onCloseClick = {
                    showPopupModal = false
                    showModal = false
                },
                modalDetails = modalDetails,
                onModalClick = {
                    campaign?.id?.let { campaignId ->
                        trackCampaignActions(campaignId, "CLK")
                        trackEvents(campaignId, "clicked")
                    }
                    val link = modalDetails.modals?.getOrNull(0)?.link
                    if (!isValidUrl(link)) {
                        link?.let { navigateToScreen(it) }
                    } else {
                        link?.let { openUrl(it) }
                    }

                    showPopupModal = false
                    showModal = false
                },
            )
        }
    }


    @Composable
    override fun TestUserButton(
        modifier: Modifier,
        screenName: String?
    ) {
        val context = LocalContext.current
        var shouldAnalyze by remember { mutableStateOf(false) }
        var isCapturing by remember { mutableStateOf(false) }

        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(shouldAnalyze) {
            if (shouldAnalyze) {
                isCapturing = true
                delay(500)
                val activity = context as? Activity
                val rootView = activity?.window?.decorView?.rootView
                rootView?.let {
                    val screenToAnalyze = screenName ?: currentScreen
                    analyzeViewRoot(it, screenToAnalyze, activity)

                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Screen captured successfully!")
                    }
                }
                shouldAnalyze = false
                isCapturing = false
            }
        }

        if (
            isScreenCaptureEnabled &&
            !isCapturing
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                FloatingActionButton(
                    onClick = {
                        shouldAnalyze = true
                    },
                    modifier = modifier
                        .padding(bottom = 86.dp, end = 16.dp)
                        .align(Alignment.BottomEnd),
                    containerColor = Color.White
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        text = "Capture Screen"
                    )
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 80.dp)
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    internal fun handleTooltipAction(tooltip: Tooltip, isClick: Boolean = false) {
        coroutineScope.launch {
            val campaign = campaigns.value.firstOrNull { campaign ->
                campaign.campaignType == "TTP" && campaign.details is TooltipsDetails && campaign.details.tooltips?.any { it.id == tooltip.id } != null
            } ?: campaigns.value.firstOrNull { campaign ->
                campaign.campaignType == "TTP" && campaign.details is TooltipsDetails
            }
            repository.trackTooltipsActions(
                accessToken, TrackActionTooltips(
                    campaign_id = campaign?.id,
                    user_id = userId,
                    event_type = "IMP",
                    tooltip_id = tooltip.id
                )
            )
            trackEvents(
                campaign?.id,
                "viewed",
                mapOf("tooltip_id" to tooltip.id!!)
            )

            if (isClick) {
                if (!tooltip.deepLinkUrl.isNullOrEmpty()) {
                    if (tooltip.clickAction == "deepLink") {
                        if (!isValidUrl(tooltip.deepLinkUrl)) {
                            navigateToScreen(tooltip.deepLinkUrl)
                        } else {
                            openUrl(tooltip.deepLinkUrl)
                        }
                    } else {
                        dismissTooltip()
                    }
                } else {
                    dismissTooltip()
                }
            }
        }
    }

    internal fun dismissTooltip() {
        coroutineScope.launch {
            _tooltipTargetView.emit(null)
            _showcaseVisible.emit(false)
        }
    }

    private fun clickEvent(link: Any?, campaignId: String, widgetImageId: String? = null) {

        if (link != null && link is String) {
            if (link.isNotEmpty()) {
                if (!isValidUrl(link)) {
                    navigateToScreen(link)
                } else {
                    openUrl(link)
                }
                trackCampaignActions(campaignId, "CLK", widgetImageId)
            }
        } else if (link is Map<*, *>) {
            val json = JSONObject(link)
            handleDeepLink(json, campaignId, widgetImageId)
        } else if (link is JSONObject) {
            handleDeepLink(link, campaignId, widgetImageId)
        }
    }

    private fun handleDeepLink(json: JSONObject, campaignId: String, widgetImageId: String?) {
        try {
            val value = json.optString("value", null)
            val type = json.optString("type", null)
            val context = json.optJSONObject("context")?.toMap()

            if (value != null) {
                navigateToScreen(value)
            }

            trackCampaignActions(campaignId, "CLK", widgetImageId)
        } catch (e: Exception) {
            Log.e("DeepLinkException", e.message.toString())
        }
    }

    private fun List<WidgetImage>.turnToPair(): List<Pair<WidgetImage, WidgetImage>> {
        if (this.isEmpty()) {
            return emptyList()
        }
        val widgetImagePairs: List<Pair<WidgetImage, WidgetImage>> = this
            .sortedBy { it.order }
            .windowed(2, 2, partialWindows = false) { (first, second) ->
                first to second
            }

        return widgetImagePairs
    }

    private fun trackCampaignActions(
        campId: String,
        eventType: String,
        widgetImageId: String? = null
    ) {
        coroutineScope.launch {
            if (eventType != "CLK") {
                if (widgetImageId != null && !impressions.value.contains(widgetImageId)) {
                    val impressions = ArrayList(impressions.value)
                    impressions.add(widgetImageId)
                    _impressions.emit(impressions)
                    repository.trackActions(
                        accessToken,
                        TrackAction(campId, userId, eventType, widgetImageId)
                    )

                } else if (!impressions.value.contains(campId)) {
                    val impressions = ArrayList(impressions.value)
                    impressions.add(campId)
                    _impressions.emit(impressions)
                    repository.trackActions(
                        accessToken,
                        TrackAction(campId, userId, eventType, null)
                    )
                }
            } else {
                repository.trackActions(
                    accessToken,
                    TrackAction(campId, userId, eventType, widgetImageId)
                )
            }
        }
    }


    private fun isValidUrl(url: String?): Boolean {
        return !url.isNullOrEmpty() && Patterns.WEB_URL.matcher(url).matches()
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }
}