package com.ororo.auto.jigokumimi.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.databinding.DetectedSongListItemBinding
import com.ororo.auto.jigokumimi.databinding.FragmentDetectedSongListBinding
import com.ororo.auto.jigokumimi.domain.Song
import com.ororo.auto.jigokumimi.util.Constants
import com.ororo.auto.jigokumimi.viewmodels.SongListViewModel
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationResponse

/**
 *  周辺曲情報表示画面
 */
class DetectedSongListFragment : Fragment() {


    lateinit var viewModel: SongListViewModel

    /**
     * RecyclerView Adapter
     */
    private lateinit var viewModelAdapter: DetectedSongListAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.songlist.observe(viewLifecycleOwner, Observer<List<Song>> { songs ->
            songs?.apply {
                viewModelAdapter.songs = songs
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        activity?.run {
            val viewModelFactory = SongListViewModel.Factory(this.application, this)

            viewModel = ViewModelProvider(viewModelStore, viewModelFactory).get(SongListViewModel::class.java)
        }

        val binding: FragmentDetectedSongListBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_detected_song_list,
            container,
            false
        )

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        viewModelAdapter = DetectedSongListAdapter(PlayClick { song: Song ->

            viewModel.playingSong.value = song
            viewModel.isPlaying.value = true
        })

        binding.recyclerView.adapter = viewModelAdapter

        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        val request = viewModel.getAuthenticationRequest(AuthorizationResponse.Type.TOKEN)

        AuthorizationClient.openLoginActivity(
            activity,
            Constants.AUTH_TOKEN_REQUEST_CODE,
            request
        )

        binding.refreshButton.setOnClickListener {
            refreshSongs()
        }

        binding.locationButton.setOnClickListener {
            getLocation()
        }

        viewModel.eventNetworkError.observe(
            viewLifecycleOwner,
            Observer<Boolean> { isNetworkError ->
                if (isNetworkError) onNetworkError()
            })

        return binding.root
    }

    /**
     * 周辺曲情報を更新する
     */
    fun refreshSongs() {
        viewModel.refreshSongsFromRepository()
    }

    /**
     * 位置情報を取得する
     */
    fun getLocation() {
        viewModel.postLocationAndMyFavoriteSongs()
    }

    /**
     * ネットワークエラー時にトーストでエラー表示
     */
    private fun onNetworkError() {
        if (!viewModel.isNetworkErrorShown.value!!) {
            Toast.makeText(activity, "Network Error", Toast.LENGTH_LONG).show()
            viewModel.onNetworkErrorShown()
        }
    }
}

/**
 * リストアイテムを設定､表示するアダプタ
 */
class DetectedSongListAdapter(val callback: PlayClick) :
    RecyclerView.Adapter<DetectedSongListViewHolder>() {

    /**
     * リストに表示する曲情報
     */
    var songs: List<Song> = emptyList()
        set(value) {
            field = value

            // リストが更新されたことを通知
            notifyDataSetChanged()
        }

    /**
     * リストアイテムが作られたときに呼ばれる
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetectedSongListViewHolder {
        val withDataBinding: DetectedSongListItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            DetectedSongListViewHolder.LAYOUT,
            parent,
            false
        )
        return DetectedSongListViewHolder(withDataBinding)
    }

    override fun getItemCount() = songs.size

    /**
     * リストアイテムが表示されたときに呼ばれる
     */
    override fun onBindViewHolder(holder: DetectedSongListViewHolder, position: Int) {
        holder.viewDataBinding.also {
            it.song = songs[position]

            it.playCallback = callback
        }
    }
}

/**
 * 別ファイルで定義したレイアウトをつなげるViewHolder
 */
class DetectedSongListViewHolder(val viewDataBinding: DetectedSongListItemBinding) :
    RecyclerView.ViewHolder(viewDataBinding.root) {
    companion object {
        @LayoutRes
        val LAYOUT = R.layout.detected_song_list_item
    }
}

/**
 * リストアイテム内のキューアイコンをクリックしたときのイベントハンドラ
 *
 */
class PlayClick(val block: (Song) -> Unit) {
    /**
     * Called when a song is clicked
     *
     * @param song the song that was clicked
     */
    fun onClick(song: Song) = block(song)
}