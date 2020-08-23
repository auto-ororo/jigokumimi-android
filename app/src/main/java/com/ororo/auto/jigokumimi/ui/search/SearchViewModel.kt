package com.ororo.auto.jigokumimi.ui.search

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.network.asPostMyFavoriteArtistsRequest
import com.ororo.auto.jigokumimi.network.asPostMyFavoriteTracksRequest
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.repository.ILocationRepository
import com.ororo.auto.jigokumimi.repository.IMusicRepository
import com.ororo.auto.jigokumimi.ui.common.BaseAndroidViewModel
import com.ororo.auto.jigokumimi.util.Constants
import com.ororo.auto.jigokumimi.util.SingleLiveEvent
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
    private var _isSearchFinished = SingleLiveEvent<Unit>()
    val isSearchFinished: LiveData<Unit>
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
        _isLoading.value = true

        viewModelScope.launch {
            // 位置情報を取得する
            val location = locationRepository.getCurrentLocation()
            Timber.d("Search Music called")
            try {
                Timber.d("緯度:${location.latitude}, 経度:${location.longitude}")

                // JigokumimiのユーザーIDを取得する
                val jigokumimiUserId = authRepository.getSavedJigokumimiUserId()

                if (searchType.value == Constants.SearchType.TRACK) {
                    // 検索種別が曲の場合

                    // 前回のお気に入り曲送信日時から一定時間経過している場合、ユーザーのお気に入り曲一覧を取得してリクエストを作成
                    if (musicRepository.shouldPostFavoriteTracks()) {

                        val postTracks = musicRepository.getMyFavoriteTracks()
                            .asPostMyFavoriteTracksRequest(jigokumimiUserId, location)

                        // Jigokumimiにお気に入り曲リストを登録
                        musicRepository.postMyFavoriteTracks(postTracks)
                    }

                    // 周りのJigokumimiユーザーのお気に入り曲を取得
                    musicRepository.refreshTracks(
                        jigokumimiUserId,
                        location,
                        _distance.value!!
                    )
                    _isLoading.value = false

                    // 取得件数が0件の場合はエラー表示
                    if (musicRepository.tracks.value?.size == 0) {
                        showMessageDialog(getApplication<Application>().getString(R.string.no_tracks_error_message))
                    } else {
                        // 検索完了
                        _isSearchFinished.call()
                    }

                } else {
                    // 検索種別がアーティストの場合

                    // 前回のお気に入りアーティスト送信日時から一定時間経過している場合、ユーザーのお気に入りアーティスト一覧を取得してリクエストを作成
                    if (musicRepository.shouldPostFavoriteArtists()) {
                        val postArtists = musicRepository.getMyFavoriteArtists()
                            .asPostMyFavoriteArtistsRequest(jigokumimiUserId, location)

                        // Jigokumimiにお気に入り曲リストを登録
                        musicRepository.postMyFavoriteArtists(postArtists)
                    }
                    // 周りのJigokumimiユーザーのお気に入り曲を取得
                    musicRepository.refreshArtists(
                        jigokumimiUserId,
                        location,
                        _distance.value!!
                    )
                    _isLoading.value = false

                    // 取得件数が0件の場合はエラー表示
                    if (musicRepository.artists.value?.size == 0) {
                        showMessageDialog(getApplication<Application>().getString(R.string.no_artists_error_message))
                    } else {
                        // 検索完了
                        _isSearchFinished.call()
                    }
                }
                Timber.d("Search Music Succeeded")

            } catch (e: Exception) {
                handleConnectException(e)
            }
        }
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
}