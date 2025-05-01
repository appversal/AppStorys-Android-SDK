package com.appversal.appstorys

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appversal.appstorys.api.ApiRepository
import com.appversal.appstorys.api.BannerDetails
import com.appversal.appstorys.api.CSATDetails
import com.appversal.appstorys.api.Campaign
import com.appversal.appstorys.api.CsatFeedbackPostRequest
import com.appversal.appstorys.api.FloaterDetails
import com.appversal.appstorys.api.ReelsDetails
import com.appversal.appstorys.api.RetrofitClient
import com.appversal.appstorys.api.TrackAction
import com.appversal.appstorys.api.WidgetDetails
import com.appversal.appstorys.api.WidgetImage
import com.appversal.appstorys.ui.AutoSlidingCarousel
import com.appversal.appstorys.ui.CarousalImage
import com.appversal.appstorys.ui.CsatDialog
import com.appversal.appstorys.ui.DoubleWidgets
import com.appversal.appstorys.ui.FullScreenVideoScreen
import com.appversal.appstorys.ui.ImageCard
import com.appversal.appstorys.ui.OverlayFloater
import com.appversal.appstorys.ui.ReelsRow
import com.appversal.appstorys.ui.TooltipContent
import com.appversal.appstorys.ui.TooltipPopup
import com.appversal.appstorys.ui.TooltipPopupPosition
import com.appversal.appstorys.ui.calculateTooltipPopupPosition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.appversal.appstorys.api.ApiResult
import com.appversal.appstorys.api.BottomSheetDetails
import com.appversal.appstorys.api.IdentifyTooltips
import com.appversal.appstorys.api.ModalDetails
import com.appversal.appstorys.api.PipDetails
import com.appversal.appstorys.api.ReelActionRequest
import com.appversal.appstorys.api.ReelStatusRequest
import com.appversal.appstorys.api.StoryGroup
import com.appversal.appstorys.api.Tooltip
import com.appversal.appstorys.api.TooltipsDetails
import com.appversal.appstorys.api.TrackActionStories
import com.appversal.appstorys.api.TrackActionTooltips
import com.appversal.appstorys.ui.BottomSheetComponent
import com.appversal.appstorys.ui.PipVideo
import com.appversal.appstorys.ui.PopupModal
import com.appversal.appstorys.ui.StoryAppMain
import com.appversal.appstorys.ui.getLikedReels
import com.appversal.appstorys.ui.saveLikedReels
import com.appversal.appstorys.utils.toMap
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

public interface AppStorysAPI {
    fun initialize(
        context: Application,
        appId: String,
        accountId: String,
        userId: String,
        attributes: List<Map<String, Any>>? = null,
        navigateToScreen: (String) -> Unit
    )

    fun getScreenCampaigns(screenName: String, positionList: List<String>)
    fun trackEvents(campaign_id: String? = null, event: String, metadata: Map<String, Any>? = null)

    @Composable fun CSAT(modifier: Modifier, displayDelaySeconds: Long, position: String?)
    @Composable fun Floater(boxModifier: Modifier, iconModifier: Modifier)
    @Composable fun ToolTipWrapper(targetModifier: Modifier, targetKey: String, isNavigationBarItem: Boolean, requesterView: @Composable (Modifier) -> Unit)
    @Composable fun Pip(modifier: Modifier, bottomPadding: Dp, topPadding: Dp)
    @Composable fun PinnedBanner(modifier: Modifier, contentScale: ContentScale, staticWidth: Dp?, placeHolder: Drawable?, placeholderContent: (@Composable () -> Unit)?, position: String?)
    @Composable fun Widget(modifier: Modifier, contentScale: ContentScale, staticWidth: Dp?, placeHolder: Drawable?, placeholderContent: (@Composable () -> Unit)?, position: String?)
    @Composable fun Stories()
    @Composable fun Reels(modifier: Modifier)
    @Composable fun BottomSheet(onDismissRequest: () -> Unit)
    @Composable fun Modals(onCloseClick: () -> Unit,)
    @Composable fun getBannerHeight(): Dp

