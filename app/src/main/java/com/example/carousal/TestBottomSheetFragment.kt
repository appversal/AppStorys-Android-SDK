package com.example.carousal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.appversal.appstorys.ui.xml.OverlayLayoutView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TestBottomSheetFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_test_bottom_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // The root OverlayLayoutView (insideBottomSheet=true) needs the Activity
        // so TestUserButton can resolve it for screen capture.
        view.findViewById<OverlayLayoutView>(R.id.bs_overlay)
            .setActivity(requireActivity())

        view.findViewById<android.widget.Button>(R.id.xml_bs_close_btn)
            .setOnClickListener { dismiss() }

        // Tell the SDK which screen this bottom sheet represents,
        // so the backend can return tooltip campaigns for it.
        App.appStorys.getScreenCampaigns("Bottom Sheet XML")
    }
}