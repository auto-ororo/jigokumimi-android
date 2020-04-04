package com.ororo.auto.jigokumimi.ui

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.databinding.FragmentSearchBinding
import com.ororo.auto.jigokumimi.util.Constants
import com.ororo.auto.jigokumimi.viewmodels.SearchViewModel
import timber.log.Timber

/**
 * 検索画面
 */
class SearchFragment : BaseFragment() {

    lateinit var viewModel: SearchViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        activity?.run {
            val viewModelFactory = SearchViewModel.Factory(this.application)

            viewModel = ViewModelProvider(
                viewModelStore,
                viewModelFactory
            ).get(SearchViewModel::class.java)
        }

        val binding: FragmentSearchBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_search,
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
                binding.trackButton.setBackgroundResource(R.drawable.shape_rounded_corners_color_primary)
                binding.artistButton.setBackgroundResource(R.drawable.shape_rounded_corners_color_grey)
            } else {
                binding.trackButton.setBackgroundResource(R.drawable.shape_rounded_corners_color_grey)
                binding.artistButton.setBackgroundResource(R.drawable.shape_rounded_corners_color_primary)
            }
        }

        binding.searchTracksButton.setOnClickListener {
            searchTracks()
        }

        binding.trackButton.setOnClickListener {
            onTrackTextTapped()
        }

        binding.artistButton.setOnClickListener {
            onArtistTextTapped()
        }

        val adapter = ArrayAdapter(
                context!!,
                R.layout.custom_spinner,
                resources.getStringArray(R.array.distance_array)
        )
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown)
        binding.distanceSpinner.adapter = adapter

        // スピナー内のアイテムが選択されたときの動作を設定
        binding.distanceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
               viewModel.setDistanceFromSelectedSpinnerString(binding.distanceSpinner.getItemAtPosition(p2).toString())
            }

        }

        setHasOptionsMenu(true)

        // ホームメニューアイコンを表示する
        activity?.actionBar?.setDisplayShowHomeEnabled(true);

        return binding.root
    }

    /**
     * 曲情報を検索
     */
    private fun searchTracks() {
        viewModel.searchMusic()
    }

    /**
     * 曲情報検索が完了した際の処理
     */
    private fun onSearchFinished() {
        Timber.d(this.findNavController().currentDestination.toString())
        viewModel.doneSearchTracks()
        this.findNavController()
            .navigate(SearchFragmentDirections.actionSearchFragmentToResultFragment(viewModel.searchType.value!!))
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
