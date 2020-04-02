package com.ororo.auto.jigokumimi.util

import android.widget.ImageButton
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

/**
 * Binding adapter used to display images from URL using Glide
 */
@BindingAdapter("imageUrl")
fun setImageUrl(imageView: ImageView, url: String) {
    Glide.with(imageView.context).load(url).into(imageView)
}

@BindingAdapter("dynamicSrcCompat")
fun dynamicSrcCompat(view: ImageButton, resourceId: Int) {
    view.setImageResource(resourceId)
}