package com.androdevdk.camerax_mvvm_app

import android.app.Application
import com.androdevdk.camerax_mvvm_app.model.Image

class ApplicationClass : Application() {
    var imageData: MutableList<Image> = mutableListOf<Image>()
    var capturedData: MutableList<String> = mutableListOf<String>()
    var clearState: Boolean = true

    override fun onCreate() {
        super.onCreate()
        instance = this

        //overlay image list
        imageData.add(Image(R.drawable.overlayimg))
        imageData.add(Image(R.drawable.square_overlay))
        imageData.add(Image(R.drawable.overlayimg))
        imageData.add(Image(R.drawable.overlayimg))
        imageData.add(Image(R.drawable.overlayimg))

        for (i in 0..4) {
            capturedData.add(i, "")
        }
    }

    companion object {
        lateinit var instance: ApplicationClass
            private set
    }
}