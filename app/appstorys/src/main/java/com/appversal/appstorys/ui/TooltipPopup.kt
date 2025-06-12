package com.appversal.appstorys.ui

import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlin.math.roundToInt
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.appversal.appstorys.AppStorys.showcaseVisible
import com.appversal.appstorys.AppStorys.tooltipTargetView
import com.appversal.appstorys.AppStorys.viewsCoordinates
import com.appversal.appstorys.api.Tooltip
import com.appversal.appstorys.ui.OverlayContainer.toAppStorysCoordinates
import com.appversal.appstorys.utils.AppStorysCoordinates
import com.appversal.appstorys.utils.toColor


@Composable
internal fun TooltipContent(tooltip: Tooltip, onClick: () -> Unit, exitUnit: () -> Unit){
    val context = LocalContext.current

    val imageRequest = ImageRequest.Builder(context)
        .data(tooltip.url)
        .memoryCacheKey(tooltip.url)
        .diskCacheKey(tooltip.url)
        .diskCachePolicy(CachePolicy.ENABLED)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .crossfade(true)
        .build()

    Box(modifier = Modifier.then(
        tooltip.styling?.spacing?.padding?.let { padding ->
            Modifier.padding(
                start = padding.paddingLeft?.dp ?: 0.dp,
                end = padding.paddingRight?.dp ?: 0.dp,
                top = padding.paddingTop?.dp ?: 0.dp,
                bottom = padding.paddingBottom?.dp ?: 0.dp
            )
        } ?: Modifier
    )){
        AsyncImage(
            model = imageRequest,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize().clip(
                if (tooltip.styling?.tooltipDimensions?.cornerRadius != null) RoundedCornerShape(
                    tooltip.styling.tooltipDimensions.cornerRadius.toIntOrNull()?.dp ?: 12.dp) else MaterialTheme.shapes.medium
            ).clickable {
                onClick()
            }
        )
        if (tooltip.styling?.closeButton == true) {
            Icon(
                modifier = Modifier
                    .padding(15.dp)
                    .size(30.dp)
                    .align(Alignment.TopEnd)
                    .clickable {
                        exitUnit()
                    },
                tint = Color.White,
                imageVector = Icons.Filled.Close,
                contentDescription = ""
            )
        }
    }
}

@Composable
internal fun TooltipPopup(
    modifier: Modifier = Modifier,
    isShowTooltip: Boolean,
    tooltip: Tooltip?,
    position: TooltipPopupPosition,
    backgroundShape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = Color.White,
    onDismissRequest: (() -> Unit),
    isNavigationBarItem: Boolean,
    requesterView: @Composable (Modifier) -> Unit,
    tooltipContent: @Composable () -> Unit,
) {

    if (isShowTooltip){
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
            onDismissRequest = onDismissRequest
        ) {
            val coordinates by viewsCoordinates.collectAsStateWithLifecycle()
            val visibleShowcase by showcaseVisible.collectAsStateWithLifecycle()
            val currentToolTipTarget by tooltipTargetView.collectAsStateWithLifecycle()

            coordinates[currentToolTipTarget?.target]?.let {
                if (it.isAttached) {
                    ShowcaseView(
                        visible = visibleShowcase,
                        targetCoordinates = it.toAppStorysCoordinates(),
                        highlight = ShowcaseHighlight.Rectangular(
                            cornerRadius = currentToolTipTarget?.styling?.highlightRadius?.toIntOrNull()?.dp ?: 8.dp,
                            padding = currentToolTipTarget?.styling?.highlightPadding?.toIntOrNull()?.dp ?: 8.dp
                        )
                    )
                }
            }
        }

        if (!isNavigationBarItem){
            Box(modifier = modifier){
                TooltipPopup(
                    backgroundShape = backgroundShape,
                    backgroundColor = backgroundColor,
                    onDismissRequest = onDismissRequest,
                    tooltip = tooltip,
                    position = position,
                ) {
                    tooltipContent()
                }
            }
        }else{
            TooltipPopup(
                backgroundShape = backgroundShape,
                backgroundColor = backgroundColor,
                onDismissRequest = onDismissRequest,
                tooltip = tooltip,
                position = position,
            ) {
                tooltipContent()
            }
        }
    }
    requesterView(modifier)
}

