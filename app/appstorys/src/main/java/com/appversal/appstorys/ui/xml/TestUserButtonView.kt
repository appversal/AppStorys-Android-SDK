package com.appversal.appstorys.ui.xml

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.Keep
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.withStyledAttributes
import com.appversal.appstorys.AppStorys
import com.appversal.appstorys.R

@Keep class TestUserButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var screenName = ""

    init {
        attrs?.let {
            context.withStyledAttributes(it, R.styleable.TestUserButtonView) {
                screenName = getString(R.styleable.TestUserButtonView_screenName) ?: screenName
            }
        }
        if (screenName.isNotBlank()) {
            addView(
                ComposeView(context).apply {
                    setContent {
                        AppStorys.TestUserButton(
                            screenName = screenName
                        )
                    }
                }
            )
        }
    }
}