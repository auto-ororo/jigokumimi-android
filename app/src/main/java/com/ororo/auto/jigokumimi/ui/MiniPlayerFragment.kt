package com.ororo.auto.jigokumimi.ui

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.databinding.FragmentMiniPlayerBinding
import com.ororo.auto.jigokumimi.util.setFavIconFromTrackList
import com.ororo.auto.jigokumimi.viewmodels.ResultViewModel
import kotlinx.android.synthetic.main.fragment_mini_player.*


/**
 * 音楽再生プレーヤー
 */
class MiniPlayerFragment : Fragment() {

    lateinit var viewModel: ResultViewModel

    lateinit var binding: FragmentMiniPlayerBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        activity?.run {
            val viewModelFactory = ResultViewModel.Factory(this.application)

            viewModel = ViewModelProvider(
                viewModelStore,
                viewModelFactory
            ).get(ResultViewModel::class.java)
        }

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_mini_player,
            container,
            false
        )

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        // 再生/停止時の動作
        viewModel.isPlaying.observe(
            viewLifecycleOwner,
            Observer { isPlaying ->
                if (isPlaying) {
                    viewModel.showMiniPlayer()
                    seekBar.max = viewModel.mp?.duration!!
                    viewModel.mp?.setOnCompletionListener(viewModel)
                    binding.playStopButton.setImageResource(R.drawable.ic_pause)
                } else {
                    binding.playStopButton.setImageResource(R.drawable.ic_play_arrow)
                }
            })

        // 音楽プレーヤーの表示/非表示
        viewModel.isMiniPlayerShown.observe(
            viewLifecycleOwner,
            Observer { isMiniPlayerShown ->
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
        )

        viewModel.changeDataIndex.observe(viewLifecycleOwner) {
            if (it == viewModel.playingTrackIndex.value) {
                setFavIconFromTrackList(binding.miniPlayerSaveTrackButton, viewModel.tracklist.value, it)
            }
        }

        // お気にり曲追加ボタン
        binding.miniPlayerSaveTrackButton.setOnClickListener {
            viewModel.playingTrackIndex.value?.let {
                viewModel.changeTrackFavoriteState(it)
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

        // シークバーの位置と再生時間を同期させるThread
        Thread(Runnable {
            while (true) {
                try {
                    viewModel.mp?.let {
                        val msg = Message()
                        msg.what = it.currentPosition
                        handler.sendMessage(msg)
                        Thread.sleep(10)
                    }
                } catch (e: InterruptedException) {
                }
            }
        }).start()

        binding.miniPlayerLayout.setOnTouchListener { _, event ->
            gesture.onTouchEvent(event)
        }

        return binding.root
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

    /**
     * 再生状態を監視するイベントハンドラ
     * シークバー､再生時間の表示を同期
     */
    private val handler =
        Handler(Handler.Callback { msg ->
            val currentPosition = msg.what
            // 再生位置を更新
            binding.seekBar.progress = currentPosition
            // 経過時間ラベル更新
            binding.elapsedTimeText.text = viewModel.createTimeLabel(currentPosition)
            binding.remainingTimeText.text =
                "- ${viewModel.createTimeLabel(viewModel.mp?.duration!! - currentPosition)}"

            true
        })
}
