package com.appversal.appstorys.api

import retrofit2.HttpException
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.IOException

internal interface ApiService {

    @POST("api/v1/users/validate-account/")
    suspend fun validateAccount(
        @Body request: ValidateAccountRequest
    ): ValidateAccountResponse

    @POST("api/v1/users/track-screen/")
    suspend fun trackScreen(
        @Header("Authorization") token: String,
        @Body request: TrackScreenRequest
    ): TrackScreenResponse

    @POST("api/v1/users/track-user/")
    suspend fun trackUser(
        @Header("Authorization") token: String,
        @Body request: TrackUserRequest,

    ): CampaignResponse

    @POST("api/v1/users/track-action/")
    suspend fun trackAction(
        @Header("Authorization") token: String,
        @Body request: TrackAction
    )

    @POST("api/v1/campaigns/capture-csat-response/")
    suspend fun sendCSATResponse(
        @Header("Authorization") token: String,
        @Body request: CsatFeedbackPostRequest
    )

    @POST("api/v1/campaigns/reel-like/")
    suspend fun sendReelLikeStatus(
        @Header("Authorization") token: String,
        @Body request: ReelStatusRequest
    )

    @POST("api/v1/users/track-action/")
    suspend fun trackReelAction(
        @Header("Authorization") token: String,
        @Body request: ReelActionRequest
    )

    @POST("api/v1/users/track-action/")
    suspend fun trackStoriesAction(
        @Header("Authorization") token: String,
        @Body request: TrackActionStories
    )

    @POST("api/v1/users/track-action/")
    suspend fun trackTooltipsAction(
        @Header("Authorization") token: String,
        @Body request: TrackActionTooltips
    )

    @POST("api/v1/appinfo/identify-elements/")
    suspend fun identifyTooltips(
        @Header("Authorization") token: String,
        @Query("screen") screen: String,
        @Body request: IdentifyTooltips
    )
}

internal sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
}

internal suspend fun <T> safeApiCall(apiCall: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(apiCall())
    } catch (e: HttpException) {
        ApiResult.Error(e.message ?: "Unknown error", e.code())
    } catch (e: IOException) {
        ApiResult.Error("Network error. Please check your internet connection.")
    } catch (e: Exception) {
        ApiResult.Error("Unexpected error occurred.")
    }
}