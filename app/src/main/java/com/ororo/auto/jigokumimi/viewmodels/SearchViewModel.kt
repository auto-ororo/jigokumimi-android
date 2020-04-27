package com.ororo.auto.jigokumimi.viewmodels

import android.app.Application
import android.location.Location
import androidx.lifecycle.*
import com.ororo.auto.jigokumimi.JigokumimiApplication
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.network.asPostMyFavoriteArtistsRequest
import com.ororo.auto.jigokumimi.network.asPostMyFavoriteTracksRequest
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.repository.ILocationRepository
import com.ororo.auto.jigokumimi.repository.IMusicRepository
import com.ororo.auto.jigokumimi.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class SearchViewModel(
    application: Application,
    authRepository: IAuthRepository,
    private val musicRepository: IMusicRepository,
    private val locationRepository: ILocationRepository
) : BaseAndroidViewModel(application, authRepository) {

    /**
     *  検索完了フラグ
     */
    private var _isSearchFinished = MutableLiveData(false)
    val isSearchFinished: LiveData<Boolean>
        get() = _isSearchFinished

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
     * 周辺曲情報を更新する
     */
    fun searchMusic() {

        // 位置情報を取得する
        viewModelScope.launch {
            val flow = locationRepository.getCurrentLocation()
            flow.collect { location: Location ->
                viewModelScope.launch(Dispatchers.IO) {
                    Timber.d("Search Music called")
                    try {
                        Timber.d("緯度:${location.latitude}, 経度:${location.longitude}")

                        // JigokumimiのユーザーIDを取得する
                        val jigokumimiUserId = authRepository.getSavedJigokumimiUserId()

                        if (searchType.value == Constants.SearchType.TRACK) {
                            // 検索種別が曲の場合

                            // ユーザーのお気に入り曲一覧を取得し､リクエストを作成
                            val postTracks = musicRepository.getMyFavoriteTracks()
                                .asPostMyFavoriteTracksRequest(jigokumimiUserId, location)

                            // Jigokumimiにお気に入り曲リストを登録
                            musicRepository.postMyFavoriteTracks(postTracks)

                            // 周りのJigokumimiユーザーのお気に入り曲を取得
                            musicRepository.refreshTracks(
                                jigokumimiUserId,
                                location,
                                _distance.value!!
                            )

                            // 取得件数が0件の場合はエラー表示
                            if (musicRepository.tracks.value?.size == 0) {
                                showMessageDialog(getApplication<Application>().getString(R.string.no_tracks_error_message))
                            } else {
                                // 検索完了フラグをON
                                _isSearchFinished.postValue(true)
                            }

                        } else {
                            // 検索種別がアーティストの場合

                            // ユーザーのお気に入りアーティスト一覧を取得し､リクエストを作成
                            val postArtists = musicRepository.getMyFavoriteArtists()
                                .asPostMyFavoriteArtistsRequest(jigokumimiUserId, location)

                            // Jigokumimiにお気に入り曲リストを登録
                            musicRepository.postMyFavoriteArtists(postArtists)

                            // 周りのJigokumimiユーザーのお気に入り曲を取得
                            musicRepository.refreshArtists(
                                jigokumimiUserId,
                                location,
                                _distance.value!!
                            )

                            // 取得件数が0件の場合はエラー表示
                            if (musicRepository.artists.value?.size == 0) {
                                showMessageDialog(getApplication<Application>().getString(R.string.no_artists_error_message))
                            } else {
                                // 検索完了フラグをON
                                _isSearchFinished.postValue(true)
                            }
                        }
                        Timber.d("Search Music Succeeded")

                    } catch (e: Exception) {
                        handleConnectException(e)
                    }

                }
            }
        }
    }

    /**
     * 検索完了フラグをリセット
     */
    fun doneSearchTracks() {
        _isSearchFinished.postValue(false)
    }

    /**
     * 検索種別を曲にセット
     */
    fun setSearchTypeToTrack() {
        _searchType.value = Constants.SearchType.TRACK
    }

    /**
     * 検索種別をアーティストにセット
     */
    fun setSearchTypeToArtist() {
        _searchType.value = Constants.SearchType.ARTIST
    }

    /**
     * スピナーで選択した距離を設定
     */
    fun setDistanceFromSelectedSpinnerString(distanceStr: String) {
        _distance.value = distanceStr.removeSuffix("m").toInt()
    }

    /**
     * Factoryクラス
     */
    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SearchViewModel(
                    app,
                    (app.applicationContext as JigokumimiApplication).authRepository,
                    (app.applicationContext as JigokumimiApplication).musicRepository,
                    (app.applicationContext as JigokumimiApplication).locationRepository
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}