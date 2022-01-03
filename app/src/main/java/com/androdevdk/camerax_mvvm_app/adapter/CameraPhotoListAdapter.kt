package com.androdevdk.camerax_mvvm_app.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.androdevdk.camerax_mvvm_app.R
import com.androdevdk.camerax_mvvm_app.model.Image


class CameraPhotoListAdapter : RecyclerView.Adapter<CameraPhotoListAdapter.ViewHolder> {
    private var lastPosition = -1
    lateinit var context: Context
    var mList: MutableList<Image> = mutableListOf<Image>()
    var captureList: MutableList<String> = mutableListOf<String>()
    lateinit var activity: CameraPhotoListAdapter.ImageItemClicked
    private var row_index: Int = -1

    constructor()
    constructor(
        context: Context,
        mList: MutableList<Image>,
        captureList: MutableList<String>
    ) : this() {
        this.context = context
        this.mList = mList
        this.captureList = captureList
        this.activity = context as ImageItemClicked
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_list, parent, false)
        return CameraPhotoListAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemsModel = mList[position]
        var captureStatus: Boolean = false
        if (captureList[position].equals("")) {
            holder.imageView.setImageResource(itemsModel.image)
            captureStatus = false
        } else {
            holder.imageView.setImageURI(Uri.parse(captureList.get(position)))
            captureStatus = true
        }
        holder.linearLayout.setOnClickListener() {
            row_index = position
            activity.onImageItemClicked(position, captureStatus)
            notifyDataSetChanged()
        }
        if (row_index == position) {
            holder.linearLayout.setBackgroundResource(R.drawable.border1)
        } else {
            holder.linearLayout.setBackgroundResource(R.drawable.border)
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.item_img)
        val linearLayout: LinearLayout = itemView.findViewById(R.id.animlayout)
    }

    public interface ImageItemClicked {
        fun onImageItemClicked(index: Int, catureaStatus: Boolean)
        //fun onClick(pos: Int, aView: View)
    }
}