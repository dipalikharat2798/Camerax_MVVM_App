package com.androdevdk.camerax_mvvm_app.view.fragments

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.SeekBar
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import com.androdevdk.camerax_mvvm_app.ApplicationClass
import com.androdevdk.camerax_mvvm_app.R
import com.androdevdk.camerax_mvvm_app.databinding.FragmentCameraBinding
import com.androdevdk.camerax_mvvm_app.view.activities.CameraActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraFragment : Fragment() {
    private var _fragmentCameraBinding: FragmentCameraBinding? = null
    private val fragmentCameraBinding get() = _fragmentCameraBinding!!
    private lateinit var outputDirectory: File
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private lateinit var cameraControl: CameraControl
    lateinit var cameraProvider: ProcessCameraProvider
    val ANIMATION_FAST_MILLIS = 50L
    val ANIMATION_SLOW_MILLIS = 100L
    lateinit var cameraSendData: CameraSendData
    lateinit var applicationClass: ApplicationClass
    private var trackCount: Int = -1
    private var savedUri1: Uri? = null
    private var valueCountForAdd: Int = 0

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService
    var zoomVal: Int = 0
    var flashVal: Int = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _fragmentCameraBinding = FragmentCameraBinding.inflate(inflater, container, false)
        return fragmentCameraBinding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        cameraSendData = context as CameraSendData
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applicationClass = ApplicationClass.instance
        zoomVal = 0
        flashVal = 0
        cameraExecutor = Executors.newSingleThreadExecutor()
        // Determine the output directory
        outputDirectory = CameraActivity.getOutputDirectory(requireContext())
        fragmentCameraBinding.viewFinder.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE)
        // Wait for the views to be properly laid out
        fragmentCameraBinding.viewFinder.post {
            // Set up the camera and its use cases
            setUpCamera()
        }

        fragmentCameraBinding.zoomBtn.setOnClickListener(listener)
        fragmentCameraBinding.flashBtn.setOnClickListener(listener)
        fragmentCameraBinding.retakeBtn.setOnClickListener(listener)
        fragmentCameraBinding.tapToCaptureTv.setOnClickListener(listener)
        fragmentCameraBinding.saveAndNextBtn.setOnClickListener(listener)
    }

    override fun onResume() {
        super.onResume()
        if (!PermissionsFragment.hasPermissions(requireContext())) {
            var fragment = PermissionsFragment()
            var fragmentManager = requireActivity().supportFragmentManager
            var fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.camera_frag, fragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }
    }

    override fun onStart() {
        super.onStart()
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        fragmentCameraBinding.viewFinder.setOnClickListener {
            imageCapture?.let { imageCapture ->

                // Create output file to hold the image
                val photoFile = createFile(outputDirectory, FILENAME, PHOTO_EXTENSION)

                // Setup image capture metadata
                val metadata = ImageCapture.Metadata().apply {

                    // Mirror image when using the front camera
                    isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
                }

                // Create output options object which contains file + metadata
                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
                    .setMetadata(metadata)
                    .build()

                // Setup image capture listener which is triggered after photo has been taken
                imageCapture.takePicture(
                    outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
                        override fun onError(exc: ImageCaptureException) {
                            Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                        }

                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                            savedUri1 = savedUri
                            Log.d(TAG, "Photo capture succeeded: $savedUri")

                            if (!trackCount.equals(-1)) {
                                applicationClass.clearState = false
                                applicationClass.capturedData.set(trackCount, savedUri.toString())
                                Log.d(TAG, "onImageSaved: " + trackCount)
                                cameraSendData.sendUriToPhotoList(savedUri.toString())
                                activity?.runOnUiThread {
                                    tapToCapture()
                                    //  fragmentCameraBinding.mainimageView.setImageURI(savedUri)
                                }
                            }
                            // If the folder selected is an external media directory, this is
                            // unnecessary but otherwise other apps will not be able to access our
                            // images unless we scan them using [MediaScannerConnection]
                            val mimeType = MimeTypeMap.getSingleton()
                                .getMimeTypeFromExtension(savedUri.toFile().extension)
                            MediaScannerConnection.scanFile(
                                context,
                                arrayOf(savedUri.toFile().absolutePath),
                                arrayOf(mimeType)
                            ) { _, uri ->
                                Log.d(TAG, "Image capture scanned into media store: $uri")
                            }
                        }
                    })
                // We can only change the foreground Drawable using API level 23+ API
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    // Display flash animation to indicate that photo was captured
                    fragmentCameraBinding.root.postDelayed({
                        fragmentCameraBinding.root.foreground = ColorDrawable(Color.WHITE)
                        fragmentCameraBinding.root.postDelayed(
                            { fragmentCameraBinding.root.foreground = null }, ANIMATION_FAST_MILLIS
                        )
                    }, ANIMATION_SLOW_MILLIS)
                }
            }
            //  tapToCapture()
        }
    }

    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(Runnable {

            // CameraProvider
            cameraProvider = cameraProviderFuture.get()

            // Select lensFacing depending on the available cameras
            lensFacing = when {
                hasBackCamera() -> CameraSelector.LENS_FACING_BACK
                hasFrontCamera() -> CameraSelector.LENS_FACING_FRONT
                else -> throw IllegalStateException("Back and front camera are unavailable")
            }

            // Enable or disable switching between cameras
            //updateCameraSwitchButton()

            // Build and bind the camera use cases
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext().applicationContext))

    }

    private fun bindCameraUseCases() {
        // CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        // Preview
        preview = Preview.Builder()
            // We request aspect ratio but no resolution

            .build()

        // ImageCapture
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            // We request aspect ratio but no resolution to match preview config, but letting
            // CameraX optimize for whatever specific resolution best fits our use cases
            .build()

        // ImageAnalysis
        imageAnalyzer = ImageAnalysis.Builder()
            // We request aspect ratio but no resolution
            .build()

        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture, imageAnalyzer
            )
            cameraControl = camera!!.cameraControl
            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(fragmentCameraBinding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e("TAG", "Use case binding failed", exc)
        }
    }

    /** Returns true if the device has an available back camera. False otherwise */
    private fun hasBackCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }

    /** Returns true if the device has an available front camera. False otherwise */
    private fun hasFrontCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
    }

    override fun onDestroy() {
        super.onDestroy()
        _fragmentCameraBinding = null
        cameraExecutor.shutdown()
    }

    public fun startCapture(index: Int, captureStatus: Boolean) {

        trackCount = index
        fragmentCameraBinding.viewFinder.setVisibility(View.VISIBLE)
        Toast.makeText(requireContext(), "Received in LeftFrag " + index, Toast.LENGTH_SHORT)
            .show()
        fragmentCameraBinding.mainimageView.setImageResource(applicationClass.imageData[index].image)
        fragmentCameraBinding.retakeBtn.setVisibility(View.INVISIBLE)
        fragmentCameraBinding.saveAndNextBtn.setVisibility(View.INVISIBLE)
        fragmentCameraBinding.mainimageView1.setVisibility(View.INVISIBLE)
        fragmentCameraBinding.tapToCaptureTv.setVisibility(View.VISIBLE)
        fragmentCameraBinding.mainimageView2.setVisibility(View.GONE)
        takePhoto()

//            var fragment = GalleryFragment()
//            var fragmentManager = requireActivity().supportFragmentManager
//            var fragmentTransaction = fragmentManager.beginTransaction()
//            fragmentTransaction.replace(R.id.camera_frag, fragment)
//            fragmentTransaction.addToBackStack(null)
//            fragmentTransaction.commit()
    }

    //zoom_finctionality
    private fun zoom() {
        if (zoomVal.equals(0)) {
            fragmentCameraBinding.seekBar.setVisibility(View.VISIBLE)
            fragmentCameraBinding.seekBar.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    cameraControl.setLinearZoom(progress / 100.toFloat())
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                    Log.d(TAG, "onStartTrackingTouch: ")
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                    Log.d(TAG, "onStopTrackingTouch: ")
                }
            })
            Toast.makeText(requireContext(), "Zoom enable", Toast.LENGTH_SHORT).show()
            zoomVal = 1
        } else {
            fragmentCameraBinding.seekBar.setVisibility(View.GONE)
            Toast.makeText(requireContext(), "Zoom disable", Toast.LENGTH_SHORT).show()
            zoomVal = 0
        }
    }

    //flassOnOff_functionality
    private fun flash_onoff() {
        if (flashVal.equals(0)) {
            fragmentCameraBinding.flashBtn.setImageResource(R.drawable.ic_flash_on)
            Toast.makeText(requireContext(), "flash enable", Toast.LENGTH_SHORT).show()
            cameraControl.enableTorch(true)
            flashVal = 1
        } else {
            fragmentCameraBinding.flashBtn.setImageResource(R.drawable.ic_flash_off)
            Toast.makeText(requireContext(), "flash disable", Toast.LENGTH_SHORT).show()
            cameraControl.enableTorch(false)
            flashVal = 0
        }
    }

    //retake
    private fun retakeImage() {
        applicationClass.capturedData.set(trackCount, "")
        cameraSendData.itemRemmoved(trackCount)
        //static overlay
        fragmentCameraBinding.mainimageView.setImageResource(R.drawable.overlayimg)
        fragmentCameraBinding.retakeBtn.setVisibility(View.GONE)
        fragmentCameraBinding.saveAndNextBtn.setVisibility(View.GONE)
        fragmentCameraBinding.tapToCaptureTv.setVisibility(View.VISIBLE)
        fragmentCameraBinding.viewFinder.setVisibility(View.VISIBLE)
        applicationClass.clearState = false
    }

    //saveandnext
    private fun saveAndNext() {
        applicationClass.clearState = true
        valueCountForAdd = valueCountForAdd + 1
        cameraSendData.itemRemmoved(trackCount)
        //static overlay
        fragmentCameraBinding.mainimageView.setImageResource(R.drawable.overlayimg)
        fragmentCameraBinding.retakeBtn.setVisibility(View.GONE)
        fragmentCameraBinding.saveAndNextBtn.setVisibility(View.GONE)
        fragmentCameraBinding.tapToCaptureTv.setVisibility(View.GONE)
        fragmentCameraBinding.viewFinder.setVisibility(View.GONE)

        if (valueCountForAdd.equals(applicationClass.imageData.size)) {
            cameraSendData.addButton()
        }
    }

    //tapToCapture
    private fun tapToCapture() {
        // cameraSendData.sendUriToPhotoList("uri")
        fragmentCameraBinding.retakeBtn.setVisibility(View.VISIBLE)
        fragmentCameraBinding.tapToCaptureTv.setVisibility(View.GONE)
        fragmentCameraBinding.saveAndNextBtn.setVisibility(View.VISIBLE)
        fragmentCameraBinding.viewFinder.setVisibility(View.GONE)
        fragmentCameraBinding.mainimageView.setImageURI(savedUri1)
//        fragmentCameraBinding.mainimageView.setVisibility(View.VISIBLE)
//        fragmentCameraBinding.mainimageView.setImageURI(uri)
    }

    val listener = View.OnClickListener { view ->
        when (view.getId()) {
            R.id.zoom_btn -> {
                zoom()
            }
            R.id.flash_btn -> {
                // if(fragmentCameraBinding.viewFinder.visibility == View.VISIBLE)
                flash_onoff()
            }
            R.id.retake_btn -> {
                retakeImage()
            }
            R.id.saveAndNext_btn -> {
                saveAndNext()
            }
        }
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"

        /** Helper function used to create a timestamped file */
        private fun createFile(baseFolder: File, format: String, extension: String) =
            File(
                baseFolder, SimpleDateFormat(format, Locale.US)
                    .format(System.currentTimeMillis()) + extension
            )
    }

    public interface CameraSendData {
        fun sendUriToPhotoList(uri: String)
        fun itemRemmoved(index: Int)
        fun addButton()
        fun camnotify()
    }
}