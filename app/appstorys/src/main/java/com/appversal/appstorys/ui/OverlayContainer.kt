package com.appversal.appstorys.ui

import android.graphics.Rect
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.appversal.appstorys.AppStorys.CurrentTooltipContent
import com.appversal.appstorys.AppStorys.dismissTooltip
import com.appversal.appstorys.AppStorys.tooltipTargetView
import com.appversal.appstorys.AppStorysAPI
import com.appversal.appstorys.api.Tooltip
import com.appversal.appstorys.utils.AppStorysCoordinates
import kotlin.math.roundToInt

/**
 * `OverlayContainer` is responsible for managing and rendering overlays such as tooltips
 * and highlights in the application. It maintains constraints and tooltips, and provides
 * utility functions to add constraints and render content.
 */
object OverlayContainer {

    // Stores the mapping of target IDs to their corresponding coordinates.
    private val constraints = mutableStateMapOf<String, AppStorysCoordinates>()

    // Stores the list of active tooltips.
    private val tooltips = mutableStateListOf<Tooltip>()

    /**
     * Adds a constraint for a target using its `LayoutCoordinates`.
     *
     * @param id The unique identifier for the target.
     * @param coordinates The `LayoutCoordinates` of the target.
     */
    fun addConstraint(id: String, coordinates: LayoutCoordinates) {
        constraints[id] = coordinates.toAppStorysCoordinates()
    }

    /**
     * Adds a constraint for a target using its `View` object.
     *
     * @param id The unique identifier for the target.
     * @param view The `View` object representing the target.
     */
    fun addViewConstraint(id: String, view: View) {
        constraints[id] = view.toAppStorysCoordinates()
    }

    /**
     * Adds a tooltip to the list of active tooltips.
     *
     * @param tooltip The `Tooltip` object to be added.
     */
    fun addTooltip(tooltip: Tooltip) {
        if (tooltips.any { it.id == tooltip.id }) {
            return // Tooltip with this ID already exists
        }
        tooltips.add(tooltip)
    }

    /**
     * A composable function that renders overlays based on the provided constraints.
     * This function should be placed at the root of your composable hierarchy above any other content.
     *
     * @param modifier The `Modifier` to be applied to the overlay container.
     */
    @RequiresApi(Build.VERSION_CODES.N)
    @Composable
    fun Content(modifier: Modifier = Modifier, topPadding: Dp, bottomPadding: Dp) {

        val campaignManager = AppStorysAPI.getInstance()

        // Collects the target view for tooltips and updates the tooltip list.
        LaunchedEffect(Unit) {
            tooltipTargetView.collect { target ->
                when (target) {
                    null -> tooltips.clear()
                    else -> addTooltip(target)
                }
            }
        }

        Box(
            modifier = modifier.fillMaxSize(),
            content = {

                campaignManager.PinnedBanner(
                    bottomPadding = bottomPadding
                )

                campaignManager.Floater(
                    bottomPadding = bottomPadding
                )

                campaignManager.Pip(
                    bottomPadding = bottomPadding,
                    topPadding = topPadding,
                )

                campaignManager.CSAT(
                    bottomPadding = bottomPadding
                )

                campaignManager.BottomSheet()

                campaignManager.TestUserButton()

                campaignManager.Survey()

                val visibleTooltips by remember {
                    derivedStateOf {
                        tooltips.filter { constraints.containsKey(it.target) }
                    }
                }
                visibleTooltips.forEach { tooltip ->
                    val coordinates by rememberUpdatedState(constraints[tooltip.target])
                    if (coordinates != null) {
                        // Renders the tooltip popup.
                        Popup(
                            popupPositionProvider = object : PopupPositionProvider {
                                override fun calculatePosition(
                                    anchorBounds: IntRect,
                                    windowSize: IntSize,
                                    layoutDirection: LayoutDirection,
                                    popupContentSize: IntSize
                                ): IntOffset = IntOffset(0, 0)
                            },
                            properties = PopupProperties(
                                focusable = true,
                                dismissOnBackPress = true,
                                dismissOnClickOutside = true
                            ),
                            onDismissRequest = ::dismissTooltip,
                            content = {
                                ShowcaseView(
                                    visible = true,
                                    targetCoordinates = coordinates!!,
                                    highlight = ShowcaseHighlight.Rectangular(
                                        cornerRadius = tooltip.styling?.highlightRadius?.toIntOrNull()?.dp
                                            ?: 8.dp,
                                        padding = tooltip.styling?.highlightPadding?.toIntOrNull()?.dp
                                            ?: 8.dp
                                    )
                                )
                            }
                        )

                        // Renders the tooltip content.
                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        x = coordinates!!.x.roundToInt(),
                                        y = coordinates!!.y.roundToInt()
                                    )
                                }
                        ) {
                            val view = LocalView.current.rootView
                            TooltipPopup(
                                backgroundShape = MaterialTheme.shapes.medium,
                                backgroundColor = Color.Transparent,
                                onDismissRequest = ::dismissTooltip,
                                tooltip = tooltip,
                                position = remember(tooltip, coordinates) {
                                    calculateTooltipPopupPosition(
                                        view,
                                        coordinates!!,
                                        tooltip,
                                        true
                                    )
                                },
                            ) {
                                CurrentTooltipContent(tooltip)
                            }
                        }
                    }
                }

