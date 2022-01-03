package com.androdevdk.camerax_mvvm_app.view.activities

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.androdevdk.camerax_mvvm_app.ApplicationClass
import com.androdevdk.camerax_mvvm_app.R
import com.androdevdk.camerax_mvvm_app.adapter.CameraPhotoListAdapter
import com.androdevdk.camerax_mvvm_app.databinding.ActivityCameraBinding
import com.androdevdk.camerax_mvvm_app.view.fragments.CameraFragment
import com.androdevdk.camerax_mvvm_app.view.fragments.GalleryFragment
import com.androdevdk.camerax_mvvm_app.view.fragments.PhotoListFragment
import java.io.File

class CameraActivity : AppCompatActivity(), CameraPhotoListAdapter.ImageItemClicked,
    CameraFragment.CameraSendData, GalleryFragment.GalleryFragmentInterface {
    private lateinit var activityCameraBinding: ActivityCameraBinding
    public lateinit var cameraFragment: CameraFragment
    public lateinit var photoListFragment: PhotoListFragment
    public lateinit var galleryFragment: GalleryFragment
    private var startState: Int = 0
    lateinit var applicationClass: ApplicationClass
    lateinit var fragmentManager: FragmentManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityCameraBinding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(activityCameraBinding.root)

        applicationClass = ApplicationClass.instance
        fragmentManager = this.supportFragmentManager
        cameraFragment = fragmentManager.findFragmentById(R.id.camera_frag) as CameraFragment
        photoListFragment =
            fragmentManager.findFragmentById(R.id.photolist_frag) as PhotoListFragment
        galleryFragment = fragmentManager.findFragmentById(R.id.gallery_frag) as GalleryFragment

        fragmentManager.beginTransaction()
            .show(cameraFragment)
            .hide(galleryFragment)
            .show(photoListFragment)
            .commit()
    }

    public fun showFrag() {
        fragmentManager.beginTransaction()
            .show(cameraFragment)
            .hide(galleryFragment)
            .show(photoListFragment)
            .commit()
    }

    @Suppress("DEPRECATION")
    companion object {
        /** Use external media if it is available, our app's file directory otherwise */
        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
            }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }
    }

    override fun onImageItemClicked(index: Int, captureStatus: Boolean) {

        if (captureStatus.equals(true)) {
            galleryFragment.setstartImageViewpager(index)
            if (!galleryFragment.isVisible && applicationClass.clearState) {
                fragmentManager.beginTransaction()
                    .hide(cameraFragment)
                    .show(galleryFragment)
                    .show(photoListFragment)
                    .commit()
            }

        } else {
            if (applicationClass.clearState) {
                fragmentManager.beginTransaction()
                    .show(cameraFragment)
                    .hide(galleryFragment)
                    .show(photoListFragment)
                    .commit()
                cameraFragment.startCapture(index, captureStatus)
            }
        }
    }


    override fun sendUriToPhotoList(uri: String) {
        Log.d("TAG", "sendUriToPhotoList: " + uri)
        runOnUiThread {
            photoListFragment.notifylistAdapter()
        }
    }

    override fun itemRemmoved(index: Int) {
        runOnUiThread {
            photoListFragment.notifylistAdapter()
        }
    }

    override fun addButton() {
        activityCameraBinding.photolistFrag.findViewById<Button>(R.id.addBtn)
            .setVisibility(View.VISIBLE)
    }

    override fun camnotify() {
        photoListFragment.notifylistAdapter()
    }

    override fun changeBorderOfRecyclerItem(position: Int) {
        Log.d("TAG", "changeBorderOfRecyclerItem: " + position)
//        photoListFragment.changeItemBgOnPagerchange(position)
    }

}