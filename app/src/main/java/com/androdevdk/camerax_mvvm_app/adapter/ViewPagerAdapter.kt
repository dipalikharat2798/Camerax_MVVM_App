package com.androdevdk.camerax_mvvm_app.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.androdevdk.camerax_mvvm_app.R

class ViewPagerAdapter(
    val captureList: MutableList<String> = mutableListOf<String>()
) : RecyclerView.Adapter<ViewPagerAdapter.ViewPageViewHolder>() {

    inner class ViewPageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.item_img)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPageViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.viewpagerimage_list, parent, false)
        return ViewPageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewPageViewHolder, position: Int) {
        if (!captureList[position].equals(""))
            holder.imageView.setImageURI(Uri.parse(captureList[position]))
        else
            holder.imageView.setImageResource(R.drawable.overlayimg)
    }

    override fun getItemCount(): Int {
        return captureList.size
    }
}