package com.ororo.auto.jigokumimi.ui.result

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.databinding.FragmentResultListBinding
import com.ororo.auto.jigokumimi.ui.common.BaseFragment
import com.ororo.auto.jigokumimi.ui.common.ItemClick
import com.ororo.auto.jigokumimi.util.Constants
import com.ororo.auto.jigokumimi.util.dataBinding
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

/**
 *  検索結果表示画面
 */
class ResultFragment : BaseFragment(R.layout.fragment_result_list) {

    private val viewModel: ResultViewModel by sharedViewModel()

    private val miniPlayerViewModel: MiniPlayerViewModel by sharedViewModel()

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

        if (args.searchType == Constants.Type.TRACK) {
            viewModel.tracklist.observe(viewLifecycleOwner) { tracks ->
                tracks?.apply {
                    viewModelAdapterTrack.tracks = tracks
                }
            }

        } else {
            viewModel.artistlist.observe(viewLifecycleOwner) { artists ->
                artists?.apply {
                    viewModelAdapterArtist.artists = artists
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        viewModel.setDistance(args.distance)
        viewModel.setSearchDateTime(args.searchDateTime)
        viewModel.setSearchType(args.searchType)

        // 検索種別に応じてアダプター(リストに表示する項目)を切り替え
        if (args.searchType == Constants.Type.TRACK) {

            // キューアイコンがタップされたときのコールバックを設定
            val playCallback =
                ItemClick { index: Int ->
                    onQueueButtonClicked(index)
                }

            // Track追加ボタンがタップされたときのコールバックを設定
            val saveOrRemoveCallback =
                ItemClick { trackIndex: Int ->
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
                ItemClick { artistIndex: Int ->
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
                    "${context?.getString(R.string.title_result)} ${
                        if (viewModel.isDemo()) context?.getString(
                            R.string.title_demo
                        ) else ""
                    }"
                title = titleStr
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // 再生曲を停止し､音楽プレーヤーを隠す
        miniPlayerViewModel.stopTrack()
        miniPlayerViewModel.hideMiniPlayer()
    }

    private fun onQueueButtonClicked(trackIndex: Int) {
        miniPlayerViewModel.setPlayingTrack(trackIndex)
        miniPlayerViewModel.playTrack()
    }

    private fun onSaveOrRemoveButtonClicked(trackIndex: Int) {
        viewModel.changeTrackFavoriteState(trackIndex)

    }

    private fun onFollowOrUnFollowButtonClicked(artistIndex: Int) {
        viewModel.changeArtistFollowState(artistIndex)
    }
}

