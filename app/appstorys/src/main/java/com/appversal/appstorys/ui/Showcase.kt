package com.appversal.appstorys.ui

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

internal class ShowcaseDuration(val enterMillis: Int, val exitMillis: Int) {
    companion object {
        val Default = ShowcaseDuration(700, 700)
    }
}

internal sealed interface ShowcaseDisplayState {
    data object Appeared : ShowcaseDisplayState
    data object Disappeared : ShowcaseDisplayState
}

class HighlightProperties internal constructor(
    val drawHighlight: DrawScope.(LayoutCoordinates) -> Unit,
    val highlightBounds: Rect
)

@Composable
internal fun ShowcaseView(
    visible: Boolean,
    targetCoordinates: LayoutCoordinates,
    duration: ShowcaseDuration = ShowcaseDuration.Default,
    onDisplayStateChanged: (ShowcaseDisplayState) -> Unit = {},
    highlight: ShowcaseHighlight = ShowcaseHighlight.Rectangular()
) {
    val transition =  remember { MutableTransitionState(false) }
    val highlightDrawer = highlight.create(targetCoordinates = targetCoordinates)

    AnimatedVisibility(
        visibleState = transition,
        enter = fadeIn(tween(duration.enterMillis)),
        exit = fadeOut(tween(duration.exitMillis))
    ) {
        Box {
            ShowcaseBackground(
                coordinates = targetCoordinates,
                drawHighlight = highlightDrawer.drawHighlight
            )

        }
    }
    LaunchedEffect(key1 = visible) {
        transition.targetState = visible
    }
    LaunchedEffect(key1 = transition.isIdle) {
        if (transition.isIdle) {
            if (transition.targetState) {
                onDisplayStateChanged(ShowcaseDisplayState.Appeared)
            } else {
                onDisplayStateChanged(ShowcaseDisplayState.Disappeared)
            }
        }
    }
}

@Composable
private fun ShowcaseBackground(
    coordinates: LayoutCoordinates,
    drawHighlight: DrawScope.(LayoutCoordinates) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent()
                    }
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = 0.9f)
        ) {
            drawRect(
                Color.Black.copy(alpha = 0.9f),
                size = Size(size.width, size.height)
            )
            drawHighlight(coordinates)
        }
    }
}


sealed interface ShowcaseHighlight {
    @Composable
    fun create(targetCoordinates: LayoutCoordinates): HighlightProperties

    data class Rectangular(val cornerRadius: Dp = 8.dp, val padding: Dp = 8.dp) :
        ShowcaseHighlight {

        @Composable
        override fun create(targetCoordinates: LayoutCoordinates): HighlightProperties {
            val highlightBounds = createHighlightBounds(
                targetCoordinates.boundsInWindow(),
                with(LocalDensity.current) { padding.toPx() },
            )
            return HighlightProperties(
                drawHighlight = { rectangularHighlight(cornerRadius.toPx(), highlightBounds) },
                highlightBounds = highlightBounds
            )
        }

        private fun DrawScope.rectangularHighlight(
            cornerRadius: Float,
            highlightBounds: Rect
        ) {
            drawRoundRect(
                color = Color.White,
                size = highlightBounds.size,
                topLeft = highlightBounds.topLeft,
                blendMode = BlendMode.Clear,
                cornerRadius = CornerRadius(cornerRadius, cornerRadius)
            )
        }

        @Composable
        private fun createHighlightBounds(
            targetRect: Rect,
            targetMargin: Float,
        ): Rect {
            val rectangle = android.graphics.Rect()
            LocalView.current.getWindowVisibleDisplayFrame(rectangle)
            val shift = rectangle.top

            return Rect(
                top = targetRect.top - (targetMargin + shift),
                bottom = (targetRect.bottom - shift) + (targetMargin),
                left = targetRect.left - targetMargin,
                right = targetRect.right + targetMargin
            )
        }
    }

    data class Circular(val targetMargin: Dp = 4.dp) : ShowcaseHighlight {

        @Composable
        override fun create(targetCoordinates: LayoutCoordinates): HighlightProperties {
            val targetMargin = with(LocalDensity.current) { targetMargin.toPx() }
            return HighlightProperties(
                drawHighlight = { circularHighlight(it, targetMargin) },
                highlightBounds = createHighlightBounds(
                    targetCoordinates.boundsInRoot(),
                    targetMargin = targetMargin
                )
            )
        }

        private fun DrawScope.circularHighlight(
            coordinates: LayoutCoordinates,
            targetMargin: Float
        ) {
            val targetRect = coordinates.boundsInRoot()
            val xOffset = targetRect.topLeft.x
            val yOffset = targetRect.topLeft.y
            val rectSize = coordinates.boundsInParent().size
            val radius = if (rectSize.width > rectSize.height) {
                rectSize.width / 2
            } else {
                rectSize.height / 2
            }
            drawCircle(
                color = Color.White,
                radius = radius + targetMargin,
                center = Offset(xOffset + rectSize.width / 2, yOffset + rectSize.height / 2),
                blendMode = BlendMode.Clear
            )
        }

        private fun createHighlightBounds(targetRect: Rect, targetMargin: Float): Rect {
            val radius = if (targetRect.width > targetRect.height) {
                targetRect.width / 2
            } else {
                targetRect.height / 2
            }

            return Rect(
                top = targetRect.center.y - radius - targetMargin,
                bottom = targetRect.center.y + radius + targetMargin,
                left = targetRect.center.x - radius - targetMargin,
                right = targetRect.center.x + radius + targetMargin
            )
        }
    }
}