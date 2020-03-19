package com.ororo.auto.jigokumimi.viewmodels

import android.app.Activity
import android.app.Application
import android.location.Location
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.lifecycle.*
import com.ororo.auto.jigokumimi.database.getDatabase
import com.ororo.auto.jigokumimi.domain.Song
import com.ororo.auto.jigokumimi.network.PostMyFavoriteSongsRequest
import com.ororo.auto.jigokumimi.repository.LocationRepository
import com.ororo.auto.jigokumimi.repository.SongsRepository
import com.ororo.auto.jigokumimi.repository.SpotifyRepository
import com.ororo.auto.jigokumimi.util.Constants
import com.ororo.auto.jigokumimi.util.Constants.Companion.SPOTIFY_SDK_REDIRECT_HOST
import com.ororo.auto.jigokumimi.util.Constants.Companion.SPOTIFY_SDK_REDIRECT_SCHEME
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.database.DatabaseSong
import com.ororo.auto.jigokumimi.network.asPostMyFavoriteSongsRequest
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


/**
 * 周辺曲情報に関するViewのViewModel
 *
 *
 */
class SongListViewModel(application: Application) :
    AndroidViewModel(application), MediaPlayer.OnCompletionListener {

    /*
     * 曲情報を取得､管理するRepository
     */
    private val songsRepository = SongsRepository(getDatabase(application))

    /*
     * 位置情報を取得､管理するRepository
     */
    private val locationRepository = LocationRepository(application)

    /**
     * spotifyデータにアクセスするRepository
     */
    private val spotifyRepository = SpotifyRepository(getDatabase(application))

    /*
     * 音楽再生クラス
     */
    var mp: MediaPlayer? = null

    /**
     * 取得した周辺曲情報の一覧
     */
    val songlist = songsRepository.songs

    /**
     * エラーメッセージダイアログの表示状態
     */
    var isErrorDialogShown = MutableLiveData<Boolean>(false)

    /**
     * エラーメッセージの内容(Private)
     */
    private var _errorMessage = MutableLiveData<String>()

    /**
     * エラーメッセージの内容
     */
    val errorMessage: MutableLiveData<String>
        get() = _errorMessage

    /*
     * 再生中の曲情報
     */
    var playingSong = MutableLiveData<Song>()

    /*
     * 再生状態
     */
    var isPlaying = MutableLiveData<Boolean>(false)

    /*
     * 再生プレーヤーの表示状態
     */
    var isMiniPlayerShown = MutableLiveData<Boolean>(false)

    /**
     * 再生中の曲ID
     */
    private var playingSongId: String = ""

    /**
     * 周辺曲情報を更新する
     */
    fun refreshSongsFromRepository() {
        viewModelScope.launch {
            try {
                // 位置情報を取得する
                val flow = locationRepository.getCurrentLocation()
                flow.collect { location: Location ->
                    Timber.d("緯度:${location.latitude}, 経度:${location.longitude}")

                    // SpotifyのユーザーIDを取得する
                    val spotifyUserId = spotifyRepository.getUserProfile().id

                    songsRepository.refreshSongs(spotifyUserId, location)

                    Timber.d("Refresh Songs Succeeded")
                }

            } catch (e: Exception) {
                val msg = when (e) {
                    is HttpException -> {
                        e.response().toString()
                    }
                    is IOException -> {
                        getApplication<Application>().getString(R.string.no_connection_error_message)
                    }
                    else -> {
                        getApplication<Application>().getString(R.string.no_connection_error_message)
                    }
                }
                showMessageDialog(msg)
            }
        }
    }

    /**
     * 位置情報取得後､APサーバーに位置情報､及びSpotifyから取得したお気に入りの曲リストを送信する
     */
    fun postLocationAndMyFavoriteSongs() {
        Timber.d("Post Location And Fav Songs Called")
        viewModelScope.launch {
            try {
                // 位置情報を取得する
                val flow = locationRepository.getCurrentLocation()
                flow.collect { location: Location ->
                    Timber.d("緯度:${location.latitude}, 経度:${location.longitude}")

                    // SpotifyのユーザーIDを取得する
                    val spotifyUserId = spotifyRepository.getUserProfile().id

                    // ユーザーのお気に入り曲一覧を取得し､リクエストを作成
                    val postSongs = songsRepository.getMyFavoriteSongs()
                        .asPostMyFavoriteSongsRequest(spotifyUserId, location)

                    // Jigokumimiにお気に入り曲リストを登録
                    songsRepository.postMyFavoriteSongs(postSongs)
                    Timber.d("Post Fav Songs Succeeded")
                }
            } catch (e: Exception) {
                val msg = when (e) {
                    is HttpException -> {
                        e.response().toString()
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

    /*
     * 再生する曲を指定数だけ進めるor戻す
     */
    private fun movePlayingSong(moveIndex: Int) {
        val song: List<Song>? = songlist.value?.filter {
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
     * 再生する曲を一つ戻すor開始位置に戻す
     */
    fun skipPreviousSong() {
        mp?.let {
            if (it.currentPosition > 1500) {
                it.seekTo(0)
            } else {
                movePlayingSong(-1)
            }
            isPlaying.value = true
        }
    }

    /*
     * 曲を再生する
     */
    fun playSong() {

        try {
            stopSong()

            // 再生する曲が変わった場合はMediaPlayerを初期化
            if (playingSongId != playingSong.value?.id) {
                mp = MediaPlayer().apply {
                    if (Build.VERSION.SDK_INT >= 21) {
                        setAudioAttributes(
                            AudioAttributes
                                .Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                    } else {
                        setAudioStreamType(AudioManager.STREAM_MUSIC)
                    }
                    setDataSource(playingSong.value?.previewUrl!!)
                    prepare()
                    start()
                    playingSongId = playingSong.value?.id!!
                }
            }

        } catch (e: Exception) {
            val msg =
                getApplication<Application>().getString(R.string.general_error_message, e.javaClass)
            showMessageDialog(msg)
        }
    }

    /*
     * 曲を停止する
     */
    fun stopSong() {
        mp?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
    }

    /**
     * シークバーを動かす
     */
    fun moveSeekBar(progress: Int) {
        mp?.seekTo(progress)
    }

    /**
     * 再生中の曲の経過時間を取得する
     */
    fun createTimeLabel(time: Int): String? {
        var timeLabel: String? = ""
        val min = time / 1000 / 60
        val sec = time / 1000 % 60
        timeLabel = "$min:"
        if (sec < 10) timeLabel += "0"
        timeLabel += sec
        return timeLabel
    }

    /**
     * 再生が完了したとき､次の曲を流す
     */
    override fun onCompletion(mp: MediaPlayer?) {
        skipNextSong()
    }

    /**
     * 例外を元にエラーメッセージを表示する
     */
    private fun showMessageDialog(message: String) {
        errorMessage.value = message
        isErrorDialogShown.value = true
    }

    /**
     * Factoryクラス
     */
    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SongListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SongListViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

}