package com.ororo.auto.jigokumimi.ui.result

import android.os.Bundle
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.databinding.FragmentMiniPlayerBinding
import com.ororo.auto.jigokumimi.util.dataBinding
import com.ororo.auto.jigokumimi.util.setFavIconFromTrackList
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


/**
 * 音楽再生プレーヤー
 */
class MiniPlayerFragment : Fragment(R.layout.fragment_mini_player) {

    private val resultViewModel: ResultViewModel by sharedViewModel()

    private val viewModel: MiniPlayerViewModel by sharedViewModel()

    private val binding by dataBinding<FragmentMiniPlayerBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        // 再生/停止時の動作
        viewModel.isPlaying.observe(viewLifecycleOwner) { isPlaying ->
            if (isPlaying) {
                viewModel.showMiniPlayer()
                binding.seekBar.max = viewModel.mp?.duration!!
                viewModel.mp?.setOnCompletionListener(viewModel)
                binding.playStopButton.setImageResource(R.drawable.ic_pause)
            } else {
                binding.playStopButton.setImageResource(R.drawable.ic_play_arrow)
            }
        }

        // 音楽プレーヤーの表示/非表示
        viewModel.isMiniPlayerShown.observe(viewLifecycleOwner) { isMiniPlayerShown ->
            // 下からプレイヤーが出現するようにアニメーションを設定
            TransitionManager.beginDelayedTransition(
                binding.miniPlayerLayout,
                Slide(Gravity.BOTTOM)
            )

            if (isMiniPlayerShown) {
                binding.miniPlayerLayout.visibility = View.VISIBLE
            } else {
                binding.miniPlayerLayout.visibility = View.GONE
            }
        }

        resultViewModel.changeDataIndex.observe(viewLifecycleOwner) {
            if (it == viewModel.playingTrackIndex.value) {
                setFavIconFromTrackList(
                    binding.miniPlayerSaveTrackButton,
                    viewModel.tracklist.value,
                    it
                )
            }
        }

        // お気にり曲追加ボタン
        binding.miniPlayerSaveTrackButton.setOnClickListener {
            viewModel.playingTrackIndex.value?.let {
                resultViewModel.changeTrackFavoriteState(it)
            }
        }

        // 再生・停止ボタン
        binding.playStopButton.setOnClickListener {
            if (viewModel.isPlaying.value!!) {
                viewModel.stopTrack()
            } else {
                viewModel.resumeTrack()
            }
        }

        // スキップ(次へ)
        binding.nextButton.setOnClickListener {
            viewModel.skipNextTrack()
        }

        // スキップ(戻る)
        binding.previousButton.setOnClickListener {
            viewModel.skipPreviousTrack()
        }

        // シークバー
        binding.seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        viewModel.moveSeekBar(progress);
                        seekBar.progress = progress
                    }
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }
            }
        )

        binding.miniPlayerLayout.setOnTouchListener { _, event ->
            gesture.onTouchEvent(event)
        }
    }

    /**
     * 上から下にスワイプした際にプレーヤーを隠すジェスチャクラス
     */
    private val gesture = GestureDetector(
        activity,
        object : SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onFling(
                e1: MotionEvent, e2: MotionEvent, velocityX: Float,
                velocityY: Float
            ): Boolean {
                val swipeMinDistance = 120
                val swipeThresholdVelocity = 200

                try {
                    // 上から下のスワイプを判断
                    if (e2.y - e1.y > swipeMinDistance && Math.abs(velocityY) > swipeThresholdVelocity) {
                        viewModel.hideMiniPlayer()
                    }
                } catch (e: Exception) { // nothing
                }
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        })

}