@Composable
internal fun TooltipPopup(
    position: TooltipPopupPosition,
    tooltip: Tooltip?,
    backgroundShape: Shape,
    backgroundColor: Color,
    arrowHeight: Dp = 8.dp,
    arrowWidth: Dp = 8.dp,
    horizontalPadding: Dp = 8.dp,
    onDismissRequest: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    var alignment = Alignment.TopCenter
    var offset = position.offset

    val horizontalPaddingInPx = with(LocalDensity.current) {
        horizontalPadding.toPx()
    }

    var arrowPositionX by remember { mutableFloatStateOf(position.centerPositionX) }

//    Log.e("TooltipPopup", "Position: $position, Offset: $offset, Alignment: $alignment, arrowPositionX: $arrowPositionX")

    with(LocalDensity.current) {
        val arrowPaddingPx = arrowHeight.toPx().roundToInt() * 2

        when (position.alignment) {
            TooltipAlignment.TopCenter -> {
                alignment = Alignment.TopCenter
                offset = offset.copy(
                    y = position.offset.y + arrowPaddingPx
                )
            }
            TooltipAlignment.BottomCenter -> {
                alignment = Alignment.BottomCenter
                offset = offset.copy(
                    y = position.offset.y - arrowPaddingPx
                )
            }
        }
    }

    val popupPositionProvider = remember(alignment, offset) {
        TooltipAlignmentOffsetPositionProvider(
            alignment = alignment,
            offset = offset,
            horizontalPaddingInPx = horizontalPaddingInPx,
            centerPositionX = position.centerPositionX,
        ) { position ->
            arrowPositionX = position
        }
    }

    Popup(
        popupPositionProvider = popupPositionProvider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(dismissOnBackPress = false),
    ) {
        Box(
//            modifier = Modifier.border(color = Color.Green, width = 1.dp)
        ) {
            BubbleLayout(
                modifier = Modifier
                    .padding(vertical = maxOf((tooltip?.styling?.highlightPadding?.toIntOrNull()?.dp ?: 0.dp), (tooltip?.styling?.tooltipArrow?.arrowHeight?.toIntOrNull()?.dp ?: 0.dp)))
                    .then(
                        Modifier.height(
                            tooltip?.styling?.tooltipDimensions?.height?.toIntOrNull()?.dp ?: 200.dp
                        )
                    )
                    .then(
                        Modifier.width(
                            tooltip?.styling?.tooltipDimensions?.width?.toIntOrNull()?.dp ?: 300.dp
                        )
                    )
                    .padding(horizontal = horizontalPadding)
                    .background(
                        color = tooltip?.styling?.backgroudColor.toColor(backgroundColor) ,
                        shape = if (tooltip!!.styling?.tooltipDimensions?.cornerRadius != null) RoundedCornerShape(
                            tooltip.styling.tooltipDimensions.cornerRadius.toIntOrNull()?.dp ?: 12.dp) else backgroundShape,
                    ),
                alignment = position.alignment,
                arrowHeight = tooltip.styling?.tooltipArrow?.arrowHeight?.toIntOrNull()?.dp ?: arrowHeight,
                arrowWidth =  tooltip.styling?.tooltipArrow?.arrowWidth?.toIntOrNull()?.dp ?: arrowWidth,
                backgroundColor = backgroundColor,
                arrowPositionX = arrowPositionX,
            ) {
                content()
            }
        }
    }
}

@Composable
internal fun BubbleLayout(
    modifier: Modifier = Modifier,
    alignment: TooltipAlignment = TooltipAlignment.TopCenter,
    backgroundColor: Color,
    arrowHeight: Dp,
    arrowWidth: Dp,
    arrowPositionX: Float,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val arrowHeightPx = with(density) { arrowHeight.toPx() }
    val arrowWidthPx = with(density) { arrowWidth.toPx() }

    Box(
        modifier = modifier
            .drawBehind {
                if (arrowPositionX <= 0f) return@drawBehind

                val isTopCenter = alignment == TooltipAlignment.TopCenter
                val path = Path()

                if (isTopCenter) {
                    val position = Offset(arrowPositionX, 0f)
                    path.apply {
                        moveTo(position.x, position.y)
                        lineTo(position.x - arrowWidthPx / 2, position.y)
                        lineTo(position.x, position.y - arrowHeightPx)
                        lineTo(position.x + arrowWidthPx / 2, position.y)
                        close()
                    }
                } else {
                    val arrowY = drawContext.size.height
                    val position = Offset(arrowPositionX, arrowY)
                    path.apply {
                        moveTo(position.x, position.y)
                        lineTo(position.x + arrowWidthPx / 2, position.y)
                        lineTo(position.x, position.y + arrowHeightPx)
                        lineTo(position.x - arrowWidthPx / 2, position.y)
                        close()
                    }
                }

                drawPath(
                    path = path,
                    color = Color.White,
                )
                path.close()
            }
    ) {
        content()
    }
}

internal data class TooltipPopupPosition(
    val offset: IntOffset = IntOffset(0, 0),
    val alignment: TooltipAlignment = TooltipAlignment.TopCenter,
    val centerPositionX: Float = 0f,
)

