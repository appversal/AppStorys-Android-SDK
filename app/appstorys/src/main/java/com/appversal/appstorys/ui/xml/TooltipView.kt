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
import com.appversal.appstorys.AppStorys
import com.appversal.appstorys.R

class ToolTipWrapperLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var targetKey: String? = null
    private var composed = false
    private var isNavigationBarItem = false

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ToolTipWrapperView,
            0, 0
        ).apply {
            try {
                targetKey = getString(R.styleable.ToolTipWrapperView_targetKey)
                isNavigationBarItem = getBoolean(R.styleable.ToolTipWrapperView_isNavigationBarItem, false)
            } finally {
                recycle()
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        post {
            if (!composed && childCount > 0 && targetKey != null) {
                val originalView = getChildAt(0)
                removeView(originalView)

                val composeView = ComposeView(context).apply {
                    setContent {
                        TooltipWrapperComposable(targetKey!!, originalView)
                    }
                }
                addView(composeView)
                composed = true
            }
        }
    }

    @Composable
    private fun TooltipWrapperComposable(key: String, view: View) {
        if (view.layoutParams == null) {
            view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
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