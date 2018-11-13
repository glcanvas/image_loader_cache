package com.imageLoader.nikita.imageLoader.utils

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.imageLoader.nikita.imageLoader.R

class ImageListViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    var author_name = view.findViewById<TextView>(R.id.holder_author_name)
    var description = view.findViewById<TextView>(R.id.holder_description)
    var previewImage = view.findViewById<ImageView>(R.id.holder_preview_image)
    var progressBar = view.findViewById<ProgressBar>(R.id.holder_progress_bar)
}