package com.androdevdk.camerax_mvvm_app.view.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.androdevdk.camerax_mvvm_app.ApplicationClass
import com.androdevdk.camerax_mvvm_app.R
import com.androdevdk.camerax_mvvm_app.adapter.ViewPagerAdapter
import com.androdevdk.camerax_mvvm_app.databinding.FragmentGalleryBinding
import com.androdevdk.camerax_mvvm_app.view.activities.CameraActivity

/** Fragment used to present the user with a gallery of photos taken */
class GalleryFragment : Fragment() {

    /** Android ViewBinding */
    private var _fragmentGalleryBinding: FragmentGalleryBinding? = null

    private val fragmentGalleryBinding get() = _fragmentGalleryBinding!!

    lateinit var applicationClass: ApplicationClass
    lateinit var galleryFragmentInterface: GalleryFragmentInterface
    public var positionForViewpager:Int=0
    override fun onAttach(context: Context) {
        super.onAttach(context)
        galleryFragmentInterface = context as GalleryFragmentInterface
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentGalleryBinding = FragmentGalleryBinding.inflate(inflater, container, false)
        return fragmentGalleryBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applicationClass = ApplicationClass.instance

        val adapter = ViewPagerAdapter(applicationClass.capturedData)
        fragmentGalleryBinding.photoViewPager.adapter = adapter
        fragmentGalleryBinding.photoViewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
        fragmentGalleryBinding.backButton.setOnClickListener(listener)
        fragmentGalleryBinding.photoViewPager?.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d("TAG", "onPageSelected: " + position)
//                    galleryFragmentInterface.changeBorderOfRecyclerItem(position)
            }
        })
    }

    public fun setstartImageViewpager(index:Int){
        fragmentGalleryBinding.photoViewPager.setCurrentItem(index,false)
    }
    val listener = View.OnClickListener { view ->
        when (view.getId()) {
            R.id.back_button -> {
                backToCamera()
            }
        }
    }

    private fun backToCamera() {
        (activity as CameraActivity?)?.showFrag()
    }

    override fun onDestroyView() {
        _fragmentGalleryBinding = null
        super.onDestroyView()
    }

    public interface GalleryFragmentInterface {
        fun changeBorderOfRecyclerItem(position: Int)
    }
}