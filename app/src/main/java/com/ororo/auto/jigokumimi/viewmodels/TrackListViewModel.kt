package com.ororo.auto.jigokumimi.viewmodels

import android.app.Application
import android.location.Location
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.database.getDatabase
import com.ororo.auto.jigokumimi.domain.Track
import com.ororo.auto.jigokumimi.repository.LocationRepository
import com.ororo.auto.jigokumimi.repository.TracksRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException


/**
 * 周辺曲情報に関するViewのViewModel
 *
 *
 */
class TrackListViewModel(application: Application) :
    BaseAndroidViewModel(application), MediaPlayer.OnCompletionListener {

    /*
     * 曲情報を取得､管理するRepository
     */
    private val tracksRepository = TracksRepository(
        getDatabase(application),
        PreferenceManager.getDefaultSharedPreferences(application.applicationContext)
    )

    /*
     * 位置情報を取得､管理するRepository
     */
    private val locationRepository = LocationRepository(application)

    /*
     * 音楽再生クラス
     */
    var mp: MediaPlayer? = null

    /**
     * 取得した周辺曲情報の一覧
     */
    val tracklist = tracksRepository.tracks

    /*
     * 再生中の曲情報
     */
    var playingTrack = MutableLiveData<Track>()

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
    private var playingTrackId: String = ""

    /**
     * 周辺曲情報を更新する
     */
    fun refreshTracksFromRepository() {
        viewModelScope.launch() {
            try {
                // 位置情報を取得する
                val flow = locationRepository.getCurrentLocation()
                flow.collect { location: Location ->
                    Timber.d("緯度:${location.latitude}, 経度:${location.longitude}")

                    // SpotifyのユーザーIDを取得する
                    val spotifyUserId = authRepository.getSpotifyUserProfile().id

                    tracksRepository.refreshTracks(spotifyUserId, location)

                    Timber.d("Refresh Tracks Succeeded")
                }

            } catch (e: Exception) {
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

    /*
     * 再生する曲を指定数だけ進めるor戻す
     */
    private fun movePlayingTrack(moveIndex: Int) {
        val track: List<Track>? = tracklist.value?.filter {
            it.rank == (playingTrack.value?.rank!! + moveIndex)
        }

        if (track?.size!! > 0) {
            playingTrack.value = track[0]
        } else {
            if (moveIndex > 0) {
                playingTrack.value = tracklist.value?.get(0)
            } else {
                playingTrack.value = tracklist.value?.get(tracklist.value!!.lastIndex)
            }
        }
    }

    /*
     * 再生する曲を一つ進める
     */
    fun skipNextTrack() {
        movePlayingTrack(1)
        isPlaying.value = true
    }

    /*
     * 再生する曲を一つ戻すor開始位置に戻す
     */
    fun skipPreviousTrack() {
        mp?.let {
            if (it.currentPosition > 1500) {
                it.seekTo(0)
            } else {
                movePlayingTrack(-1)
            }
            isPlaying.value = true
        }
    }

    /*
     * 曲を再生する
     */
    fun playTrack() {

        try {
            stopTrack()

            // 再生する曲が変わった場合はMediaPlayerを初期化
            if (playingTrackId != playingTrack.value?.id) {
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
                    setDataSource(playingTrack.value?.previewUrl!!)
                    prepare()
                    start()
                    playingTrackId = playingTrack.value?.id!!
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
    fun stopTrack() {
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
        skipNextTrack()
    }

    /**
     * Factoryクラス
     */
    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TrackListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TrackListViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

}