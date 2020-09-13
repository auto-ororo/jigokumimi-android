package com.ororo.auto.jigokumimi.ui.history

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.util.Constants

/**
 * Fragmentの切り替えを行うAdapter
 */
class HistoryTabAdapter(fm: FragmentManager, private val context: Context) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    /**
     * タブの切り替えに応じてTrack,Artistの履歴一覧を表示する
     */
    override fun getItem(position: Int): Fragment {

        fun createFragmentFromSearchType(searchType: Constants.Type) : Fragment {
            val bundle = Bundle()
            val fragment =
                HistoryListFragment()
            bundle.putSerializable("SearchType", searchType)
            fragment.arguments = bundle
            return fragment
        }

        return when (position) {
            0 -> {
                createFragmentFromSearchType(Constants.Type.TRACK)
            }
            else -> {
                createFragmentFromSearchType(Constants.Type.ARTIST)
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
