package com.appversal.appstorys.ui.xml

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.withStyledAttributes
import androidx.core.view.isNotEmpty
import com.appversal.appstorys.AppStorys
import com.appversal.appstorys.R

class TooltipWrapperView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var targetKey: String? = null
    private var composed = false
    private var isNavigationBarItem = false

    init {
        attrs?.let {
            context.withStyledAttributes(it, R.styleable.TooltipWrapperView) {
                targetKey = getString(R.styleable.TooltipWrapperView_targetKey)
                isNavigationBarItem = getBoolean(
                    R.styleable.TooltipWrapperView_isNavigationBarItem,
                    isNavigationBarItem
                )
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        post {
            if (!composed && isNotEmpty() && targetKey != null) {
                val originalView = getChildAt(0)
                removeView(originalView)

                addView(
                    ComposeView(context).apply {
                        setContent {
                            TooltipWrapperComposable(targetKey!!, originalView)
                        }
                    }
                )
                composed = true
            }
        }
    }

    @Composable
    private fun TooltipWrapperComposable(key: String, view: View) {
        if (view.layoutParams == null) {
            view.layoutParams = ViewGroup.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
        }

        AppStorys.ToolTipWrapper(
            targetModifier = Modifier,
            targetKey = key,
            isNavigationBarItem = isNavigationBarItem
        ) { modifier ->
            AndroidView(
                modifier = modifier,
                factory = { context ->
                    // We need to remove the view from any previous parent
                    (view.parent as? ViewGroup)?.removeView(view)

                    // Create a container to hold the view and allow proper layout
                    FrameLayout(context).apply {
                        layoutParams = view.layoutParams
                        addView(view)
                    }
                },
                update = { layout ->
                    layout.requestLayout()
                }
            )
        }
    }
}