                campaignManager.Modals()
            }
        )
    }

    /**
     * Converts `LayoutCoordinates` to `AppStorysCoordinates`.
     *
     * @return The converted `AppStorysCoordinates`.
     */
    fun LayoutCoordinates.toAppStorysCoordinates(): AppStorysCoordinates {
        return AppStorysCoordinates(
            x = positionInRoot().x,
            y = positionInRoot().y,
            width = size.width,
            height = size.height,
            boundsInParent = { boundsInParent() },
            boundsInRoot = { boundsInRoot() },
            boundsInWindow = { boundsInWindow() }
        )
    }

    /**
     * Converts a `View` to `AppStorysCoordinates`.
     *
     * This method calculates position values similar to the `positionInRoot()` approach
     * used in Compose's LayoutCoordinates to ensure consistent positioning between
     * XML views and Compose UI elements.
     *
     * @return The converted `AppStorysCoordinates`.
     */
    fun View.toAppStorysCoordinates(): AppStorysCoordinates {
        // Get location in window (similar to Compose's coordinates system)
        val locationInWindow = IntArray(2)
        getLocationInWindow(locationInWindow)

        // Get status bar height to adjust for proper positioning
        val rootRect = Rect()
        getWindowVisibleDisplayFrame(rootRect)
        val statusBarHeight = rootRect.top

        // Calculate position relative to root, adjusting for status bar
        val rootX = locationInWindow[0].toFloat()
        val rootY = (locationInWindow[1] - statusBarHeight).toFloat()

        return AppStorysCoordinates(
            // TODO: Find out why I need to subtract 55 from rootX and rootY
            x = rootX - 55,
            y = rootY - 55,
            width = width,
            height = height,
            boundsInParent = {
                androidx.compose.ui.geometry.Rect(
                    left = left.toFloat(),
                    top = top.toFloat(),
                    right = right.toFloat(),
                    bottom = bottom.toFloat()
                )
            },
            boundsInRoot = {
                androidx.compose.ui.geometry.Rect(
                    left = rootX,
                    top = rootY,
                    right = rootX + width,
                    bottom = rootY + height
                )
            },
            boundsInWindow = {
                val locationOnScreen = IntArray(2)
                getLocationOnScreen(locationOnScreen)
                androidx.compose.ui.geometry.Rect(
                    left = locationOnScreen[0].toFloat(),
                    top = locationOnScreen[1].toFloat(),
                    right = (locationOnScreen[0] + width).toFloat(),
                    bottom = (locationOnScreen[1] + height).toFloat()
                )
            }
        )
    }
}