package com.ororo.auto.jigokumimi.ui.history

import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.databinding.HistoryItemBinding

/**
 * 別ファイルで定義したHistoryItemレイアウトをつなげるViewHolder
 */
class HistoryListViewHolder(val viewDataBinding: HistoryItemBinding) :
    RecyclerView.ViewHolder(viewDataBinding.root) {
    companion object {
        @LayoutRes
        val LAYOUT = R.layout.history_item
    }
}
