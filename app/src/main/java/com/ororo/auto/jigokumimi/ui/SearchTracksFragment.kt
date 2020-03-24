package com.ororo.auto.jigokumimi.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.databinding.FragmentSearchTracksBinding
import com.ororo.auto.jigokumimi.util.Constants
import com.ororo.auto.jigokumimi.viewmodels.SearchTracksViewModel
import timber.log.Timber

/**
 * 検索画面
 */
class SearchTracksFragment : BaseFragment() {

    lateinit var viewModel: SearchTracksViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        activity?.run {
            val viewModelFactory = SearchTracksViewModel.Factory(this.application)

            viewModel = ViewModelProvider(
                viewModelStore,
                viewModelFactory
            ).get(SearchTracksViewModel::class.java)
        }

        val binding: FragmentSearchTracksBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_search_tracks,
            container,
            false
        )

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        baseInit(viewModel)

        viewModel.isSearchFinished.observe(viewLifecycleOwner) {
            if (it) onSearchFinished()
        }

        viewModel.searchType.observe(viewLifecycleOwner) {
            if (it == Constants.SearchType.TRACK) {
                binding.trackText.setBackgroundColor(ContextCompat.getColor(context!!,R.color.colorPrimary))
                binding.artistText.setBackgroundColor(ContextCompat.getColor(context!!,R.color.colorGrey))
            } else {
                binding.artistText.setBackgroundColor(ContextCompat.getColor(context!!,R.color.colorPrimary))
                binding.trackText.setBackgroundColor(ContextCompat.getColor(context!!,R.color.colorGrey))
            }
        }

        binding.searchTracksButton.setOnClickListener {
            searchTracks()
        }

        binding.trackText.setOnClickListener {
            onTrackTextTapped()
        }

        binding.artistText.setOnClickListener {
            onArtistTextTapped()
        }

        val adapter = ArrayAdapter(
                context!!,
                R.layout.custom_spinner,
                resources.getStringArray(R.array.distance_array)
        )
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown)
        binding.distanceSpinner.adapter = adapter

        setHasOptionsMenu(true)

        return binding.root
    }

    /**
     * 曲情報を検索
     */
    private fun searchTracks() {
        viewModel.searchTracks()
    }

    /**
     * 曲情報検索が完了した際の処理
     */
    private fun onSearchFinished() {
        Timber.d(this.findNavController().currentDestination.toString())
        viewModel.doneSearchTracks()
        this.findNavController()
            .navigate(SearchTracksFragmentDirections.actionSearchTracksFragmentToDetectedSongListFragment())
    }

    /**
     * 検索種別で「Track」がタップされたときの処理
      */
    private fun onTrackTextTapped() {
        viewModel.setSearchTypeToTrack()
    }

    /**
     * 検索種別で「Artist」がタップされたときの処理
     */
    private fun onArtistTextTapped() {
        viewModel.setSearchTypeToArtist()
    }
}
