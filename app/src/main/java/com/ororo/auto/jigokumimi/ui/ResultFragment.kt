package com.ororo.auto.jigokumimi.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.databinding.FragmentResultListBinding
import com.ororo.auto.jigokumimi.databinding.ResultArtistItemBinding
import com.ororo.auto.jigokumimi.databinding.ResultTrackItemBinding
import com.ororo.auto.jigokumimi.domain.Artist
import com.ororo.auto.jigokumimi.domain.Track
import com.ororo.auto.jigokumimi.util.Constants
import com.ororo.auto.jigokumimi.viewmodels.ResultViewModel

/**
 *  検索結果表示画面
 */
class ResultFragment : BaseFragment() {

    lateinit var viewModel: ResultViewModel

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        activity?.run {
            val viewModelFactory = ResultViewModel.Factory(this.application)

            viewModel = ViewModelProvider(
                viewModelStore,
                viewModelFactory
            ).get(ResultViewModel::class.java)
        }

        val binding: FragmentResultListBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_result_list,
            container,
            false
        )

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        // 検索種別に応じてアダプター(リストに表示するレイアウト)を分岐
        if (args.searchType == Constants.SearchType.TRACK) {
            // キューボタンがタップされたときの処理を設定
            viewModelAdapterTrack = ResultTrackListAdapter(PlayClick { track: Track ->

                viewModel.setPlayingTrack(track)
                viewModel.playTrack()
            })

            binding.recyclerView.adapter = viewModelAdapterTrack

        } else {
            viewModelAdapterArtist = ResultArtistListAdapter()
            binding.recyclerView.adapter = viewModelAdapterArtist
        }


        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        baseInit(viewModel)

        return binding.root
    }

}

/**
 * Trackを設定､表示するアダプタ
 */
class ResultTrackListAdapter(val callback: PlayClick) :
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
        return ResultTrackListViewHolder(withDataBinding)
    }

    override fun getItemCount() = tracks.size

    /**
     * リストアイテムが表示されたときに呼ばれる
     */
    override fun onBindViewHolder(holderTrack: ResultTrackListViewHolder, position: Int) {
        holderTrack.viewDataBinding.also {
            it.track = tracks[position]
            it.playCallback = callback
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
class ResultArtistListAdapter() :
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
        return ResultArtistListViewHolder(withDataBinding)
    }

    override fun getItemCount() = artists.size

    /**
     * リストアイテムが表示されたときに呼ばれる
     */
    override fun onBindViewHolder(holderTrack: ResultArtistListViewHolder, position: Int) {
        holderTrack.viewDataBinding.also {
            it.artist = artists[position]

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
 * リストアイテム内のキューアイコンをクリックしたときのイベントハンドラ
 *
 */
class PlayClick(val block: (Track) -> Unit) {
    /**
     * Called when a song is clicked
     *
     * @param track the song that was clicked
     */
    fun onClick(track: Track) = block(track)
}