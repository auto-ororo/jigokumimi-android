package com.ororo.auto.jigokumimi.ui.result

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ororo.auto.jigokumimi.databinding.ResultArtistItemBinding
import com.ororo.auto.jigokumimi.domain.Artist
import com.ororo.auto.jigokumimi.ui.common.ItemClick

/**
 * Artistを設定､表示するアダプタ
 */
class ResultArtistListAdapter(private val followOrUnFollowArtistCallback: ItemClick) :
    RecyclerView.Adapter<ResultArtistListViewHolder>() {

    /**
     * リストに表示する曲情報
     */
    var artists: List<Artist> = emptyList()
        set(value) {
            field = value

            // リストが更新されたことを通知
            notifyDataSetChanged()
        }

    /**
     * リストアイテムが作られたときに呼ばれる
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultArtistListViewHolder {
        val withDataBinding: ResultArtistItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            ResultArtistListViewHolder.LAYOUT,
            parent,
            false
        )
        return ResultArtistListViewHolder(
            withDataBinding
        )
    }

    override fun getItemCount() = artists.size

    /**
     * リストアイテムが表示されたときに呼ばれる
     */
    override fun onBindViewHolder(holderTrack: ResultArtistListViewHolder, position: Int) {
        holderTrack.viewDataBinding.also {
            it.artist = artists[position]
            it.followOrUnFollowCallback = followOrUnFollowArtistCallback
            it.position = position
        }
    }
}
