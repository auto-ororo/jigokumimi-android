package com.ororo.auto.jigokumimi.viewmodels

import android.app.Activity
import android.app.Application
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.*
import com.ororo.auto.jigokumimi.database.getDatabase
import com.ororo.auto.jigokumimi.domain.Song
import com.ororo.auto.jigokumimi.repository.LocationRepository
import com.ororo.auto.jigokumimi.repository.SongsRepository
import com.ororo.auto.jigokumimi.util.Constants
import com.ororo.auto.jigokumimi.util.Constants.Companion.SPOTIFY_SDK_REDIRECT_HOST
import com.ororo.auto.jigokumimi.util.Constants.Companion.SPOTIFY_SDK_REDIRECT_SCHEME
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 周辺曲情報に関するViewのViewModel
 *
 *
 */

class SongListViewModel(application: Application, private val activity: Activity) :
    AndroidViewModel(application) {

    /*
     * 曲情報を取得､管理するRepository
     */
    private  val songsRepository = SongsRepository(getDatabase(application))

    /*
     * 位置情報を取得､管理するRepository
     */
    private val locationRepository = LocationRepository(activity)

    /*
     * 音楽再生クラス
     */
    var mp: MediaPlayer? = null

    /**
     * 取得した周辺曲情報の一覧
     */
    val songlist = songsRepository.songs

    /**
     * ネットワークエラー状態
     */
    private var _eventNetworkError = MutableLiveData<Boolean>(false)

    /**
     * ネットワークエラー状態
     */
    val eventNetworkError: LiveData<Boolean>
        get() = _eventNetworkError

    /**
     * ネットワークエラーメッセージの表示状態(Private)
     */
    private var _isNetworkErrorShown = MutableLiveData<Boolean>(false)

    /**
     * ネットワークエラーメッセージの表示状態
     */
    val isNetworkErrorShown: LiveData<Boolean>
        get() = _isNetworkErrorShown

    /*
     * 再生中の曲情報
     */
    var playingSong = MutableLiveData<Song>()

    /*
     * 再生状態
     */
    var isPlaying =  MutableLiveData<Boolean>(false)

    /*
     * 再生プレーヤーの表示状態
     */
    var isMiniPlayerShown = MutableLiveData<Boolean>(false)

    /**
     * 周辺曲情報を更新する
     */
    fun refreshSongsFromRepository() {
        viewModelScope.launch {
            try {
                songsRepository.refreshSongs()
                _eventNetworkError.value = false
                _isNetworkErrorShown.value = false
            } catch (e: Exception) {
                Timber.d(e.message)
                // Show a Toast error message and hide the progress bar.
                if (songlist.value.isNullOrEmpty())
                    _eventNetworkError.value = true
            }
        }
    }

    /**
     * 位置情報取得後､APサーバーに位置情報､及びSpotifyから取得したお気に入りの曲リストを送信する
     */
    fun postLocationAndMyFavoriteSongs() {
        viewModelScope.launch {
            try {
                val flow = locationRepository.getCurrentLocation()
                flow.collect {
                    Timber.d("緯度:${it.latitude}, 経度:${it.longitude}")
                }
            } catch (e: Exception) {
                Timber.d(e.message)
            }
        }
    }

    /**
     * Spotifyに対して認証リクエストを行う
     */
    fun getAuthenticationRequest(type: AuthorizationResponse.Type): AuthorizationRequest {
        return AuthorizationRequest.Builder(
            Constants.CLIENT_ID,
            type,
            Uri.Builder().scheme(SPOTIFY_SDK_REDIRECT_SCHEME).authority(SPOTIFY_SDK_REDIRECT_HOST).build().toString()
        )
            .setShowDialog(false)
            .setScopes(
                arrayOf(
                    "user-read-email",
                    "user-top-read",
                    "user-read-recently-played",
                    "user-library-modify",
                    "user-follow-modify",
                    "user-follow-read",
                    "user-library-read"
                )
            )
            .setCampaign("your-campaign-token")
            .build()
    }

    /**
     * ネットワークフラグをリセット
     */
    fun onNetworkErrorShown() {
        _isNetworkErrorShown.value = true
    }

    /*
     * 再生する曲を指定数だけ進めるor戻す
     */
    private fun movePlayingSong(moveIndex : Int) {
        val song : List<Song>? = songlist.value?.filter {
            it.rank == (playingSong.value?.rank!! + moveIndex)
        }

        if (song?.size!! > 0) {
            playingSong.value = song[0]
        } else {
            if (moveIndex > 0) {
                playingSong.value = songlist.value?.get(0)
            } else {
                playingSong.value = songlist.value?.get(songlist.value!!.lastIndex)
            }
        }
    }

    /*
     * 再生する曲を一つ進める
     */
    fun skipNextSong() {
        movePlayingSong(1)
        isPlaying.value = true
    }

    /*
     * 再生する曲を一つ戻す
     */
    fun skipPreviousSong() {
        movePlayingSong(-1)
        isPlaying.value = true
    }

    /*
     * 曲を再生する
     */
    fun playSong() {

        stopSong()

        // 曲のプレビューを再生
        mp = MediaPlayer.create(activity, Uri.parse(playingSong.value?.previewUrl))
        mp?.start()
    }

    /*
     * 曲を停止する
     */
    fun stopSong() {
        if (mp != null) {
            if (mp?.isPlaying!!) {
                mp?.stop()
            }
        }
    }

    /**
     * Factoryクラス
     */
    class Factory(val app: Application, val activity: Activity) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SongListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SongListViewModel(app, activity) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

}