package com.appversal.appstorys.ui.xml

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import com.appversal.appstorys.AppStorys
import com.appversal.appstorys.R
import androidx.core.content.withStyledAttributes

class BottomSheetView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var isOpen by mutableStateOf(false)

    init {
        attrs?.let {
            context.withStyledAttributes(it, R.styleable.BottomSheetView) {
                isOpen = getBoolean(R.styleable.BottomSheetView_open, isOpen)
            }
        }
        val composeView = ComposeView(context).apply {
            setContent {
                if (isOpen) {
                    AppStorys.BottomSheet(
                        onDismissRequest = { isOpen = false }
                    )
                }
            }
        }
        addView(composeView)
    }

    fun open() {
        isOpen = true
    }
}