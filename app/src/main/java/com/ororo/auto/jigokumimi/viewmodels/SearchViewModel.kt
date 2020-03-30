package com.ororo.auto.jigokumimi.viewmodels

import android.app.Application
import android.location.Location
import androidx.lifecycle.*
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.network.asPostMyFavoriteArtistsRequest
import com.ororo.auto.jigokumimi.network.asPostMyFavoriteTracksRequest
import com.ororo.auto.jigokumimi.repository.*
import com.ororo.auto.jigokumimi.util.Constants
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

class SearchViewModel(
    application: Application,
    private val authRepository: IAuthRepository,
    private val musicRepository: IMusicRepository,
    private val locationRepository: ILocationRepository
) : BaseAndroidViewModel(application) {

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
     * 検索種別
     */
    private val _searchType = MutableLiveData(Constants.SearchType.TRACK)

    /**
     *  検索完了フラグ
     */
    val searchType: LiveData<Constants.SearchType>
        get() = _searchType

    /**
     *  検索距離(Private)
     */
    private val _distance = MutableLiveData(0)

    /**
     *  検索距離
     */
    val distance: LiveData<Int>
        get() = _distance

    /**
     * 周辺曲情報を更新する
     */
    fun searchMusic() {
        viewModelScope.launch {
            Timber.d("Search Music called")
            try {
                // 位置情報を取得する
                val flow = locationRepository.getCurrentLocation()
                flow.collect { location: Location ->
                    Timber.d("緯度:${location.latitude}, 経度:${location.longitude}")

                    // SpotifyのユーザーIDを取得する
                    val spotifyUserId = authRepository.getSpotifyUserProfile().id

                    if (searchType.value == Constants.SearchType.TRACK) {
                        // 検索種別が曲の場合

                        // ユーザーのお気に入り曲一覧を取得し､リクエストを作成
                        val postTracks = musicRepository.getMyFavoriteTracks()
                            .asPostMyFavoriteTracksRequest(spotifyUserId, location)

                        // Jigokumimiにお気に入り曲リストを登録
                        musicRepository.postMyFavoriteTracks(postTracks)

                        // 周りのJigokumimiユーザーのお気に入り曲を取得
                        musicRepository.refreshTracks(spotifyUserId, location, _distance.value!!)

                    } else {
                        // 検索種別がアーティストの場合

                        // ユーザーのお気に入りアーティスト一覧を取得し､リクエストを作成
                        val postArtists = musicRepository.getMyFavoriteArtists()
                            .asPostMyFavoriteArtistsRequest(spotifyUserId, location)

                        // Jigokumimiにお気に入り曲リストを登録
                        musicRepository.postMyFavoriteArtists(postArtists)

                        // 周りのJigokumimiユーザーのお気に入り曲を取得
                        musicRepository.refreshArtists(spotifyUserId, location, _distance.value!!)
                    }


                    // 検索完了フラグをON
                    _isSearchFinished.postValue(true)

                    Timber.d("Search Music Succeeded")
                }

            } catch (e: Exception) {
                _isSearchFinished.postValue(false)
                val msg = when (e) {
                    is HttpException -> {
                        if (e.code() == 401) {
                            _isTokenExpired.postValue(true)
                            getApplication<Application>().getString(R.string.token_expired_error_message)
                        } else {
                            getMessageFromHttpException(e)
                        }
                    }
                    is IOException -> {
                        getApplication<Application>().getString(R.string.no_connection_error_message)
                    }
                    else -> {
                        getApplication<Application>().getString(
                            R.string.general_error_message,
                            e.javaClass
                        )
                    }
                }
                showMessageDialog(msg)
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
                    AuthRepository.getRepository(app),
                    MusicRepository.getRepository(app),
                    LocationRepository.getRepository(app)
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}