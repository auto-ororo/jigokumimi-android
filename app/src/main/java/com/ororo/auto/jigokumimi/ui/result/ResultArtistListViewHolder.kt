package com.ororo.auto.jigokumimi.ui.result

import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.databinding.ResultArtistItemBinding

/**
 * 別ファイルで定義したArtistレイアウトをつなげるViewHolder
 */
class ResultArtistListViewHolder(val viewDataBinding: ResultArtistItemBinding) :
    RecyclerView.ViewHolder(viewDataBinding.root) {
    companion object {
        @LayoutRes
        val LAYOUT = R.layout.result_artist_item
    }
}
