package com.ororo.auto.jigokumimi.ui.history

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.domain.History
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.repository.ILocationRepository
import com.ororo.auto.jigokumimi.repository.IMusicRepository
import com.ororo.auto.jigokumimi.ui.common.BaseAndroidViewModel
import com.ororo.auto.jigokumimi.util.Constants
import com.ororo.auto.jigokumimi.util.SingleLiveEvent
import kotlinx.coroutines.launch

class HistoryViewModel(
    application: Application,
    private val musicRepository: IMusicRepository,
    authRepository: IAuthRepository,
    private val locationRepository: ILocationRepository
) :
    BaseAndroidViewModel(application, authRepository) {

    /**
     * 曲検索履歴情報の一覧
     */
    val artistHistoryList = MutableLiveData<MutableList<History>>()

    /**
     * アーティスト検索履歴情報の一覧
     */
    val trackHistoryList = MutableLiveData<MutableList<History>>()

    /**
     *  検索完了フラグ
     */
    private var _isSearchFinished = SingleLiveEvent<Unit>()
    val isSearchFinished: LiveData<Unit>
        get() = _isSearchFinished

    /**
     * 削除されたデータ位置
     */
    private val _deleteDataIndex = MutableLiveData<Int>()
    val deleteDataIndex: LiveData<Int>
        get() = _deleteDataIndex

    /**
     * 検索種別
     */
    private val _searchType = MutableLiveData(Constants.Type.TRACK)
    val searchType: LiveData<Constants.Type>
        get() = _searchType

    /**
     *  検索距離
     */
    private val _distance = MutableLiveData(0)
    val distance: LiveData<Int>
        get() = _distance

    /**
     *  検索日時
     */
    private val _searchDateTime = MutableLiveData("")
    val searchDateTime: LiveData<String>
        get() = _searchDateTime

    /**
     * 検索履歴を取得する
     */
    fun getSearchHistories(searchType: Constants.Type) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val userId = authRepository.getUserId()

                val list = musicRepository.getSearchHistories(searchType, userId)

                if (searchType == Constants.Type.TRACK) {
                    trackHistoryList.value = list.toMutableList()
                } else {
                    artistHistoryList.value = list.toMutableList()
                }

            } catch (e: Exception) {
                handleConnectException(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 検索履歴を削除する
     */
    fun deleteHistory(searchType: Constants.Type, historyIndex: Int) {
        if (isLoading.value == true) return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val userId = authRepository.getUserId()

                trackHistoryList.value?.get(historyIndex)?.let { history ->
                    musicRepository.deleteSearchHistory(searchType, userId, history.id)
                    val msg = getApplication<Application>().getString(
                        R.string.remove_history_message,
                        history.createdAt
                    )

                    showSnackbar(msg)
                    _deleteDataIndex.value = historyIndex
                    getSearchHistories(searchType)
                }
            } catch (e: Exception) {
                handleConnectException(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 検索履歴の詳細を取得する
     */
    fun searchHistoryDetails(searchType: Constants.Type, historyIndex: Int) {
        if (isLoading.value == true) return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _searchType.value = searchType
                trackHistoryList.value?.get(historyIndex)?.let { history ->
                    musicRepository.refreshMusicFromHistory(searchType, history)
                    _distance.value = history.distance
                    _searchDateTime.value = history.createdAt
                    _isLoading.value = false
                    _isSearchFinished.call()
                }
            } catch (e: Exception) {
                handleConnectException(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}