internal fun calculateTooltipPopupPosition(
    rootView: View,
    coordinates: AppStorysCoordinates,
    tooltip: Tooltip?,
    inBottomNavigation: Boolean,
): TooltipPopupPosition {
    val visibleWindowBounds = Rect()
    rootView.getWindowVisibleDisplayFrame(visibleWindowBounds)

    val boundsInWindow = coordinates.boundsInWindow()

    val heightAbove = boundsInWindow.top - visibleWindowBounds.top
    val heightBelow = visibleWindowBounds.bottom - visibleWindowBounds.top - boundsInWindow.bottom

    val centerPositionX = boundsInWindow.right - (boundsInWindow.right - boundsInWindow.left) / 2

    val offsetX = centerPositionX.roundToInt() - visibleWindowBounds.centerX()

    return if (heightAbove < heightBelow) {
        val offset = IntOffset(
            y = if ((tooltip?.styling?.tooltipArrow?.arrowHeight?.toIntOrNull()?.dp?.coerceAtLeast(0.dp) ?: 0.dp) < 8.dp) coordinates.height - 30 else coordinates.height,
            x = offsetX
        )
        TooltipPopupPosition(
            offset = offset,
            alignment = TooltipAlignment.TopCenter,
            centerPositionX = centerPositionX,
        )
    } else {
        TooltipPopupPosition(
            offset = IntOffset(
//                y =if ((tooltip?.styling?.tooltipArrow?.arrowHeight?.toIntOrNull()?.dp?.coerceAtLeast(0.dp) ?: 0.dp) < 8.dp) -coordinates.height + ( if (inBottomNavigation) 30 else 60) else -coordinates.height,
                y = 0,
                x = offsetX
            ),
            alignment = TooltipAlignment.BottomCenter,
            centerPositionX = centerPositionX,
        )
    }
}

internal enum class TooltipAlignment {
    BottomCenter,
    TopCenter,
}

internal class TooltipAlignmentOffsetPositionProvider(
    val alignment: Alignment,
    val offset: IntOffset,
    val centerPositionX: Float,
    val horizontalPaddingInPx: Float,
    private val onArrowPositionX: (Float) -> Unit,
) : PopupPositionProvider {

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        var popupPosition = IntOffset(0, 0)

        val parentAlignmentPoint = alignment.align(
            IntSize.Zero,
            IntSize(anchorBounds.width, anchorBounds.height),
            layoutDirection
        )
        val relativePopupPos = alignment.align(
            IntSize.Zero,
            IntSize(popupContentSize.width, popupContentSize.height),
            layoutDirection
        )

        popupPosition += IntOffset(anchorBounds.left, anchorBounds.top)

        popupPosition += parentAlignmentPoint

        popupPosition -= IntOffset(relativePopupPos.x, relativePopupPos.y)

        val resolvedOffset = IntOffset(
            offset.x * (if (layoutDirection == LayoutDirection.Ltr) 1 else -1),
            offset.y
        )

        popupPosition += resolvedOffset

//        Log.e("TooltipPopup", "Popup Position: $popupPosition, Popup Content Size: $popupContentSize, alignment: $alignment, anchorBounds: $anchorBounds, offset: $offset")

        val leftSpace = centerPositionX - horizontalPaddingInPx
        val rightSpace = windowSize.width - centerPositionX - horizontalPaddingInPx

        val tooltipWidth = popupContentSize.width
        val halfPopupContentSize = popupContentSize.center.x

        val fullPadding = horizontalPaddingInPx * 2

        val maxTooltipSize = windowSize.width - fullPadding

        val isCentralPositionTooltip = halfPopupContentSize <= leftSpace && halfPopupContentSize <= rightSpace

        when {
            isCentralPositionTooltip -> {
                popupPosition = IntOffset(centerPositionX.toInt() - halfPopupContentSize, popupPosition.y)
                val arrowPosition = halfPopupContentSize.toFloat() - horizontalPaddingInPx
                onArrowPositionX.invoke(arrowPosition)
            }
            tooltipWidth >= maxTooltipSize -> {
                popupPosition = IntOffset(windowSize.center.x - halfPopupContentSize, popupPosition.y)
                val arrowPosition = centerPositionX - popupPosition.x - horizontalPaddingInPx
                onArrowPositionX.invoke(arrowPosition)
            }
            halfPopupContentSize > rightSpace -> {
                popupPosition = IntOffset(centerPositionX.toInt(), popupPosition.y)
                val arrowPosition = halfPopupContentSize + (halfPopupContentSize - rightSpace) - fullPadding

                onArrowPositionX.invoke(arrowPosition)
            }
            halfPopupContentSize > leftSpace -> {
                popupPosition = IntOffset(0, popupPosition.y)
                val arrowPosition = centerPositionX - horizontalPaddingInPx
                onArrowPositionX.invoke(arrowPosition)
            }
            else -> {
                val position = centerPositionX
                onArrowPositionX.invoke(position)
            }
        }
        return popupPosition
    }
}
