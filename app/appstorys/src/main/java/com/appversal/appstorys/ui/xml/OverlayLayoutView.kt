package com.appversal.appstorys.ui.xml

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.content.withStyledAttributes
import com.appversal.appstorys.AppStorys.tooltipTargetView
import com.appversal.appstorys.R
import com.appversal.appstorys.ui.OverlayContainer

/**
 * `OverlayLayoutView` is a custom `FrameLayout` that integrates Compose content into a traditional
 * Android View hierarchy. It is responsible for rendering overlays such as tooltips and highlights
 * using the `OverlayContainer` composable.
 *
 * This view listens for tooltip target updates and dynamically updates the constraints for the
 * target views.
 *
 * @constructor Creates an instance of `OverlayLayoutView`.
 * @param context The `Context` in which the view is running.
 * @param attrs The attributes of the XML tag that is inflating the view.
 * @param defStyleAttr An attribute in the current theme that contains a reference to a style resource.
 */
class OverlayLayoutView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var overlayComposeView: ComposeView

    init {
        // Initialize ComposeView but delay adding it until onFinishInflate()
        overlayComposeView = ComposeView(context).apply {
            setContent {
                LaunchedEffect(Unit) {
                    tooltipTargetView.collect { target ->
                        handleTargetView(target?.target)
                    }
                }

                // Composable overlay content
                OverlayContainer.Content(
                    topPadding = 0.dp,
                    bottomPadding = 0.dp
                )
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        // Add ComposeView after all XML children to ensure it's on top
        addView(overlayComposeView)

        // Optional: force it to front in case of future dynamic view additions
        overlayComposeView.bringToFront()
    }

    /**
     * Retrieves a `View` by its ID string.
     *
     * @param id The string identifier of the view.
     * @return The `View` object if found, or `null` if not found.
     */
    @SuppressLint("DiscouragedApi")
    private fun getViewId(id: String): View? {
        val viewId = context.resources.getIdentifier(id, "id", context.packageName)
        return when {
            viewId != 0 -> findViewById(viewId)
            else -> {
                Log.e("OverlayLayoutView", "View ID not found: $id")
                null
            }
        }
    }

    /**
     * Handles the target view by adding its constraints to the `OverlayContainer`.
     *
     * @param target The string identifier of the target view.
     */
    private fun handleTargetView(target: String?) {
        if (target == null) {
            Log.e("OverlayLayoutView", "Target view is null")
            return
        }

        val view = getViewId(target)
        if (view == null) {
            Log.e("OverlayLayoutView", "Target view not found: $target")
            return
        }

        // Add the view's constraints to the OverlayContainer.
        OverlayContainer.addViewConstraint(target, view)

        // Listen for layout changes to update the constraints dynamically.
        view.onLayoutChanges {
            OverlayContainer.addViewConstraint(target, view)
        }
    }
}