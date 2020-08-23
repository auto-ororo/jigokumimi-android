package com.ororo.auto.jigokumimi.ui.search

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.databinding.FragmentSearchBinding
import com.ororo.auto.jigokumimi.ui.common.BaseFragment
import com.ororo.auto.jigokumimi.util.Constants
import com.ororo.auto.jigokumimi.util.Util
import com.ororo.auto.jigokumimi.util.dataBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

/**
 * 検索画面
 */
class SearchFragment : BaseFragment(R.layout.fragment_search) {

    private val viewModel: SearchViewModel by viewModel()

    private val binding by dataBinding<FragmentSearchBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        baseInit(viewModel)

        viewModel.isSearchFinished.observe(viewLifecycleOwner) {
            onSearchFinished()
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
            requireContext(),
            R.layout.custom_spinner,
            resources.getStringArray(R.array.distance_array)
        )
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown)
        binding.distanceSpinner.adapter = adapter

        // スピナー内のアイテムが選択されたときの動作を設定
        binding.distanceSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {
                }

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    viewModel.setDistanceFromSelectedSpinnerString(
                        binding.distanceSpinner.getItemAtPosition(
                            p2
                        ).toString()
                    )
                }

            }

        setHasOptionsMenu(true)

        // ホームメニューアイコンを表示する
        activity?.actionBar?.setDisplayShowHomeEnabled(true);


        // タイトル設定
        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity).supportActionBar?.run {
                show()
                val titleStr =
                    "${context?.getString(R.string.title_search)} ${if (viewModel.isDemo()) context?.getString(
                        R.string.title_demo
                    ) else ""}"
                title = titleStr
            }
        }
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
        this.findNavController()
            .navigate(
                SearchFragmentDirections.actionSearchFragmentToResultFragment(
                    viewModel.searchType.value!!,
                    viewModel.distance.value!!,
                    Util.getNowDate()
                )
            )
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
