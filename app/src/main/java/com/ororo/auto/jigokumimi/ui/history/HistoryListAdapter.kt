package com.ororo.auto.jigokumimi.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ororo.auto.jigokumimi.databinding.HistoryItemBinding
import com.ororo.auto.jigokumimi.domain.History
import com.ororo.auto.jigokumimi.ui.common.ItemClick
import timber.log.Timber

/**
 * Historyを設定､表示するアダプタ
 */
class HistoryListAdapter(
    private val historyClickCallback: ItemClick,
    private val deleteHistoryClickCallback: ItemClick
) :
    RecyclerView.Adapter<HistoryListViewHolder>() {

    /**
     * リストに表示する検索履歴情報
     */
    var histories: List<History> = emptyList()
        set(value) {
            field = value

            // リストが更新されたことを通知
            notifyDataSetChanged()
        }

    /**
     * リストアイテムが作られたときに呼ばれる
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryListViewHolder {
        Timber.d("onCreateViewHolder called")
        val withDataBinding: HistoryItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            HistoryListViewHolder.LAYOUT,
            parent,
            false
        )
        return HistoryListViewHolder(
            withDataBinding
        )
    }

    override fun getItemCount() = histories.size

    /**
     * リストアイテムが表示されたときに呼ばれる
     */
    override fun onBindViewHolder(holderTrack: HistoryListViewHolder, position: Int) {
        Timber.d("onBindViewHolder called")
        holderTrack.viewDataBinding.also {
            it.history = histories[position]
            it.historyClickCallback = historyClickCallback
            it.deleteHistoryClickCallback = deleteHistoryClickCallback
            it.position = position
        }
    }
}
