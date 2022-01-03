package com.androdevdk.camerax_mvvm_app.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.androdevdk.camerax_mvvm_app.ApplicationClass
import com.androdevdk.camerax_mvvm_app.R
import com.androdevdk.camerax_mvvm_app.adapter.CameraPhotoListAdapter
import com.androdevdk.camerax_mvvm_app.databinding.FragmentPhotoListBinding
import com.androdevdk.camerax_mvvm_app.model.Image

class PhotoListFragment : Fragment() {
    private var _fragmentPhotoListBinding: FragmentPhotoListBinding? = null
    private val fragmentPhotoListBinding get() = _fragmentPhotoListBinding!!
    var adapter: CameraPhotoListAdapter? = null
    lateinit var applicationClass: ApplicationClass
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _fragmentPhotoListBinding = FragmentPhotoListBinding.inflate(inflater, container, false)
        return fragmentPhotoListBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applicationClass = ApplicationClass.instance
        fragmentPhotoListBinding.recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = CameraPhotoListAdapter(
            requireContext(),
            applicationClass.imageData,
            applicationClass.capturedData
        )
        fragmentPhotoListBinding.recycler.adapter = adapter
        fragmentPhotoListBinding.addBtn.setOnClickListener(listener)
    }

    public fun notifylistAdapter() {
        adapter?.notifyDataSetChanged()
    }

    public fun call(index: Int) {
        adapter?.notifyItemRemoved(index)
        adapter?.notifyItemRangeChanged(index, applicationClass.imageData.size)
    }

    override fun onDestroy() {
        super.onDestroy()
        _fragmentPhotoListBinding = null
    }

    val listener = View.OnClickListener { view ->
        when (view.getId()) {
            R.id.addBtn -> {
                addListItemDynamically()
            }
        }
    }

    public fun changeItemBgOnPagerchange(position: Int) {
        Log.d("TAG", "Photolist: " + position)
      //  adapter?.notifyDataSetChanged()
    }

    private fun addListItemDynamically() {
        applicationClass.imageData.add(Image(R.drawable.overlayimg))
        applicationClass.capturedData.add("")
        adapter?.notifyDataSetChanged()
        fragmentPhotoListBinding.addBtn.setVisibility(View.GONE)
    }
}