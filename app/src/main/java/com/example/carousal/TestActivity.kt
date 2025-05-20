package com.example.carousal

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import com.appversal.appstorys.ui.xml.BottomSheetView

//import com.appversal.appstorys.utils.ViewTreeAnalyzer


class TestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        findViewById<Button>(R.id.click_me).setOnClickListener {
            findViewById<BottomSheetView>(R.id.bottom_sheet).open()
        }
    }

    override fun onResume() {
        super.onResume()
        val rootView = findViewById<View>(android.R.id.content)
        rootView.post {
//            ViewTreeAnalyzer.analyzeViewRoot(rootView, "TestActivity")
        }
    }
}