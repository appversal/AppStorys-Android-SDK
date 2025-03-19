package com.appversal.appstorys.api

import com.google.gson.annotations.SerializedName

data class ValidateAccountRequest(
    val app_id: String?,
    val account_id: String?
)

data class TrackAction(
    val campaign_id: String?,
    val user_id: String?,
    val event_type: String?,
    val widget_image: String?
)

data class ReelStatusRequest(
    val user_id: String?,
    val action: String?,
    val reel: String?
)

data class TrackActionStories(
    val campaign_id: String?,
    val user_id: String?,
    val event_type: String?,
    val story_slide: String?
)

data class TrackActionTooltips(
    val campaign_id: String?,
    val user_id: String?,
    val event_type: String?,
    val tooltip_id: String?
)

data class ReelActionRequest(
    val user_id: String?,
    val event_type: String?,
    val reel_id: String?,
    val campaign_id: String?,
)

data class ValidateAccountResponse(
    val access_token: String?
)

data class TrackScreenRequest(
    val screen_name: String?,
    val position_list: List<String>?,
    val element_list: List<String>?
)

data class TrackScreenResponse(
    val campaigns: List<String>?
)

data class TrackUserRequest(
    val user_id: String?,
    val campaign_list: List<String>?,
    val attributes: List<Map<String, Any>>?
)

data class CampaignResponse(
    val user_id: String?,
    val campaigns: List<Campaign>?
)

data class Campaign(
    val id: String?,
    @SerializedName("campaign_type") val campaignType: String?,
    val details: Any?,
    val position: String?
)

data class StoryGroup(
    val id: String?,
    val name: String?,
    val thumbnail: String?,
    val ringColor: String?,
    val nameColor: String?,
    val order: Int?,
    val slides: List<StorySlide>?
)

data class StorySlide(
    val id: String?,
    val parent: String?,
    val image: String?,
    val video: String?,
    val link: String?,
    @SerializedName("button_text") val buttonText: String?,
    val order: Int?
)

data class BannerDetails(
    val id: String?,
    val image: String?,
    val width: Int?,
    val height: Int?,
    val link: Any?,
    val styling: Styling?,
    val lottie_data: String?
)

data class Styling(
    val isClose: Boolean?,
    val marginBottom: Int?,
    val topLeftRadius: Int?,
    val topRightRadius: Int?,
    val bottomLeftRadius: Int?,
    val bottomRightRadius: Int?
)

data class WidgetDetails(
    val id: String?,
    val type: String?,
    val width: Int?,
    val height: Int?,
    @SerializedName("widget_images") val widgetImages: List<WidgetImage>?,
    val campaign: String?,
    val screen: String?,
    val styling: Styling?
)

data class WidgetImage(
    val id: String?,
    val image: String?,
    val link: Any?,
    val order: Int?
)

data class CSATDetails(
    val id: String?,
    val title: String?,
    val height: Int?,
    val width: Int?,
    val styling: CSATStyling?,
    val thankyouImage: String?,
    val thankyouText: String?,
    val thankyouDescription: String?,
    @SerializedName("description_text") val descriptionText: String?,
    @SerializedName("feedback_option") val feedbackOption: FeedbackOption?,
    val campaign: String?,
    val link: String?
)

data class FloaterDetails(
    val id: String?,
    val image: String?,
    val width: Int?,
    val height: Int?,
    val link: String?,
    val position: String?,
    val campaign: String?
)

data class FeedbackOption(
    val option1: String?,
    val option2: String?,
    val option3: String?,
    val option4: String?,
    val option5: String?,
    val option6: String?,
    val option7: String?,
    val option8: String?,
    val option9: String?,
    val option10: String?,
    ) {
    fun toList(): List<String> = listOf(
        option1 ?: "",
        option2 ?: "",
        option3 ?: "",
        option4 ?: "",
        option5 ?: "",
        option6 ?: "",
        option7 ?: "",
        option8 ?: "",
        option9 ?: "",
        option10 ?: "",
        ).filter { it.isNotBlank() }
}

data class CSATStyling(
    val delayDisplay: Int?,
    val displayDelay: String?,
    val csatTitleColor: String?,
    val csatCtaTextColor: String?,
    val csatBackgroundColor: String?,
    val csatOptionTextColour: String?,
    val csatOptionStrokeColor: String?,
    val csatCtaBackgroundColor: String?,
    val csatDescriptionTextColor: String?,
    val csatSelectedOptionTextColor: String?,
    val csatSelectedOptionBackgroundColor: String?
)

data class CsatFeedbackPostRequest(
    val csat: String?,
    val user_id: String?,
    val rating: Int?,
    val feedback_option: String? = null,
    val additional_comments: String = ""
)

data class ReelsDetails(
    val id: String?,
    val reels: List<Reel>?,
    val styling: ReelStyling?
)

data class Reel(
    val id: String?,
    @SerializedName("button_text") val buttonText: String?,
    val order: Int?,
    @SerializedName("description_text") val descriptionText: String?,
    val video: String?,
    val likes: Int?,
    val thumbnail: String?,
    val link: String?
)

data class ReelStyling(
    val ctaBoxColor: String?,
    val cornerRadius: String?,
    val ctaTextColor: String?,
    val thumbnailWidth: String?,
    val likeButtonColor: String?,
    val thumbnailHeight: String?,
    val descriptionTextColor: String?
)

data class TooltipsDetails(
    @SerializedName("_id") val id: String?,
    val campaign: String?,
    val name: String?,
    val tooltips: List<Tooltip>?,
    @SerializedName("created_at") val createdAt: String?
)

data class Tooltip(
    val type: String?,
    val url: String?,
    val clickAction: String?,
    val link: String?,
    val target: String?,
    val order: Int?,
    val styling: TooltipStyling?,
    @SerializedName("_id") val id: String?
)

data class TooltipStyling(
    val tooltipDimensions: TooltipDimensions?,
    val highlightRadius: String?,
    val highlightPadding: String?,
    val backgroudColor: String?,
    val enableBackdrop: Boolean?,
    val tooltipArrow: TooltipArrow?,
    val spacing: TooltipSpacing?,
    val closeButton: Boolean?
)

data class TooltipDimensions(
    val height: String?,
    val width: String?,
    val cornerRadius: String?
)

data class TooltipArrow(
    val arrowHeight: String?,
    val arrowWidth: String?
)

data class TooltipSpacing(
    val padding: TooltipPadding?
)

data class TooltipPadding(
    val paddingTop: Int?,
    val paddingRight: Int?,
    val paddingBottom: Int?,
    val paddingLeft: Int?
)

data class PipDetails(
    val id: String?,
    val position: String?,
    val small_video: String?,
    val large_video: String?,
    val height: Int?,
    val width: Int?,
    val link: String?,
    val campaign: String?,
    val button_text: String?
)