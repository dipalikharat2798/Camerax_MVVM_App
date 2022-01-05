package com.androdevdk.camerax_mvvm_app.view.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.androdevdk.camerax_mvvm_app.R
import com.androdevdk.camerax_mvvm_app.util.FragmentPermissionHelper
import com.androdevdk.camerax_mvvm_app.util.FragmentPermissionInterface

private const val PERMISSIONS_REQUEST_CODE = 10
private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)

@Suppress("DEPRECATION")
class PermissionsFragment : Fragment() {
    lateinit var fr: FragmentActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fr = context as FragmentActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FragmentPermissionHelper().startPermissionRequest(
            fr,
            object : FragmentPermissionInterface {
                override fun onGranted(isgranted: Boolean) {
                    if (isgranted) {
                        navigateToCamera()
                    } else {
                        // Explain to the user that the feature is unavailable because the
                        // features requires a permission that the user has denied. At the
                        // same time, respect the user's decision. Don't link to system
                        // settings in an effort to convince the user to change their
                        // decision.
                    }
                }
            },
            Manifest.permission.CAMERA
        );
        /*  if (!hasPermissions(requireContext())) {
              // Request camera-related permissions
              requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
          } else {
              // If permissions have already been granted, proceed
              navigateToCamera()
          }
         */
    }

    /* override fun onRequestPermissionsResult(
         requestCode: Int, permissions: Array<String>, grantResults: IntArray
     ) {
         super.onRequestPermissionsResult(requestCode, permissions, grantResults)
         if (requestCode == PERMISSIONS_REQUEST_CODE) {
             if (PackageManager.PERMISSION_GRANTED == grantResults.firstOrNull()) {
                 // Take the user to the success fragment when permission is granted
                 Toast.makeText(context, "Permission request granted", Toast.LENGTH_LONG).show()
                 navigateToCamera()
             } else {
                 Toast.makeText(context, "Permission request denied", Toast.LENGTH_LONG).show()
             }
         }
     }
 */
    private fun navigateToCamera() {
        var fragment = CameraFragment()
        var fragmentManager = requireActivity().supportFragmentManager
        var fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.camera_frag, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    companion object {
        /** Convenience method used to check if all permissions required by this app are granted */
        fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}