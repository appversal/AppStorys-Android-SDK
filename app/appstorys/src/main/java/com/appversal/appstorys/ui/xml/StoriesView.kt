package com.appversal.appstorys.ui.xml

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import com.appversal.appstorys.AppStorys

class StoriesView  @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        val composeView = ComposeView(context).apply {
            setContent {
                AppStorys.Stories()
            }
        }
        addView(composeView)
    }
}