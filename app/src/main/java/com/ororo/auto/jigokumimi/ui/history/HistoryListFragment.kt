package com.ororo.auto.jigokumimi.ui.history

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.databinding.FragmentHistoryListBinding
import com.ororo.auto.jigokumimi.ui.common.ItemClick
import com.ororo.auto.jigokumimi.util.Constants
import com.ororo.auto.jigokumimi.util.dataBinding
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

/**
 * 検索履歴の一覧を表示するFragment
 */
class HistoryListFragment : Fragment(R.layout.fragment_history_list) {

    private val viewModel: HistoryViewModel by sharedViewModel()

    private val binding by dataBinding<FragmentHistoryListBinding>()

    private lateinit var viewModelAdapterHistory: HistoryListAdapter

    lateinit var searchType: Constants.SearchType

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // 検索種別に応じて一覧表示する履歴を切り替え(Track or Artist)
        if (searchType == Constants.SearchType.TRACK) {
            viewModel.trackHistoryList.observe(viewLifecycleOwner) { histories ->
                histories?.apply {
                    viewModelAdapterHistory.histories = histories
                }
            }
        } else {
            viewModel.artistHistoryList.observe(viewLifecycleOwner) { histories ->
                histories?.apply {
                    viewModelAdapterHistory.histories = histories
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        searchType = arguments?.getSerializable("SearchType") as Constants.SearchType

        viewModel.getSearchHistories(searchType)

        // 履歴がタップされたときのコールバックを設定
        val historyClickCallback =
            ItemClick { historyIndex: Int ->
                onHistoryClicked(searchType, historyIndex)
            }

        // 履歴削除がタップされたときのコールバックを設定
        val deleteHistoryClickCallback =
            ItemClick { historyIndex: Int ->
                onDeleteHistoryClicked(searchType, historyIndex)
            }

        // コールバックをコンストラクタに渡してアダプタを登録
        viewModelAdapterHistory =
            HistoryListAdapter(
                historyClickCallback,
                deleteHistoryClickCallback
            )
        binding.recyclerView.adapter = viewModelAdapterHistory

        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        // データが変更された際にAdapterに検知
        viewModel.deleteDataIndex.observe(viewLifecycleOwner) {
            viewModelAdapterHistory.notifyItemRemoved(it)
        }

        // アイテム間に枠線を設定
        val dividerItemDecoration =
            DividerItemDecoration(binding.recyclerView.context, DividerItemDecoration.VERTICAL)
        dividerItemDecoration.setDrawable(activity?.applicationContext?.getDrawable(R.drawable.divider)!!)
        binding.recyclerView.addItemDecoration(dividerItemDecoration)

    }

    /**
     * 履歴をタップした時の処理
     */
    private fun onHistoryClicked(searchType: Constants.SearchType, historyIndex: Int) {
        viewModel.searchHistoryDetails(searchType, historyIndex)
    }

    /**
     * 履歴削除ボタンをタップした時の処理
     */
    private fun onDeleteHistoryClicked(searchType: Constants.SearchType, historyIndex: Int) {
        viewModel.deleteHistory(searchType, historyIndex)
    }
}
