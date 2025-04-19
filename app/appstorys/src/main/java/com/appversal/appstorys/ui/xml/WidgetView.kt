package com.appversal.appstorys.ui.xml

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.appversal.appstorys.AppStorys
import com.appversal.appstorys.R
import com.appversal.appstorys.utils.pxToDp

class WidgetView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var position: String? = null
    private val staticHeight = mutableStateOf(200.dp)
    private val staticWidth : MutableState<Dp?> = mutableStateOf(null)
    private var placeHolder: Drawable? = null
    private var contentScale: ContentScale = ContentScale.FillWidth

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.PinnedBannerView)
            position = typedArray.getString(R.styleable.PinnedBannerView_position)
            placeHolder = typedArray.getDrawable(R.styleable.PinnedBannerView_placeHolder)
            typedArray.recycle()
        }

        val composeView = ComposeView(context).apply {
            setContent {
                AppStorys.Widget(
                    modifier = Modifier,
                    contentScale = contentScale,
                    staticWidth = staticWidth.value,
                    placeHolder = placeHolder,
                    position = position,
                    placeholderContent = null
                )
            }
        }
        addView(composeView)
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

