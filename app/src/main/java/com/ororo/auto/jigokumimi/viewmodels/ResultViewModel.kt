package com.ororo.auto.jigokumimi.viewmodels

import android.app.Application
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import androidx.lifecycle.*
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.domain.Track
import com.ororo.auto.jigokumimi.repository.IMusicRepository
import com.ororo.auto.jigokumimi.repository.MusicRepository


/**
 * 検索結果画面のViewModel
 *
 *
 */
class ResultViewModel(application: Application, private val musicRepository: IMusicRepository) :
    BaseAndroidViewModel(application), MediaPlayer.OnCompletionListener {

    /*
     * 音楽再生クラス
     */
    var mp: MediaPlayer? = null

    /**
     * 取得した周辺曲情報の一覧
     */
    val tracklist = musicRepository.tracks

    /**
     * 取得した周辺アーティスト情報の一覧
     */
    val artistlist = musicRepository.artists

    /*
     * 再生中の曲情報(private)
     */
    private var _playingTrack = MutableLiveData<Track>()

    /*
     * 再生中の曲情報
     */
    val playingTrack: LiveData<Track?>
        get() = _playingTrack

    /*
     * 再生状態(Private)
     */
    private var _isPlaying = MutableLiveData(false)

    /*
     * 再生状態
     */
    val isPlaying: LiveData<Boolean>
        get() = _isPlaying

    /**
     * 再生プレーヤーの表示状態(Private)
     */
    private val _isMiniPlayerShown = MutableLiveData(false)

    /**
     * 再生プレーヤーの表示状態
     */
    val isMiniPlayerShown: LiveData<Boolean>
        get() = _isMiniPlayerShown

    /*
     * 再生する曲を指定数だけ進めるor戻す
     */
    private fun movePlayingTrack(moveIndex: Int) {

        val currentIndex = tracklist.value?.indexOf(playingTrack.value)

        currentIndex?.let {
            val sumIndex = it + moveIndex

            _playingTrack.value = when {
                sumIndex < 0 -> {
                    //指定位置が0以下の場合はリスト中最後の曲を設定
                    tracklist.value?.last()
                }
                sumIndex > tracklist.value?.lastIndex!! -> {
                    //指定位置が要素数を超える場合はリスト中最初の曲を設定
                    tracklist.value?.get(0)
                }
                else -> {
                    // それ以外は指定位置の曲を設定
                    tracklist.value?.get(sumIndex)
                }
            }
        }
    }

    /*
     * 再生する曲を一つ進める
     */
    fun skipNextTrack() {
        movePlayingTrack(1)
        playTrack()
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
                playTrack()
            }
        }
    }

    /*
     * 曲を再生する
     */
    fun playTrack() {

        try {
            stopTrack()

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
                setDataSource (playingTrack.value?.previewUrl!!)
                prepare ()
                start ()
            }
            _isPlaying.value = true

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
        _isPlaying.value = false
        mp?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
    }

    /*
     * 曲を再開する
     */
    fun resumeTrack() {
        mp?.let {
            if (!it.isPlaying) {
                it.start()
            }
        }
        _isPlaying.value = true
    }

    /**
     * 再生曲を設定する
     */
    fun setPlayingTrack(track: Track) {
        _playingTrack.value = track
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
     * プレーヤーを隠す
     */
    fun hideMiniPlayer() {
        _isMiniPlayerShown.value = false
    }

    /**
     * プレーヤーを表示する
     */
    fun showMiniPlayer() {
        _isMiniPlayerShown.value = true
    }

    /**
     * Factoryクラス
     */
    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ResultViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ResultViewModel(app, MusicRepository.getRepository(app)) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

}