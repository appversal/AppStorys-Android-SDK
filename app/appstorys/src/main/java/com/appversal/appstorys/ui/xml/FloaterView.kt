package com.appversal.appstorys.ui.xml

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.withStyledAttributes
import com.appversal.appstorys.AppStorys
import com.appversal.appstorys.R
import com.appversal.appstorys.utils.pxToDp

class FloaterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var bottomPadding = 0
    private val staticHeight = mutableStateOf(200.dp)
    private val staticWidth: MutableState<Dp?> = mutableStateOf(null)

    init {
        attrs?.let {
            context.withStyledAttributes(it, R.styleable.FloaterView) {
                bottomPadding = getDimensionPixelSize(
                    R.styleable.FloaterView_bottomPadding,
                    0
                )
            }
        }
        addView(
            ComposeView(context).apply {
                setContent {
                    AppStorys.Floater(
                        bottomPadding = bottomPadding.toDp()
                    )
                }
            }
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val heightPx = MeasureSpec.getSize(heightMeasureSpec)
        val widthPx = MeasureSpec.getSize(widthMeasureSpec)

        val newWidth = context.pxToDp(widthPx.toFloat())
        val newHeight = context.pxToDp(heightPx.toFloat())

        if (staticHeight.value != newHeight) {
            staticHeight.value = newHeight
        }

        if (staticWidth.value != newWidth) {
            staticWidth.value = newWidth
        }
    }
}