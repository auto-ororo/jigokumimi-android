package com.ororo.auto.jigokumimi.ui.result

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ororo.auto.jigokumimi.databinding.ResultTrackItemBinding
import com.ororo.auto.jigokumimi.domain.Track
import com.ororo.auto.jigokumimi.ui.common.ItemClick

/**
 * Trackを設定､表示するアダプタ
 */
class ResultTrackListAdapter(
    private val playTrackCallback: ItemClick,
    private val itemCallback: ItemClick
) :
    RecyclerView.Adapter<ResultTrackListViewHolder>() {

    /**
     * リストに表示する曲情報
     */
    var tracks: List<Track> = emptyList()
        set(value) {
            field = value

            // リストが更新されたことを通知
            notifyDataSetChanged()
        }

    /**
     * リストアイテムが作られたときに呼ばれる
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultTrackListViewHolder {
        val withDataBinding: ResultTrackItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            ResultTrackListViewHolder.LAYOUT,
            parent,
            false
        )
        return ResultTrackListViewHolder(
            withDataBinding
        )
    }

    override fun getItemCount() = tracks.size

    /**
     * リストアイテムが表示されたときに呼ばれる
     */
    override fun onBindViewHolder(holderTrack: ResultTrackListViewHolder, position: Int) {
        holderTrack.viewDataBinding.also {
            it.track = tracks[position]
            it.playCallback = playTrackCallback
            it.saveOrRemoveCallback = itemCallback
            it.position = position
        }
    }
}
