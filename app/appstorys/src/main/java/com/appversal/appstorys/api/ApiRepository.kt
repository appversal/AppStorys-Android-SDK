package com.appversal.appstorys.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class ApiRepository(private val apiService: ApiService) {
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
    suspend fun getCampaigns(accessToken: String, screenName: String, positions: List<String>?, elementList: List<String>?): List<String>? {
        return withContext(Dispatchers.IO) {
            when (val result = safeApiCall {
                apiService.trackScreen(
                    token = "Bearer $accessToken",
                    request = TrackScreenRequest(screen_name = screenName, positions, elementList)
                ).campaigns
            }) {
                is ApiResult.Success -> result.data
                is ApiResult.Error -> {
                    Log.e("ApiRepository", "Error getting campaigns: ${result.message}")
                    emptyList()
                }
            }
        }
    }

    suspend fun getCampaignData(
        accessToken: String,
        userId: String,
        campaignList: List<String>,
        attributes: List<Map<String, Any>>?
    ): CampaignResponse? {
        return withContext(Dispatchers.IO) {
            when (val result = safeApiCall {
                apiService.trackUser(
                    token = "Bearer $accessToken",
                    request = TrackUserRequest(user_id = userId, campaign_list = campaignList, attributes = attributes)
                )
            }) {
                is ApiResult.Success -> result.data
                is ApiResult.Error -> {
                    Log.e("ApiRepository", "Error getting campaign data: ${result.message}")
                    null
                }
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
}