    companion object {
        @JvmStatic
        fun getInstance(): AppStorysAPI = AppStorys
    }
}

internal object AppStorys : AppStorysAPI {
    private lateinit var context: Application
    private lateinit var appId: String
    private lateinit var accountId: String
    private lateinit var userId: String
    private var attributes: List<Map<String, Any>>? = null
    private lateinit var navigateToScreen: (String) -> Unit

    override fun initialize(
        context: Application,
        appId: String,
        accountId: String,
        userId: String,
        attributes: List<Map<String, Any>>?,
        navigateToScreen: (String) -> Unit
    ) {
        this.context = context
        this.appId = appId
        this.accountId = accountId
        this.userId = userId
        this.attributes = attributes
        this.navigateToScreen = navigateToScreen
        initiateData()
    }

    private val _campaigns = MutableStateFlow<List<Campaign>>(emptyList())
    private val campaigns: StateFlow<List<Campaign>> get() = _campaigns

    private val _disabledCampaigns = MutableStateFlow<List<String>>(emptyList())
    private val disabledCampaigns: StateFlow<List<String>> get() = _disabledCampaigns

    private val _impressions = MutableStateFlow<List<String>>(emptyList())
    private val impressions: StateFlow<List<String>> get() = _impressions

    private val _viewsCoordinates = MutableStateFlow<Map<String, LayoutCoordinates>>(emptyMap())
    val viewsCoordinates: StateFlow<Map<String, LayoutCoordinates>> =
        _viewsCoordinates.asStateFlow()

    private val _tooltipTargetView = MutableStateFlow<Tooltip?>(null)
    val tooltipTargetView: StateFlow<Tooltip?> = _tooltipTargetView.asStateFlow()

    private val _tooltipViewed = MutableStateFlow<List<String>>(emptyList())
    private val tooltipViewed: StateFlow<List<String>> = _tooltipViewed.asStateFlow()

    private val _showcaseVisible = MutableStateFlow(false)
    val showcaseVisible: StateFlow<Boolean> = _showcaseVisible.asStateFlow()

    private val _selectedReelIndex = MutableStateFlow<Int>(0)
    private val selectedReelIndex: StateFlow<Int> = _selectedReelIndex.asStateFlow()

    private val _reelFullScreenVisible = MutableStateFlow(false)
    private val reelFullScreenVisible: StateFlow<Boolean> = _reelFullScreenVisible.asStateFlow()

    private val apiService = RetrofitClient.apiService
    private val repository = ApiRepository(apiService)
    private var accessToken = ""
    private var currentScreen = ""

    private var showCsat = false

    private var isDataFetched = false
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private fun initiateData() {
        if (isDataFetched) return
        isDataFetched = true
        coroutineScope.launch {
            fetchData()
            showCaseInformation()
        }
    }

    private suspend fun fetchData() {
        try {
            val accessToken = repository.getAccessToken(appId, accountId)

            if (accessToken != null) {
                this.accessToken = accessToken
                currentScreen = "Home Screen"
                val campaignList = repository.getCampaigns(accessToken, currentScreen, null)

                if (campaignList?.isNotEmpty() == true) {
                    val campaignsData =
                        repository.getCampaignData(accessToken, userId, campaignList, attributes)

                    campaignsData?.campaigns?.let { _campaigns.emit(it) }
                }
            }
        } catch (exception: Exception) {
            Log.e("AppStorys", exception.message ?: "Error Fetch Data")
        }
    }

    override fun getScreenCampaigns(
        screenName: String,
        positionList: List<String>
    ) {
        try {
            coroutineScope.launch {
                if (accessToken.isNotEmpty()) {
                    if (currentScreen != screenName) {
                        _disabledCampaigns.emit(emptyList())
                        _impressions.emit(emptyList())
                        currentScreen = screenName
                    }
                    val campaignList =
                        repository.getCampaigns(
                            accessToken,
                            currentScreen,
                            positionList
                        )
                    if (campaignList?.isNotEmpty() == true) {
                        val campaignsData =
                            repository.getCampaignData(
                                accessToken,
                                userId,
                                campaignList,
                                attributes
                            )
                        campaignsData?.campaigns?.let { _campaigns.emit(it) }
                    }
                }
            }
        } catch (exception: Exception) {
            Log.e("AppStorys", exception.message ?: "Error Fetch Data")
        }
    }

