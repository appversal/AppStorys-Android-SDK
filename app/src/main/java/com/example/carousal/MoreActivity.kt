package com.example.carousal

import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.appversal.appstorys.ui.xml.BottomSheetView
import com.appversal.appstorys.ui.xml.OverlayLayoutView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MoreActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more)
    }

    override fun onResume() {
        super.onResume()
        App.appStorys.getScreenCampaigns(
            "More Screen",
            emptyList()
        )
    }
}