package com.ororo.auto.jigokumimi.util

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.ororo.auto.jigokumimi.domain.Track
import com.ororo.auto.jigokumimi.R

/**
 * Binding adapter used to display images from URL using Glide
 */
@BindingAdapter("imageUrl")
fun setImageUrl(imageView: ImageView, url: String) {
    Glide.with(imageView.context).load(url).placeholder(R.drawable.ic_no_image).into(imageView)
}

@BindingAdapter("dynamicSrcCompat")
fun dynamicSrcCompat(view: ImageButton, resourceId: Int) {
    view.setImageResource(resourceId)
}

@BindingAdapter("toggleQueueButton")
fun toggleQueueButton(view: ImageButton, previewUrl: String?) {
    view.visibility =  if (previewUrl.isNullOrEmpty()) {
        View.INVISIBLE
    } else {
        View.VISIBLE
    }
}

@BindingAdapter("toggleFavoriteButton")
fun toggleFavoriteButton(view: ImageButton, isDeleted: Boolean) {
    view.visibility =  if (isDeleted) {
        View.INVISIBLE
    } else {
        View.VISIBLE
    }
}

@BindingAdapter("android:visibility")
fun setVisibility(view: View, value: Boolean) {
    view.visibility = if (value) View.VISIBLE else View.GONE
}

@BindingAdapter("android:invisibility")
fun setInvisibility(view: View, value: Boolean) {
    view.visibility = if (value) View.VISIBLE else View.INVISIBLE
}

@BindingAdapter("trackListForRankTrack", "trackIndexForRankTrack", requireAll = false)
fun setRankTrackNameText(view: TextView, trackList: List<Track>?, trackIndex: Int) {

    trackList?.let {
        if (it.isNotEmpty()) {
            it[trackIndex].let {
                view.text = "${it.rank} . ${it.name}"
            }
        }
    }
}

@BindingAdapter("trackListForArtistAlbum", "trackIndexForArtistAlbum", requireAll = false)
fun setArtistAlbumText(view: TextView, trackList: List<Track>?, trackIndex: Int) {
    trackList?.let {
        if (it.isNotEmpty()) {
            it[trackIndex].let {
                view.text = "${it.artists} - ${it.album}"
            }
        }
    }
}

@BindingAdapter("trackListForFavIcon", "trackIndexForFavIcon", requireAll = false)
fun setFavIconFromTrackList(view: ImageButton, trackList: List<Track>?, trackIndex: Int) {

    trackList?.let {
        if (it.isNotEmpty()) {
            it[trackIndex].let {
                val resourceId = if (it.isSaved) {
                    R.drawable.ic_favorite
                } else {
                    R.drawable.ic_favorite_border
                }
                view.setImageResource(resourceId)
            }
        }
    }
}


@BindingAdapter("withInDistance")
fun withInDistance(view: TextView, distance: Int) {
    view.text = "周囲${distance}m"
}
