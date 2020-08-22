package com.ororo.auto.jigokumimi.ui.result

import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.databinding.ResultTrackItemBinding

/**
 * 別ファイルで定義したTrackレイアウトをつなげるViewHolder
 */
class ResultTrackListViewHolder(val viewDataBinding: ResultTrackItemBinding) :
    RecyclerView.ViewHolder(viewDataBinding.root) {
    companion object {
        @LayoutRes
        val LAYOUT = R.layout.result_track_item
    }
}
