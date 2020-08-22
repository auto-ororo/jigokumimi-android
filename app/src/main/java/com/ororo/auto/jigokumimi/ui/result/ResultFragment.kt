package com.ororo.auto.jigokumimi.ui.result


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.databinding.FragmentResultListBinding
import com.ororo.auto.jigokumimi.databinding.ResultArtistItemBinding
import com.ororo.auto.jigokumimi.databinding.ResultTrackItemBinding
import com.ororo.auto.jigokumimi.domain.Artist
import com.ororo.auto.jigokumimi.domain.Track
import com.ororo.auto.jigokumimi.ui.common.BaseFragment
import com.ororo.auto.jigokumimi.util.Constants
import com.ororo.auto.jigokumimi.util.dataBinding
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

/**
 *  検索結果表示画面
 */
class ResultFragment : BaseFragment(R.layout.fragment_result_list) {

    private val viewModel: ResultViewModel by sharedViewModel()

    private val binding by dataBinding<FragmentResultListBinding>()

    private val args: ResultFragmentArgs by navArgs()

    /**
     * RecyclerView Track Adapter
     */
    private lateinit var viewModelAdapterTrack: ResultTrackListAdapter

    /**
     * RecyclerView Artist Adapter
     */
    private lateinit var viewModelAdapterArtist: ResultArtistListAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (args.searchType == Constants.SearchType.TRACK) {
            viewModel.tracklist.observe(viewLifecycleOwner, Observer { tracks ->
                tracks?.apply {
                    viewModelAdapterTrack.tracks = tracks
                }
            })

        } else {
            viewModel.artistlist.observe(viewLifecycleOwner, Observer { artists ->
                artists?.apply {
                    viewModelAdapterArtist.artists = artists
                }
            })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        viewModel.setDistance(args.distance)
        viewModel.setSearchDateTime(args.searchDateTime)
        viewModel.setSearchType(args.searchType)

        // 検索種別に応じてアダプター(リストに表示する項目)を切り替え
        if (args.searchType == Constants.SearchType.TRACK) {

            // キューアイコンがタップされたときのコールバックを設定
            val playCallback =
                PlayTrackClick { index: Int ->
                    onQueueButtonClicked(index)
                }

            // Track追加ボタンがタップされたときのコールバックを設定
            val saveOrRemoveCallback =
                SaveOrRemoveTrackClick { trackIndex: Int ->
                    onSaveOrRemoveButtonClicked(trackIndex)
                }

            // コールバックをコンストラクタに渡してアダプタを登録
            viewModelAdapterTrack =
                ResultTrackListAdapter(
                    playCallback,
                    saveOrRemoveCallback
                )
            binding.recyclerView.adapter = viewModelAdapterTrack

            // データが変更された際にAdapterに検知
            viewModel.changeDataIndex.observe(viewLifecycleOwner) {
                viewModelAdapterTrack.notifyItemChanged(it)
            }

        } else {

            // Artistフォローボタンがタップされたときのコールバックを設定
            val followOrUnFollowCallback =
                FollowOrUnFollowArtistClick { artistIndex: Int ->
                    onFollowOrUnFollowButtonClicked(artistIndex)
                }

            // コールバックをコンストラクタに渡してアダプタを登録
            viewModelAdapterArtist =
                ResultArtistListAdapter(
                    followOrUnFollowCallback
                )
            binding.recyclerView.adapter = viewModelAdapterArtist


            // データが変更された際にAdapterに検知
            viewModel.changeDataIndex.observe(viewLifecycleOwner) {
                viewModelAdapterArtist.notifyItemChanged(it)
            }
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        // アイテム間に枠線を設定
        val dividerItemDecoration =
            DividerItemDecoration(binding.recyclerView.context, DividerItemDecoration.VERTICAL)
        dividerItemDecoration.setDrawable(activity?.applicationContext?.getDrawable(R.drawable.divider)!!)
        binding.recyclerView.addItemDecoration(dividerItemDecoration)

        baseInit(viewModel)

        // タイトル設定
        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity).supportActionBar?.run {
                val titleStr =
                    "${context?.getString(R.string.title_result)} ${if (viewModel.isDemo()) context?.getString(
                        R.string.title_demo
                    ) else ""}"
                title = titleStr
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // 再生曲を停止し､音楽プレーヤーを隠す
        viewModel.stopTrack()
        viewModel.hideMiniPlayer()
    }

    private fun onQueueButtonClicked(trackIndex: Int) {
        viewModel.setPlayingTrack(trackIndex)
        viewModel.playTrack()
    }

    private fun onSaveOrRemoveButtonClicked(trackIndex: Int) {
        viewModel.changeTrackFavoriteState(trackIndex)

    }

    private fun onFollowOrUnFollowButtonClicked(artistIndex: Int) {
        viewModel.changeArtistFollowState(artistIndex)
    }
}

/**
 * Trackを設定､表示するアダプタ
 */
class ResultTrackListAdapter(
    val playTrackCallback: PlayTrackClick,
    val saveOrRemoveTrackCallback: SaveOrRemoveTrackClick
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
            it.saveOrRemoveCallback = saveOrRemoveTrackCallback
            it.position = position
        }
    }
}

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

/**
 * Artistを設定､表示するアダプタ
 */
class ResultArtistListAdapter(val followOrUnFollowArtistCallback: FollowOrUnFollowArtistClick) :
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

/**
 * Trackお気に入り追加ボタンをクリックしたときの動作を定義
 *
 */
class SaveOrRemoveTrackClick(val block: (Int) -> Unit) {

    fun onClick(trackIndex: Int) = block(trackIndex)
}

/**
 * キューボタンをクリックしたときの動作を定義
 *
 */
class PlayTrackClick(val block: (Int) -> Unit) {

    fun onClick(trackIndex: Int) = block(trackIndex)
}

/**
 * Artistフォローボタンをクリックしたときの動作を定義
 *
 */
class FollowOrUnFollowArtistClick(val block: (Int) -> Unit) {

    fun onClick(artistIndex: Int) = block(artistIndex)
}
