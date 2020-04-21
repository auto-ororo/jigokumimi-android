package com.ororo.auto.jigokumimi.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.ororo.auto.jigokumimi.JigokumimiApplication
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
     *  検索完了フラグ(Private)
     */
    private var _isSearchFinished = MutableLiveData(false)

    /**
     *  検索完了フラグ
     */
    val isSearchFinished: LiveData<Boolean>
        get() = _isSearchFinished

    /**
     * 削除されたデータ位置(Private)
     */
    private val _deleteDataIndex = MutableLiveData<Int>()

    /**
     * 削除されたデータ位置
     */
    val deleteDataIndex: LiveData<Int>
        get() = _deleteDataIndex

    /**
     * 検索履歴を取得する
     */
    fun getSearchHistories(searchType: Constants.SearchType) {
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
                    artistHistoryList.postValue(artistHistories?.toMutableList() )
                }

            } catch (e: Exception) {
                handleConnectException(e)
            }
        }
    }

    /**
     * 検索履歴を削除する
     */
    fun deleteHistory(searchType: Constants.SearchType, historyIndex: Int) {
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

            } catch (e: Exception) {
                handleConnectException(e)
            }

        }
    }

    /**
     * 検索履歴の詳細を取得する
     */
    fun searchHistoryDetails(searchType: Constants.SearchType, historyIndex: Int) {
        viewModelScope.launch {
            try {

                if (searchType == Constants.SearchType.TRACK) {
                    trackHistoryList.value?.get(historyIndex)?.let { history ->
                        musicRepository.refreshTracksFromHistory(history)
                        _isSearchFinished.postValue(true)
                    }
                } else {
                    artistHistoryList.value?.get(historyIndex)?.let { history ->
                        musicRepository.refreshArtistsFromHistory(history)
                        _isSearchFinished.postValue(true)
                    }
                }
            } catch (e: Exception) {
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

    /**
     * Factoryクラス
     */
    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HistoryViewModel(
                    app,
                    (app.applicationContext as JigokumimiApplication).musicRepository,
                    (app.applicationContext as JigokumimiApplication).authRepository,
                    (app.applicationContext as JigokumimiApplication).locationRepository
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}