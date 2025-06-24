package com.appversal.appstorys.ui.xml

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.Keep
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.content.withStyledAttributes
import com.appversal.appstorys.AppStorys
import com.appversal.appstorys.R

@Keep class PinnedBannerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var composed = false
    private var placeholder: Drawable? = null
    private var bottomPadding = 0

    init {
        attrs?.let {
            context.withStyledAttributes(it, R.styleable.PinnedBannerView) {
                placeholder = getDrawable(R.styleable.PinnedBannerView_placeholder)
                bottomPadding = getDimensionPixelSize(R.styleable.PinnedBannerView_bottomPadding, 0)
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (composed) {
            return
        }
        placeholderContent { content ->
            addView(
                ComposeView(context).apply {
                    setContent {
                        AppStorys.PinnedBanner(
                            placeholder = placeholder,
                            placeholderContent = content,
                            bottomPadding = bottomPadding.toDp()
                        )
                    }
                }
            )
            composed = true
        }
    }
}
