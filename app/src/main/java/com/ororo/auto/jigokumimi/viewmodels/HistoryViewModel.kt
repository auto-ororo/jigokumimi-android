package com.ororo.auto.jigokumimi.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.domain.History
import com.ororo.auto.jigokumimi.domain.HistoryItem
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.repository.ILocationRepository
import com.ororo.auto.jigokumimi.repository.IMusicRepository
import com.ororo.auto.jigokumimi.util.Constants
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
    private var _isSearchFinished = MutableLiveData(false)
    val isSearchFinished: LiveData<Boolean>
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
    private val _searchType = MutableLiveData(Constants.SearchType.TRACK)
    val searchType: LiveData<Constants.SearchType>
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
    fun getSearchHistories(searchType: Constants.SearchType) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val userId = authRepository.getSavedJigokumimiUserId()

                if (searchType == Constants.SearchType.TRACK) {
                    val trackHistories =
                        musicRepository.getTracksAroundSearchHistories(userId).data?.map {
                            return@map History(
                                id = it.id,
                                longitude = it.longitude,
                                latitude = it.latitude,
                                distance = it.distance,
                                createdAt = it.createdAt,
                                place = locationRepository.getPlaceName(it.latitude, it.longitude),
                                historyItems = it.tracksAroundHistories?.map { item ->
                                    return@map HistoryItem(
                                        rank = item.rank,
                                        popularity = item.popularity,
                                        spotifyItemId = item.spotifyTrackId
                                    )
                                }
                            )
                        }
                    _isLoading.value = false
                    trackHistoryList.postValue(trackHistories?.toMutableList())
                } else {
                    val artistHistories =
                        musicRepository.getArtistsAroundSearchHistories(userId).data?.map {
                            return@map History(
                                id = it.id,
                                longitude = it.longitude,
                                latitude = it.latitude,
                                distance = it.distance,
                                createdAt = it.createdAt,
                                place = locationRepository.getPlaceName(it.latitude, it.longitude),
                                historyItems = it.artistsAroundHistories?.map { item ->
                                    return@map HistoryItem(
                                        rank = item.rank,
                                        popularity = item.popularity,
                                        spotifyItemId = item.spotifyArtistId
                                    )
                                }
                            )
                        }
                    _isLoading.value = false
                    artistHistoryList.postValue(artistHistories?.toMutableList())
                }

            } catch (e: Exception) {
                handleConnectException(e)
                _isLoading.value = false
            }
        }
    }

    /**
     * 検索履歴を削除する
     */
    fun deleteHistory(searchType: Constants.SearchType, historyIndex: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {

                if (searchType == Constants.SearchType.TRACK) {
                    trackHistoryList.value?.get(historyIndex)?.let { history ->
                        musicRepository.deleteTracksAroundSearchHistories(history.id)
                        val msg = getApplication<Application>().getString(
                            R.string.remove_history_message,
                            history.createdAt
                        )

                        showSnackbar(msg)
                        _deleteDataIndex.postValue(historyIndex)
                        getSearchHistories(searchType)
                    }
                } else {
                    artistHistoryList.value?.get(historyIndex)?.let { history ->
                        musicRepository.deleteArtistsAroundSearchHistories(history.id)
                        val msg = getApplication<Application>().getString(
                            R.string.remove_history_message,
                            history.createdAt
                        )

                        showSnackbar(msg)
                        _deleteDataIndex.postValue(historyIndex)
                        getSearchHistories(searchType)
                    }
                }
                _isLoading.value = false

            } catch (e: Exception) {
                handleConnectException(e)
                _isLoading.value = false
            }

        }
    }

    /**
     * 検索履歴の詳細を取得する
     */
    fun searchHistoryDetails(searchType: Constants.SearchType, historyIndex: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _searchType.postValue(searchType)
                if (searchType == Constants.SearchType.TRACK) {
                    trackHistoryList.value?.get(historyIndex)?.let { history ->
                        musicRepository.refreshTracksFromHistory(history)
                        _distance.value = history.distance
                        _searchDateTime.value = history.createdAt
                        _isLoading.value = false
                        _isSearchFinished.postValue(true)
                    }
                } else {
                    artistHistoryList.value?.get(historyIndex)?.let { history ->
                        musicRepository.refreshArtistsFromHistory(history)
                        _distance.value = history.distance
                        _searchDateTime.value = history.createdAt
                        _isLoading.value = false
                        _isSearchFinished.postValue(true)
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
                handleConnectException(e)
            }
        }
    }

    /**
     * 検索完了フラグをリセット
     */
    fun doneSearchMusic() {
        _isSearchFinished.postValue(false)
    }
}