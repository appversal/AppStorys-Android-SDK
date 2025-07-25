package com.appversal.appstorys.api
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep data class ValidateAccountRequest(
    val app_id: String?,
    val account_id: String?
)

@Keep data class ValidateAccountResponse(
    val access_token: String?
)

@Keep
data class MqttConnectionResponse(
    val mqtt: MqttConfig,
    val userID: String,
    val screen_capture_enabled: Boolean?,
)

@Keep
data class MqttConfig(
    val broker: String,
    val clientID: String,
    val topic: String
)

@Keep
data class TrackUserMqttRequest(
    val screenName: String,
    val user_id: String,
    val attributes: Map<String, Any>
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
    val userId: String?,
    @SerializedName("message_id") val messageId: String?,
    val campaigns: List<Campaign>?
)

@Keep data class Campaign(
    val id: String?,
    @SerializedName("campaign_type") val campaignType: String?,
    val details: Any?,
    val position: String?,
    val screen: String?,
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

@Keep data class ReelActionRequest(
    val user_id: String?,
    val event_type: String?,
    val reel_id: String?,
    val campaign_id: String?,
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
    val styling: BannerStyling?,
    val lottie_data: String?
)

@Keep data class BannerStyling(
    val enableCloseButton: Boolean?,
    val marginLeft: Int?,
    val marginRight: Int?,
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
    val styling: WidgetStyling?
)

@Keep data class WidgetStyling(
    val topMargin: String?,
    val leftMargin: String?,
    val rightMargin: String?,
    val bottomMargin: String?,
    val topLeftRadius: String?,
    val topRightRadius: String?,
    val bottomLeftRadius: String?,
    val bottomRightRadius: String?,
)

@Keep data class WidgetImage(
    val id: String?,
    val image: String?,
    val link: Any?,
    val order: Int?,
    val lottie_data: String?,
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
    val highStarText: String?,
    val lowStarText: String?,
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
    val campaign: String?,
    val styling: FloaterStyling?,
    val lottie_data: String?,
)

@Keep data class FloaterStyling(
    val topLeftRadius: String?,
    val topRightRadius: String?,
    val bottomLeftRadius: String?,
    val bottomRightRadius: String?
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
    val csatOptionBoxColour: String?,
    val csatOptionTextColour: String?,
    val csatOptionStrokeColor: String?,
    val csatCtaBackgroundColor: String?,
    val csatAdditionalTextColor: String?,
    val csatDescriptionTextColor: String?,
    val csatSelectedOptionTextColor: String?,
    val csatSelectedOptionStrokeColor: String?,
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
    val deepLinkUrl: String?,
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
    val styling: PipStyling?,
    val link: String?,
    val campaign: String?,
    val button_text: String?
)

@Keep data class PipStyling(
    val ctaWidth: String?,
    val fontSize: String?,
    val ctaHeight: String?,
    val isMovable: Boolean?,
    val marginTop: String?,
    val fontFamily: String?,
    val marginLeft: String?,
    val marginRight: String?,
    val cornerRadius: String?,
    val ctaFullWidth: Boolean?,
    val marginBottom: String?,
    val fontDecoration: List<String>?,
    val ctaButtonTextColor: String?,
    val ctaButtonBackgroundColor: String?
)

@Keep data class BottomSheetDetails(
    @SerializedName("_id") val id: String?,
    val campaign: String?,
    val name: String?,
    val elements: List<BottomSheetElement>?,
    val cornerRadius: String?,
    val enableCrossButton: String?,
    val triggerType: String?,
    val selectedEvent: String?,
)

@Keep data class BottomSheetElement(
    val type: String?,
    val alignment: String?,
    val order: Int?,
    val id: String?,

    // Image-specific
    val url: String? = null,
    val imageLink: String? = null,
    val overlayButton: Boolean? = null,


    // Body-specific
    val titleText: String? = null,
    val titleFontStyle: FontStyle? = null,
    val titleFontSize: Int? = null,
    val descriptionText: String? = null,
    val descriptionFontStyle: FontStyle? = null,
    val descriptionFontSize: Int? = null,
    val titleLineHeight: Float? = null,
    val descriptionLineHeight: Float? = null,
    val spacingBetweenTitleDesc: Float? = null,
    val bodyBackgroundColor: String? = null,

    // CTA-specific
    val ctaText: String? = null,
    val ctaLink: String? = null,
    val position: String? = null,
    val ctaBorderRadius: Int? = null,
    val ctaHeight: Int? = null,
    val ctaWidth: Int? = null,
    val ctaTextColour: String? = null,
    val ctaFontSize: String? = null,
    val ctaFontFamily: String? = null,
    val ctaFontDecoration: List<String>? = emptyList(),
    val ctaBoxColor: String? = null,
    val ctaBackgroundColor: String? = null,
    val ctaFullWidth: Boolean? = null,

    // Shared paddings
    val paddingLeft: Int? = null,
    val paddingRight: Int? = null,
    val paddingTop: Int? = null,
    val paddingBottom: Int? = null
)

@Keep
data class SurveyDetails(
    val id: String?,
    val name: String?,
    val styling: SurveyStyling?,
    val surveyQuestion: String?,
    val surveyOptions: Map<String, String>?,
    val campaign: String?,
    val hasOthers: Boolean?
)

@Keep
data class SurveyStyling(
    val optionColor: String?,
    val displayDelay: String?,
    val backgroundColor: String?,
    val optionTextColor: String?,
    val othersTextColor: String?,
    val surveyTextColor: String?,
    val ctaTextIconColor: String?,
    val ctaBackgroundColor: String?,
    val selectedOptionColor: String?,
    val surveyQuestionColor: String?,
    val othersBackgroundColor: String?,
    val selectedOptionTextColor: String?
)

@Keep data class SurveyFeedbackPostRequest(
    val user_id: String?,
    val survey: String?,
    val responseOptions: List<String>? = null,
    val comment: String? = ""
)

@Keep data class FontStyle(
    val fontFamily: String?,
    val colour: String?,
    val decoration: List<String>?
)

@Keep data class ModalDetails(
    @SerializedName("_id") val id: String?,
    val campaign: String?,
    val modals: List<Modal>?,
)

@Keep data class Modal(
    @SerializedName("media_type") val mediaType: String?,
    val size: String?,
    val link: String?,
    val borderRadius: String?,
    val backgroundOpacity: Double? = null,
    val name: String?,
    val url: String?,
    @SerializedName("_id") val id: String?,
)
