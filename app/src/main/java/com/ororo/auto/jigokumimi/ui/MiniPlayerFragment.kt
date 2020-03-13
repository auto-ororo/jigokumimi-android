package com.ororo.auto.jigokumimi.ui

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.databinding.FragmentMiniPlayerBinding
import com.ororo.auto.jigokumimi.viewmodels.SongListViewModel

/**
 * 音楽再生プレーヤー
 */
class MiniPlayerFragment : Fragment()  {


    lateinit var viewModel: SongListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        activity?.run {
            val viewModelFactory = SongListViewModel.Factory(this.application, this)

            viewModel = ViewModelProvider(viewModelStore, viewModelFactory).get(SongListViewModel::class.java)
        }


        val binding: FragmentMiniPlayerBinding = DataBindingUtil.inflate(
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
                    viewModel.playSong()
                    binding.queueButton.setImageResource(R.drawable.ic_pause)
                } else {
                    viewModel.stopSong()
                    binding.queueButton.setImageResource(R.drawable.ic_play_arrow)
                }
            })

        viewModel.isMiniPlayerShown.observe(
            viewLifecycleOwner,
            Observer<Boolean> {isMiniPlayerShown ->
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
            viewModel.skipNextSong()
        }

        binding.previousButton.setOnClickListener {
            viewModel.skipPreviousSong()
        }

        return binding.root
    }


}