    override fun trackEvents(
        campaign_id: String?,
        event: String,
        metadata: Map<String, Any>?
    ) {
        coroutineScope.launch {
            if (accessToken.isNotEmpty()) {
                try {
                    val requestBody = JSONObject().apply {
                        put("user_id", userId)
                        campaign_id?.let { put("campaign_id", it) }
                        put("event", event)
                        metadata?.let { put("metadata", it) }
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
    override fun CSAT(
        modifier: Modifier,
        displayDelaySeconds: Long,
        position: String?
    ) {
        if (!showCsat) {
            val campaignsData = campaigns.collectAsStateWithLifecycle()

            val campaign =
                position?.let { pos -> campaignsData.value.filter { it.position == pos } }
                    ?.firstOrNull { it.campaignType == "CSAT" }
                    ?: campaignsData.value.firstOrNull { it.campaignType == "CSAT" }

            val csatDetails = when (val details = campaign?.details) {
                is CSATDetails -> details
                else -> null
            }

            if (csatDetails != null) {
                val style = csatDetails.styling
                var isVisibleState by remember { mutableStateOf(false) }
                val updatedDelay by rememberUpdatedState(
                    style?.displayDelay?.toLong() ?: displayDelaySeconds
                )

                LaunchedEffect(Unit) {
                    campaign?.id?.let {
                        trackCampaignActions(it, "IMP")
                        trackEvents(it, "viewed")
                    }
                    delay(updatedDelay * 1000)
                    isVisibleState = true
                }

                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    AnimatedVisibility(
                        modifier = modifier,
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

    @Composable
    override fun Floater(
        boxModifier: Modifier,
        iconModifier: Modifier
    ) {
        val campaignsData = campaigns.collectAsStateWithLifecycle()

        val campaign =
            campaignsData.value.firstOrNull { it.campaignType == "FLT" && it.details is FloaterDetails }

        val floaterDetails = when (val details = campaign?.details) {
            is FloaterDetails -> details
            else -> null
        }

        if (floaterDetails != null && !floaterDetails.image.isNullOrEmpty()) {
            LaunchedEffect(Unit) {
                campaign?.id?.let {
                    trackCampaignActions(it, "IMP")
                    trackEvents(it, "viewed")
                }

            }

            Box(modifier = boxModifier.fillMaxWidth()) {
                val alignmentModifier = when (floaterDetails.position) {
                    "right" -> Modifier.align(Alignment.BottomEnd)
                    "left" -> Modifier.align(Alignment.BottomStart)
                    else -> Modifier.align(Alignment.BottomStart)
                }

                OverlayFloater(
                    modifier = iconModifier.then(alignmentModifier),
                    onClick = {
                        if (campaign?.id != null && floaterDetails.link != null) {
                            clickEvent(link = floaterDetails.link, campaignId = campaign.id)
                            trackEvents(campaign.id, "clicked")
                        }

                    },
                    image = floaterDetails.image,
                    height = floaterDetails.height?.dp ?: 60.dp,
                    width = floaterDetails.width?.dp ?: 60.dp
                )
            }
        }
    }


    @Composable
    override fun ToolTipWrapper(
        targetModifier: Modifier,
        targetKey: String,
        isNavigationBarItem: Boolean,
        requesterView: @Composable (Modifier) -> Unit
    ) {
        var position by remember { mutableStateOf(TooltipPopupPosition()) }
        val view = LocalView.current.rootView
        val visibleShowcase by showcaseVisible.collectAsStateWithLifecycle()
        val currentToolTipTarget by tooltipTargetView.collectAsStateWithLifecycle()

        LaunchedEffect(targetKey) {
            repository.tooltipIdentify(
                accessToken = accessToken,
                screen = currentScreen,
                actions = IdentifyTooltips(element = targetKey)
            )
        }

        LaunchedEffect(currentToolTipTarget) {
            if (currentToolTipTarget?.target == targetKey) {
                val campaign =
                    campaigns.value.firstOrNull { it.campaignType == "TTP" && it.details is TooltipsDetails }

                repository.trackTooltipsActions(
                    accessToken, TrackActionTooltips(
                        campaign_id = campaign?.id,
                        user_id = userId,
                        event_type = "IMP",
                        tooltip_id = currentToolTipTarget!!.id
                    )
                )
                trackEvents(campaign?.id, "viewed", mapOf("tooltip_id" to currentToolTipTarget!!.id!!))
            }
        }

        TooltipPopup(
            modifier = targetModifier,
            requesterView = { modifier ->
                requesterView(modifier.onGloballyPositioned { coordinates ->
                    _viewsCoordinates.value = _viewsCoordinates.value.toMutableMap().apply {
                        put(targetKey, coordinates)
                    }
                    position = calculateTooltipPopupPosition(
                        view,
                        coordinates,
                        currentToolTipTarget,
                        isNavigationBarItem
                    )
                })
            },
            backgroundColor = Color.Transparent,
            position = position,
            isShowTooltip = visibleShowcase && currentToolTipTarget?.target == targetKey,
            onDismissRequest = {
                coroutineScope.launch {
                    _tooltipTargetView.emit(null)
                    _showcaseVisible.emit(false)
                }
            },
            tooltip = if (currentToolTipTarget?.target == targetKey) currentToolTipTarget else null,
            isNavigationBarItem = isNavigationBarItem,
            tooltipContent = {
                if (currentToolTipTarget?.target == targetKey) {
                    TooltipContent(
                        tooltip = currentToolTipTarget!!,
                        exitUnit = {
                            coroutineScope.launch {
                                _tooltipTargetView.emit(null)
                                _showcaseVisible.emit(false)
                            }
                        },
                        onClick = {
                            coroutineScope.launch {
                                if (!currentToolTipTarget!!.link.isNullOrEmpty()) {
                                    if (currentToolTipTarget!!.clickAction == "deepLink") {
                                        if (!isValidUrl(currentToolTipTarget!!.link)) {
                                            currentToolTipTarget!!.link?.let {
                                                navigateToScreen(
                                                    it
                                                )
                                            }
                                        } else {
                                            currentToolTipTarget!!.link?.let { openUrl(it) }
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            _tooltipTargetView.emit(null)
                                            _showcaseVisible.emit(false)
                                        }
                                    }

                                    val campaign =
                                        campaigns.value.firstOrNull { it.campaignType == "TTP" && it.details is TooltipsDetails }

                                    repository.trackTooltipsActions(
                                        accessToken, TrackActionTooltips(
                                            campaign_id = campaign?.id,
                                            user_id = userId,
                                            event_type = "CLK",
                                            tooltip_id = currentToolTipTarget!!.id
                                        )
                                    )
                                    trackEvents(campaign?.id, "clicked", mapOf("tooltip_id" to currentToolTipTarget!!.id!!))
                                } else {
                                    coroutineScope.launch {
                                        _tooltipTargetView.emit(null)
                                        _showcaseVisible.emit(false)
                                    }
                                }
                            }
                        }
                    )
                }
            }
        )
    }

    @Composable
    override fun Pip(
        modifier: Modifier,
        bottomPadding: Dp,
        topPadding: Dp,
    ) {
        var showPip by remember { mutableStateOf(true) }
        val campaignsData = campaigns.collectAsStateWithLifecycle()

        val campaign =
            campaignsData.value.firstOrNull { it.campaignType == "PIP" && it.details is PipDetails }

        val pipDetails = when (val details = campaign?.details) {
            is PipDetails -> details
            else -> null
        }

        val isMovable = false

        if (pipDetails != null && !pipDetails.small_video.isNullOrEmpty()) {
            LaunchedEffect(Unit) {
                campaign?.id?.let {
                    trackCampaignActions(it, "IMP")
                    trackEvents(it, "viewed")
                }
            }

            Box(modifier = modifier.fillMaxWidth()) {

                if (showPip) {
                    pipDetails?.large_video?.let {
                        pipDetails.link?.let { it1 ->
                            PipVideo(
                                videoUri = pipDetails.small_video,
                                fullScreenVideoUri = it,
                                onClose = {
                                    showPip = false
                                },
                                height = pipDetails.height?.dp ?: 180.dp,
                                width = pipDetails.width?.dp ?: 120.dp,
                                button_text = pipDetails.button_text.toString(),
                                link = it1,
                                position = pipDetails.position.toString(),
                                bottomPadding = bottomPadding,
                                topPadding = topPadding,
                                isMovable = isMovable,
                                onButtonClick = {
                                    campaign?.id?.let { campaignId ->
                                        trackCampaignActions(campaignId, "CLK")
                                        trackEvents(campaignId, "clicked")
                                    }
                                    if (!isValidUrl(pipDetails.link)) {
                                        navigateToScreen(pipDetails.link)
                                    } else {
                                        openUrl(pipDetails.link)
                                    }
                                },
                                onExpandClick = {
                                    campaign?.id?.let { campaignId ->
                                        trackCampaignActions(campaignId, "IMP")
                                    }
                                }
                            )
                        }
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
                if (tooltipsDetails != null) {
                    for (tooltip in tooltipsDetails.tooltips?.sortedBy { it.order }
                        ?: emptyList()) {
                        if (tooltip.target != null && !_tooltipViewed.value.contains(tooltip.target)) {

                            if (coordinates.contains(tooltip.target)) {
                                while (_tooltipTargetView.value != null) {
                                    delay(500L)
                                }
                                _tooltipTargetView.emit(tooltip)
                                _showcaseVisible.emit(true)
                                _tooltipViewed.emit(
                                    tooltipViewed.value.toMutableList()
                                        .apply { add(tooltip.target) })
                            }
                        }
                    }
                }
            }
        }
    }


    @Composable
    override fun Stories() {
        val campaignsData = campaigns.collectAsStateWithLifecycle()
        val campaign = campaignsData.value.firstOrNull { it.campaignType == "STR" }
        val storiesDetails = (campaign?.details as? List<*>)?.filterIsInstance<StoryGroup>()

        if (!storiesDetails.isNullOrEmpty()) {
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

    @Composable
    override fun Reels(modifier: Modifier) {
        val campaignsData = campaigns.collectAsStateWithLifecycle()
        val campaign =
            campaignsData.value.firstOrNull { it.campaignType == "REL" && it.details is ReelsDetails }
        val reelsDetails = campaign?.details as? ReelsDetails
        val selectedReelIndex by selectedReelIndex.collectAsStateWithLifecycle()
        val visibility by reelFullScreenVisible.collectAsStateWithLifecycle()

        if (reelsDetails?.reels != null && reelsDetails.reels.isNotEmpty()) {
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
                                    trackEvents(campaignId, "viewed", mapOf("reel_id" to it.first.id!!))
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
                                trackEvents(campaignId, "clicked", mapOf("reel_id" to it.first.id!!))
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
    override fun PinnedBanner(
        modifier: Modifier,
        contentScale: ContentScale,
        staticWidth: Dp?,
        placeHolder: Drawable?,
        placeholderContent: (@Composable () -> Unit)?,
        position: String?
    ) {
        val campaignsData = campaigns.collectAsStateWithLifecycle()
        val disabledCampaigns = disabledCampaigns.collectAsStateWithLifecycle()

        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp

        val campaign =
            campaignsData.value.filter { it.campaignType == "BAN" && it.details is BannerDetails }
                .firstOrNull { it.position == position }

        val bannerDetails = when (val details = campaign?.details) {
            is BannerDetails -> details
            else -> null
        }

        if (bannerDetails != null && !disabledCampaigns.value.contains(campaign?.id)) {
            val style = bannerDetails.styling
            val bannerUrl = bannerDetails.image

            val calculatedHeight = if (bannerDetails.width != null && bannerDetails.height != null) {
                val aspectRatio = bannerDetails.height.toFloat() / bannerDetails.width.toFloat()
                val actualWidth = staticWidth ?: screenWidth
                (actualWidth.value * aspectRatio).dp
            } else {
                bannerDetails.height?.dp
            }

            LaunchedEffect(Unit) {
                campaign?.id?.let {
                    trackCampaignActions(it, "IMP")
                    trackEvents(it, "viewed")
                }
            }

            com.appversal.appstorys.ui.PinnedBanner(
                modifier = modifier.clickable {
                    campaign?.id?.let {
                        clickEvent(link = bannerDetails.link, campaignId = it)
                        trackEvents(it, "clicked")
                    }
                },
                imageUrl = bannerUrl ?: "",
                lottieUrl = bannerDetails.lottie_data,
                width = bannerDetails.width?.dp ?: staticWidth,
                exitIcon = style?.isClose ?: false,
                exitUnit = {
                    val ids: ArrayList<String> = ArrayList(_disabledCampaigns.value)
                    campaign?.id?.let {
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
                contentScale = contentScale,
                height = calculatedHeight,
                placeHolder = placeHolder,
                placeholderContent = placeholderContent
            )
        }
    }

    @Composable
    override fun Widget(
        modifier: Modifier,
        contentScale: ContentScale,
        staticWidth: Dp?,
        placeHolder: Drawable?,
        placeholderContent: (@Composable () -> Unit)?,
        position: String?
    ) {
        val campaignsData = campaigns.collectAsStateWithLifecycle()
        val campaign =
            campaignsData.value.filter { it.campaignType == "WID" && it.details is WidgetDetails }
                .firstOrNull { it.position == position }
        val widgetDetails = campaign?.details as? WidgetDetails

        if (widgetDetails != null) {

            if (widgetDetails.type == "full") {

                FullWidget(
                    modifier = modifier,
                    staticWidth = staticWidth,
                    placeHolder = placeHolder,
                    contentScale = contentScale,
                    position = position,
                    placeholderContent = placeholderContent
                )

            } else if (widgetDetails.type == "half") {
                DoubleWidget(
                    modifier = modifier,
                    staticWidth = staticWidth,
                    position = position,
                    placeHolder = placeHolder,
                    placeholderContent = placeholderContent
                )
            }
        }
    }

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

            val calculatedHeight = if (widgetDetails.width != null && widgetDetails.height != null) {
                val aspectRatio = widgetDetails.height.toFloat() / widgetDetails.width.toFloat()
                val actualWidth = staticWidth ?: screenWidth
                (actualWidth.value.minus(32) * aspectRatio).dp
            } else {
                widgetDetails.height?.dp
            }

            LaunchedEffect(pagerState.currentPage, isVisible) {
                if (isVisible) {
                    campaign?.id?.let {
                        trackCampaignActions(
                            it,
                            "IMP",
                            widgetDetails.widgetImages[pagerState.currentPage].id
                        )
                        trackEvents(it, "viewed", mapOf("widget_image" to widgetDetails.widgetImages[pagerState.currentPage].id!!))
                    }
                }
            }

            AutoSlidingCarousel(
                modifier = modifier.onGloballyPositioned { layoutCoordinates ->
                    val visibilityRect = layoutCoordinates.boundsInWindow()
                    val parentHeight = layoutCoordinates.parentLayoutCoordinates?.size?.height ?: 0
                    val widgetHeight = layoutCoordinates.size.height
                    val isAtLeastHalfVisible = visibilityRect.top < parentHeight &&
                            visibilityRect.bottom > 0 &&
                            (visibilityRect.height >= widgetHeight * 0.5f)

                    isVisible = isAtLeastHalfVisible
                },
                pagerState = pagerState,
                itemsCount = widgetDetails.widgetImages.count(),
                width = staticWidth,
                itemContent = { index ->
                    widgetDetails.widgetImages[index].image?.let {
                        CarousalImage(
                            modifier = Modifier.clickable {
                                clickEvent(
                                    link = widgetDetails.widgetImages[index].link,
                                    campaignId = campaign.id,
                                    widgetImageId = widgetDetails.widgetImages[index].id
                                )

                                trackEvents(campaign.id, "clicked", mapOf("widget_image" to widgetDetails.widgetImages[index].id!!))
                            },
                            contentScale = contentScale,
                            imageUrl = widgetDetails.widgetImages[index].image ?: "",
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

            val calculatedHeight = if (widgetDetails.width != null && widgetDetails.height != null) {
                val aspectRatio = widgetDetails.height.toFloat() / widgetDetails.width.toFloat()
                val actualWidth = staticWidth ?: screenWidth
                ((actualWidth.value.minus(48) * aspectRatio).div(2)).dp
            } else {
                (widgetDetails.height?.minus(48))?.div(2)?.dp
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

                        trackEvents(it, "viewed", mapOf("widget_image" to widgetImagesPairs[pagerState.currentPage].first.id!!))

                        trackCampaignActions(
                            it,
                            "IMP",
                            widgetImagesPairs[pagerState.currentPage].second.id
                        )

                        trackEvents(it, "viewed", mapOf("widget_image" to widgetImagesPairs[pagerState.currentPage].second.id!!))
                    }
                }
            }

            DoubleWidgets(
                modifier = modifier.onGloballyPositioned { layoutCoordinates ->
                    val visibilityRect = layoutCoordinates.boundsInWindow()
                    val parentHeight = layoutCoordinates.parentLayoutCoordinates?.size?.height ?: 0
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
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
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

                                            trackEvents(campaign.id, "clicked", mapOf("widget_image" to leftImage.id!!))
                                        }

                                    },
                                imageUrl = leftImage.image,
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

                                            trackEvents(campaign.id, "clicked", mapOf("widget_image" to rightImage.id!!))
                                        }
                                    },
                                imageUrl = rightImage.image,
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

    @Composable
    override fun BottomSheet(
        onDismissRequest: () -> Unit,
    ) {
        val campaignsData = campaigns.collectAsStateWithLifecycle()

        val campaign =
            campaignsData.value.firstOrNull { it.campaignType == "BTS" && it.details is BottomSheetDetails }

        val bottomSheetDetails = when (val details = campaign?.details) {
            is BottomSheetDetails -> details
            else -> null
        }

        if (bottomSheetDetails != null){

            LaunchedEffect(Unit) {
                campaign?.id?.let {
                    trackCampaignActions(it, "IMP")
                    trackEvents(it, "viewed")
                }
            }

            BottomSheetComponent(
                onDismissRequest = onDismissRequest,
                bottomSheetDetails = bottomSheetDetails,
                onClick = { ctaLink ->
                    campaign?.id?.let { campaignId ->
                        clickEvent(link = ctaLink, campaignId = campaignId)
                        trackEvents(campaignId, "clicked")
                    }
                }
            )
        }
    }

    @Composable
    override fun Modals(
        onCloseClick: () -> Unit,
    ) {

        val campaignsData = campaigns.collectAsStateWithLifecycle()

        val campaign =
            campaignsData.value.firstOrNull { it.campaignType == "MOD" && it.details is ModalDetails }

        val modalDetails = when (val details = campaign?.details) {
            is ModalDetails -> details
            else -> null
        }

        if (modalDetails != null){

            LaunchedEffect(Unit) {
                campaign?.id?.let {
                    trackCampaignActions(it, "IMP")
                    trackEvents(it, "viewed")
                }
            }

            PopupModal(
                onCloseClick = onCloseClick,
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
                },
            )
        }
    }

    private fun clickEvent(link: Any?, campaignId: String, widgetImageId: String? = null) {

        if (link != null && link is String) {
            val url = link as String
            if (url.isNotEmpty()) {
                if (!isValidUrl(url)) {
                    navigateToScreen(url)
                } else {
                    openUrl(url)
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