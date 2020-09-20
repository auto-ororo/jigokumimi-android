package com.ororo.auto.jigokumimi.ui.search

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.network.asPostMusicAroundRequest
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
    private val _searchType = MutableLiveData(Constants.Type.TRACK)
    val searchType: LiveData<Constants.Type> get() = _searchType

    /**
     *  検索距離
     */
    private val _distance = MutableLiveData(0)
    val distance: LiveData<Int> get() = _distance

    /**
     * 周辺曲情報を更新する
     */
    fun searchMusic() {
        _isLoading.value = true

        val type = searchType.value!!

        viewModelScope.launch {
            // 位置情報を取得
            val location = locationRepository.getCurrentLocation()
            Timber.d("Search Music called")
            try {
                Timber.d("緯度:${location.latitude}, 経度:${location.longitude}")

                // ユーザーIDを取得する
                val userId = authRepository.getUserId(authRepository.getSpotifyUserId())

                val postMusic = if (type == Constants.Type.TRACK) {
                    musicRepository.getMyFavoriteTracks()
                        .asPostMusicAroundRequest(userId, location)
                } else {
                    musicRepository.getMyFavoriteArtists()
                        .asPostMusicAroundRequest(userId, location)
                }
                musicRepository.postMyFavoriteMusic(postMusic)

                val place = locationRepository.getPlaceName(location.latitude, location.longitude)

                val count = musicRepository.refreshMusic(
                    type,
                    userId,
                    location,
                    place,
                    _distance.value!!
                )

                if (count == 0) {
                    showMessageDialog(getApplication<Application>().getString(R.string.no_music_error_message))
                } else {
                    _isSearchFinished.call()
                }

                Timber.d("Search Music Succeeded")

            } catch (e: Exception) {
                handleConnectException(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 検索種別を曲にセット
     */
    fun setSearchTypeToTrack() {
        _searchType.value = Constants.Type.TRACK
    }

    /**
     * 検索種別をアーティストにセット
     */
    fun setSearchTypeToArtist() {
        _searchType.value = Constants.Type.ARTIST
    }

    /**
     * スピナーで選択した距離を設定
     */
    fun setDistanceFromSelectedSpinnerString(distanceStr: String) {
        _distance.value = distanceStr.removeSuffix("m").toInt()
    }
}