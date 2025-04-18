package com.appversal.appstorys.api
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep data class ValidateAccountRequest(
    val app_id: String?,
    val account_id: String?
)

@Keep data class TrackAction(
    val campaign_id: String?,
    val user_id: String?,
    val event_type: String?,
    val widget_image: String?
)

@Keep data class ReelStatusRequest(
    val user_id: String?,
    val action: String?,
    val reel: String?
)

@Keep data class TrackActionStories(
    val campaign_id: String?,
    val user_id: String?,
    val event_type: String?,
    val story_slide: String?
)

@Keep data class TrackActionTooltips(
    val campaign_id: String?,
    val user_id: String?,
    val event_type: String?,
    val tooltip_id: String?
)

@Keep data class IdentifyTooltips(
    val element: String?
)

@Keep data class ReelActionRequest(
    val user_id: String?,
    val event_type: String?,
    val reel_id: String?,
    val campaign_id: String?,
)

@Keep data class ValidateAccountResponse(
    val access_token: String?
)

@Keep data class TrackScreenRequest(
    val screen_name: String?,
    val position_list: List<String>?,
)

@Keep data class TrackScreenResponse(
    val campaigns: List<String>?
)

@Keep data class TrackUserRequest(
    val user_id: String?,
    val campaign_list: List<String>?,
    val attributes: List<Map<String, Any>>?
)

@Keep data class CampaignResponse(
    val user_id: String?,
    val campaigns: List<Campaign>?
)

@Keep data class Campaign(
    val id: String?,
    @SerializedName("campaign_type") val campaignType: String?,
    val details: Any?,
    val position: String?
)

@Keep data class StoryGroup(
    val id: String?,
    val name: String?,
    val thumbnail: String?,
    val ringColor: String?,
    val nameColor: String?,
    val order: Int?,
    val slides: List<StorySlide>?
)

@Keep data class StorySlide(
    val id: String?,
    val parent: String?,
    val image: String?,
    val video: String?,
    val link: String?,
    @SerializedName("button_text") val buttonText: String?,
    val order: Int?
)

@Keep data class BannerDetails(
    val id: String?,
    val image: String?,
    val width: Int?,
    val height: Int?,
    val link: Any?,
    val styling: Styling?,
    val lottie_data: String?
)

@Keep data class Styling(
    val isClose: Boolean?,
    val marginBottom: Int?,
    val topLeftRadius: Int?,
    val topRightRadius: Int?,
    val bottomLeftRadius: Int?,
    val bottomRightRadius: Int?
)

@Keep data class WidgetDetails(
    val id: String?,
    val type: String?,
    val width: Int?,
    val height: Int?,
    @SerializedName("widget_images") val widgetImages: List<WidgetImage>?,
    val campaign: String?,
    val screen: String?,
    val styling: Styling?
)

@Keep data class WidgetImage(
    val id: String?,
    val image: String?,
    val link: Any?,
    val order: Int?
)

@Keep data class CSATDetails(
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

@Keep data class FloaterDetails(
    val id: String?,
    val image: String?,
    val width: Int?,
    val height: Int?,
    val link: String?,
    val position: String?,
    val campaign: String?
)

@Keep data class FeedbackOption(
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

@Keep data class CSATStyling(
    val delayDisplay: Int?,
    val displayDelay: String?,
    val csatTitleColor: String?,
    val csatCtaTextColor: String?,
    val csatLowStarColor: String?,
    val csatHighStarColor: String?,
    val csatUnselectedStarColor: String?,
    val csatBackgroundColor: String?,
    val csatOptionTextColour: String?,
    val csatOptionStrokeColor: String?,
    val csatCtaBackgroundColor: String?,
    val csatAdditionalTextColor: String?,
    val csatDescriptionTextColor: String?,
    val csatSelectedOptionTextColor: String?,
    val csatSelectedOptionBackgroundColor: String?,

)

@Keep data class CsatFeedbackPostRequest(
    val csat: String?,
    val user_id: String?,
    val rating: Int?,
    val feedback_option: String? = null,
    val additional_comments: String = ""
)

@Keep data class ReelsDetails(
    val id: String?,
    val reels: List<Reel>?,
    val styling: ReelStyling?
)

@Keep data class Reel(
    val id: String?,
    @SerializedName("button_text") val buttonText: String?,
    val order: Int?,
    @SerializedName("description_text") val descriptionText: String?,
    val video: String?,
    val likes: Int?,
    val thumbnail: String?,
    val link: String?
)

@Keep data class ReelStyling(
    val ctaBoxColor: String?,
    val cornerRadius: String?,
    val ctaTextColor: String?,
    val thumbnailWidth: String?,
    val likeButtonColor: String?,
    val thumbnailHeight: String?,
    val descriptionTextColor: String?
)

@Keep data class TooltipsDetails(
    @SerializedName("_id") val id: String?,
    val campaign: String?,
    val name: String?,
    val tooltips: List<Tooltip>?,
    @SerializedName("created_at") val createdAt: String?
)

@Keep data class Tooltip(
    val type: String?,
    val url: String?,
    val clickAction: String?,
    val link: String?,
    val target: String?,
    val order: Int?,
    val styling: TooltipStyling?,
    @SerializedName("_id") val id: String?
)

@Keep data class TooltipStyling(
    val tooltipDimensions: TooltipDimensions?,
    val highlightRadius: String?,
    val highlightPadding: String?,
    val backgroudColor: String?,
    val enableBackdrop: Boolean?,
    val tooltipArrow: TooltipArrow?,
    val spacing: TooltipSpacing?,
    val closeButton: Boolean?
)

@Keep data class TooltipDimensions(
    val height: String?,
    val width: String?,
    val cornerRadius: String?
)

@Keep data class TooltipArrow(
    val arrowHeight: String?,
    val arrowWidth: String?
)

@Keep data class TooltipSpacing(
    val padding: TooltipPadding?
)

@Keep data class TooltipPadding(
    val paddingTop: Int?,
    val paddingRight: Int?,
    val paddingBottom: Int?,
    val paddingLeft: Int?
)

@Keep data class PipDetails(
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