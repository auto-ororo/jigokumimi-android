package com.ororo.auto.jigokumimi.ui

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View.GONE
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.databinding.FragmentMiniPlayerBinding
import com.ororo.auto.jigokumimi.viewmodels.TrackListViewModel
import kotlinx.android.synthetic.main.fragment_mini_player.*


/**
 * 音楽再生プレーヤー
 */
class MiniPlayerFragment : Fragment() {

    lateinit var viewModel: TrackListViewModel

    lateinit var binding: FragmentMiniPlayerBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        activity?.run {
            val viewModelFactory = TrackListViewModel.Factory(this.application)

            viewModel = ViewModelProvider(
                viewModelStore,
                viewModelFactory
            ).get(TrackListViewModel::class.java)
        }

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_mini_player,
            container,
            false
        )

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        viewModel.isPlaying.observe(
            viewLifecycleOwner,
            Observer<Boolean> { isPlaying ->
                if (isPlaying) {
                    viewModel.isMiniPlayerShown.value = true
                    viewModel.playTrack()
                    seekBar.max = viewModel.mp?.duration!!
                    viewModel.mp?.setOnCompletionListener(viewModel)
                    binding.queueButton.setImageResource(R.drawable.ic_pause)
                } else {
                    viewModel.stopTrack()
                    binding.queueButton.setImageResource(R.drawable.ic_play_arrow)
                }
            })

        viewModel.isMiniPlayerShown.observe(
            viewLifecycleOwner,
            Observer<Boolean> { isMiniPlayerShown ->
                if (isMiniPlayerShown) {
                    binding.miniPlayerLayout.visibility = View.VISIBLE
                } else {
                    binding.miniPlayerLayout.visibility = View.GONE
                }
            }
        )

        binding.queueButton.setOnClickListener {
            viewModel.isPlaying.value = !viewModel.isPlaying.value!!
        }

        binding.nextButton.setOnClickListener {
            viewModel.skipNextTrack()
        }

        binding.previousButton.setOnClickListener {
            viewModel.skipPreviousTrack()
        }

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

        // Thread (seekBarを更新する)
        Thread(Runnable {
            while (true) {
                try {
                    if (viewModel.mp != null) {
                        val msg = Message()
                        msg.what = viewModel.mp!!.currentPosition
                        handler.sendMessage(msg)
                        Thread.sleep(10)
                    }
                } catch (e: InterruptedException) {
                }
            }
        }).start();

        binding.miniPlayerLayout.setOnTouchListener { _, event ->
            gesture.onTouchEvent(event)
        }

        return binding.root
    }

    /**
     * 上から下にスワイプした際にプレーヤーを隠すジェスチャクラス
     * OnCreateViewで作らないと動作しない可能性
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
                        binding.miniPlayerLayout.visibility = GONE
                    }
                } catch (e: Exception) { // nothing
                }
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        })

    /**
     * 再生状態を監視すイベントハンドラ
     * シークバー､再生時間の表示を更新する
     */
    private val handler =
        Handler(Handler.Callback { msg ->
            val currentPosition = msg.what
            // 再生位置を更新
            seekBar.progress = currentPosition
            // 経過時間ラベル更新
            elapsedTimeText.text = viewModel.createTimeLabel(currentPosition)
            remainingTimeText.text =
                "- ${viewModel.createTimeLabel(viewModel.mp?.duration!! - currentPosition)}"

            true
        })
}
