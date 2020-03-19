package com.ororo.auto.jigokumimi.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.databinding.DetectedTrackListItemBinding
import com.ororo.auto.jigokumimi.databinding.FragmentDetectedTrackListBinding
import com.ororo.auto.jigokumimi.domain.Track
import com.ororo.auto.jigokumimi.util.Constants
import com.ororo.auto.jigokumimi.viewmodels.TrackListViewModel
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationResponse

/**
 *  周辺曲情報表示画面
 */
class DetectedTrackListFragment : Fragment() {

    lateinit var viewModel: TrackListViewModel

    /**
     * RecyclerView Adapter
     */
    private lateinit var viewModelAdapter: DetectedTrackListAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.tracklist.observe(viewLifecycleOwner, Observer<List<Track>> { tracks ->
            tracks?.apply {
                viewModelAdapter.tracks = tracks
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        activity?.run {
            val viewModelFactory = TrackListViewModel.Factory(this.application)

            viewModel = ViewModelProvider(viewModelStore, viewModelFactory).get(TrackListViewModel::class.java)
        }

        val binding: FragmentDetectedTrackListBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_detected_track_list,
            container,
            false
        )

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        viewModelAdapter = DetectedTrackListAdapter(PlayClick { track: Track ->

            viewModel.playingTrack.value = track
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
            refreshTracks()
        }

        binding.locationButton.setOnClickListener {
            getLocation()
        }

        // エラー時にメッセージダイアログを表示
        // 表示時に｢OK｣タップ時の処理を併せて設定する
        viewModel.isErrorDialogShown.observe(
            viewLifecycleOwner,
            Observer<Boolean> { isErrorDialogShown ->
                if (isErrorDialogShown) {
                    val dialog = MessageDialogFragment("ERROR", viewModel.errorMessage.value!!)
                    dialog.setOnOkButtonClickListener(
                        View.OnClickListener {
                            viewModel.isErrorDialogShown.value = false
                            dialog.dismiss()
                        }
                    )
                    dialog.show(parentFragmentManager, "test")
                }
            }
        )

        return binding.root
    }

    /**
     * 周辺曲情報を更新する
     */
    fun refreshTracks() {
        viewModel.refreshTracksFromRepository()
    }

    /**
     * 位置情報を取得する
     */
    fun getLocation() {
        viewModel.postLocationAndMyFavoriteTracks()
    }
}

/**
 * リストアイテムを設定､表示するアダプタ
 */
class DetectedTrackListAdapter(val callback: PlayClick) :
    RecyclerView.Adapter<DetectedTrackListViewHolder>() {

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
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetectedTrackListViewHolder {
        val withDataBinding: DetectedTrackListItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            DetectedTrackListViewHolder.LAYOUT,
            parent,
            false
        )
        return DetectedTrackListViewHolder(withDataBinding)
    }

    override fun getItemCount() = tracks.size

    /**
     * リストアイテムが表示されたときに呼ばれる
     */
    override fun onBindViewHolder(holder: DetectedTrackListViewHolder, position: Int) {
        holder.viewDataBinding.also {
            it.track = tracks[position]

            it.playCallback = callback
        }
    }
}

/**
 * 別ファイルで定義したレイアウトをつなげるViewHolder
 */
class DetectedTrackListViewHolder(val viewDataBinding: DetectedTrackListItemBinding) :
    RecyclerView.ViewHolder(viewDataBinding.root) {
    companion object {
        @LayoutRes
        val LAYOUT = R.layout.detected_track_list_item
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