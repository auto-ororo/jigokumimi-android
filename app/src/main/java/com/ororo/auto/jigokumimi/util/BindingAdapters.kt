package com.ororo.auto.jigokumimi.util

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
    Glide.with(imageView.context).load(url).into(imageView)
}

@BindingAdapter("dynamicSrcCompat")
fun dynamicSrcCompat(view: ImageButton, resourceId: Int) {
    view.setImageResource(resourceId)
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
