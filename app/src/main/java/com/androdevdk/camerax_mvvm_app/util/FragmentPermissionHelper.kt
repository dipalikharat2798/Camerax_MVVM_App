package com.androdevdk.camerax_mvvm_app.util

import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity

public class FragmentPermissionHelper {
    public fun startPermissionRequest(
        fr: FragmentActivity,
        fs: FragmentPermissionInterface,
        manifest: String
    ) {
        val requestPermissionLauncher =
            fr.registerForActivityResult(ActivityResultContracts.RequestPermission(), fs::onGranted)
        requestPermissionLauncher.launch(
            manifest
        )
    }
}