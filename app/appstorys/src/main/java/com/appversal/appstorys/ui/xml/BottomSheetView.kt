package com.appversal.appstorys.ui.xml

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.ComposeView
import com.appversal.appstorys.AppStorys

@RequiresApi(Build.VERSION_CODES.N)
@Keep class BottomSheetView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        addView(
            ComposeView(context).apply {
                setContent {
                        AppStorys.BottomSheet()
                }
            }
        )
    }
}