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

        viewModel.isLoading.observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.INVISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getSearchHistories(Constants.SearchType.ARTIST)
        viewModel.getSearchHistories(Constants.SearchType.TRACK)

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

/**
 * Fragmentの切り替えを行うAdapter
 */
class HistoryTabAdapter(fm: FragmentManager, private val context: Context) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    /**
     * タブの切り替えに応じてTrack,Artistの履歴一覧を表示する
     */
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                val bundle = Bundle()
                val fragment =
                    HistoryListFragment()
                bundle.putSerializable("SearchType", Constants.SearchType.TRACK)
                fragment.arguments = bundle
                fragment
            }
            else -> {
                val bundle = Bundle()
                val fragment =
                    HistoryListFragment()
                bundle.putSerializable("SearchType", Constants.SearchType.ARTIST)
                fragment.arguments = bundle
                fragment
            }
        }
    }

    /**
     * タブのタイトル設定
     */
    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> {
                context.getString(R.string.track_text)
            }
            else -> {
                context.getString(R.string.artist_text)
            }
        }
    }

    override fun getCount(): Int {
        return 2
    }
}

/**
 * 検索履歴の一覧を表示するFragment
 */
class HistoryListFragment : Fragment(R.layout.fragment_history_list) {

    private val viewModel: HistoryViewModel by sharedViewModel()

    private val binding by dataBinding<FragmentHistoryListBinding>()

    private lateinit var viewModelAdapterHistory: HistoryTrackListAdapter

    lateinit var searchType: Constants.SearchType

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // 検索種別に応じて一覧表示する履歴を切り替え(Track or Artist)
        if (searchType == Constants.SearchType.TRACK) {
            viewModel.trackHistoryList.observe(viewLifecycleOwner, Observer { histories ->
                histories?.apply {
                    viewModelAdapterHistory.histories = histories
                }
            })
        } else {
            viewModel.artistHistoryList.observe(viewLifecycleOwner, Observer { histories ->
                histories?.apply {
                    viewModelAdapterHistory.histories = histories
                }
            })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        // 履歴がタップされたときのコールバックを設定
        val historyClickCallback =
            HistoryClick { historyIndex: Int ->
                onHistoryClicked(searchType, historyIndex)
            }

        // 履歴削除がタップされたときのコールバックを設定
        val deleteHistoryClickCallback =
            HistoryClick { historyIndex: Int ->
                onDeleteHistoryClicked(searchType, historyIndex)
            }

        // コールバックをコンストラクタに渡してアダプタを登録
        viewModelAdapterHistory =
            HistoryTrackListAdapter(
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

        searchType = arguments?.getSerializable("SearchType") as Constants.SearchType

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

/**
 * Trackを設定､表示するアダプタ
 */
class HistoryTrackListAdapter(
    private val historyClickCallback: HistoryClick,
    private val deleteHistoryClickCallback: HistoryClick
) :
    RecyclerView.Adapter<HistoryListViewHolder>() {

    /**
     * リストに表示する曲情報
     */
    var histories: List<History> = emptyList()
        set(value) {
            field = value

            // リストが更新されたことを通知
            notifyDataSetChanged()
        }

    /**
     * リストアイテムが作られたときに呼ばれる
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryListViewHolder {
        Timber.d("onCreateViewHolder called")
        val withDataBinding: HistoryItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            HistoryListViewHolder.LAYOUT,
            parent,
            false
        )
        return HistoryListViewHolder(
            withDataBinding
        )
    }

    override fun getItemCount() = histories.size

    /**
     * リストアイテムが表示されたときに呼ばれる
     */
    override fun onBindViewHolder(holderTrack: HistoryListViewHolder, position: Int) {
        Timber.d("onBindViewHolder called")
        holderTrack.viewDataBinding.also {
            it.history = histories[position]
            it.historyClickCallback = historyClickCallback
            it.deleteHistoryClickCallback = deleteHistoryClickCallback
            it.position = position
        }
    }
}

/**
 * 別ファイルで定義したHistoryItemレイアウトをつなげるViewHolder
 */
class HistoryListViewHolder(val viewDataBinding: HistoryItemBinding) :
    RecyclerView.ViewHolder(viewDataBinding.root) {
    companion object {
        @LayoutRes
        val LAYOUT = R.layout.history_item
    }
}

/**
 * 履歴をタップしたときの動作を定義
 *
 */
class HistoryClick(val block: (Int) -> Unit) {

    fun onClick(historyIndex: Int) = block(historyIndex)
}
