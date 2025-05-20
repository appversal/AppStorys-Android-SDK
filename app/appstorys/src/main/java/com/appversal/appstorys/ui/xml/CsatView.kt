package com.appversal.appstorys.ui.xml

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import com.appversal.appstorys.AppStorys

class CsatView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {

        val composeView = ComposeView(context).apply {
            setContent {
                AppStorys.CSAT(
                    modifier = Modifier,
                    displayDelaySeconds = 0,
                    position =null,
                )
            }
        }
        addView(composeView)
    }
}