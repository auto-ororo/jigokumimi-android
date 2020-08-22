package com.ororo.auto.jigokumimi.ui.history

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.databinding.FragmentHistoryBinding
import com.ororo.auto.jigokumimi.databinding.FragmentHistoryListBinding
import com.ororo.auto.jigokumimi.databinding.HistoryItemBinding
import com.ororo.auto.jigokumimi.domain.History
import com.ororo.auto.jigokumimi.ui.common.BaseFragment
import com.ororo.auto.jigokumimi.util.Constants
import com.ororo.auto.jigokumimi.util.dataBinding
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber


/**
 * 検索履歴画面
 */
class HistoryFragment : BaseFragment(R.layout.fragment_history) {

    private val viewModel: HistoryViewModel by sharedViewModel()

    private val binding by dataBinding<FragmentHistoryBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        // ViewPager､TabLayoutを設定
        binding.pager.adapter = HistoryTabAdapter(
            childFragmentManager,
            requireActivity().applicationContext
        )
        binding.tabLayout.setupWithViewPager(binding.pager)


        // タイトル設定
        (activity as AppCompatActivity).supportActionBar?.run {
            title = context?.getString(R.string.title_history)
        }

        viewModel.isSearchFinished.observe(viewLifecycleOwner) {
            if (it) onSearchFinished()
        }

        // タイトル設定
        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity).supportActionBar?.run {
                val titleStr =
                    "${context?.getString(R.string.title_history)} ${if (viewModel.isDemo()) context?.getString(
                        R.string.title_demo
                    ) else ""}"
                title = titleStr
            }
        }

        baseInit(viewModel)
    }

    override fun onResume() {
        super.onResume()

        Timber.d("HistoryFragment onResume Called")
    }

    /**
     * 曲情報検索が完了した際の処理
     * 結果画面に遷移
     */
    private fun onSearchFinished() {
        viewModel.doneSearchMusic()

        // 結果画面に遷移する
        this.findNavController()
            .navigate(
                HistoryFragmentDirections.actionHistoryFragmentToResultFragment(
                    viewModel.searchType.value!!,
                    viewModel.distance.value!!,
                    viewModel.searchDateTime.value!!
                )
            )
    }
}


