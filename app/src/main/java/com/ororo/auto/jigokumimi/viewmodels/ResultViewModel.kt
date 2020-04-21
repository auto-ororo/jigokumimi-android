package com.ororo.auto.jigokumimi.viewmodels

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.lifecycle.*
import com.ororo.auto.jigokumimi.JigokumimiApplication
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.repository.AuthRepository
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.repository.IMusicRepository
import com.ororo.auto.jigokumimi.util.Constants
import kotlinx.coroutines.launch


/**
 * 検索結果画面のViewModel
 *
 *
 */
class ResultViewModel(
    application: Application,
    authRepository: IAuthRepository,
    private val musicRepository: IMusicRepository
) :
    BaseAndroidViewModel(application, authRepository), MediaPlayer.OnCompletionListener {

    /**
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

    /**
     * 再生中の曲情報インデックス(private)
     */
    private var _playingTrackIndex = MutableLiveData<Int>()

    /**
     * 再生中の曲情報インデックス
     */
    val playingTrackIndex: LiveData<Int>
        get() = _playingTrackIndex

    /**
     * 再生状態
     */
    private var _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean>
        get() = _isPlaying

    /**
     * 再生プレーヤーの表示状態
     */
    private val _isMiniPlayerShown = MutableLiveData(false)
    val isMiniPlayerShown: LiveData<Boolean>
        get() = _isMiniPlayerShown


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
     * 変更されたデータ位置
     */
    private val _changeDataIndex = MutableLiveData<Int>()
    val changeDataIndex: LiveData<Int>
        get() = _changeDataIndex

    /**
     * 検索種別を設定
     */
    fun setSearchType(searchType: Constants.SearchType) {
        _searchType.value = searchType
    }

    /**
     * 検索距離を設定
     */
    fun setDistance(distance: Int) {
        _distance.value = distance
    }

    /**
     * 検索日時を設定
     */
    fun setSearchDateTime(searchDateTime: String) {
        _searchDateTime.value = searchDateTime
    }

    /**
     * Spitify上の曲お気に入り状態を変更する
     */
    fun changeTrackFavoriteState(trackIndex: Int) =
        viewModelScope.launch {

            tracklist.value?.get(trackIndex)?.let { track ->
                try {
                    musicRepository.changeTrackFavoriteState(trackIndex, !track.isSaved)

                    track.isSaved = !track.isSaved

                    val msg = if (track.isSaved) {
                        getApplication<Application>().getString(
                            R.string.save_track_message,
                            track.name
                        )
                    } else {
                        getApplication<Application>().getString(
                            R.string.remove_track_message,
                            track.name
                        )
                    }

                    _changeDataIndex.postValue(trackIndex)
                    showSnackbar(msg)

                } catch (e: Exception) {
                    handleConnectException(e)
                }
            }

        }

    /**
     * Spitify上のアーティストフォロー状態を変更する
     */
    fun changeArtistFollowState(artistIndex: Int) {
        viewModelScope.launch {

            artistlist.value?.get(artistIndex)?.let { artist ->
                try {
                    musicRepository.changeArtistFollowState(artistIndex, !artist.isFollowed)

                    artist.isFollowed = !artist.isFollowed

                    val msg = if (artist.isFollowed) {
                        getApplication<Application>().getString(
                            R.string.follow_artist_message,
                            artist.name
                        )
                    } else {
                        getApplication<Application>().getString(
                            R.string.un_follow_artist_message,
                            artist.name
                        )
                    }


                    _changeDataIndex.postValue(artistIndex)
                    showSnackbar(msg)

                } catch (e: Exception) {
                    handleConnectException(e)
                }

            }

        }
    }

    /**
     * 再生する曲を指定数だけ進めるor戻す
     */
    private fun movePlayingTrack(moveIndex: Int) {

        val currentIndex = _playingTrackIndex.value

        currentIndex?.let {
            val sumIndex = it + moveIndex

            _playingTrackIndex.value = when {
                sumIndex < 0 -> {
                    //指定位置が0以下の場合はリスト中最後の曲を設定
                    tracklist.value?.lastIndex
                }
                sumIndex > tracklist.value?.lastIndex!! -> {
                    //指定位置が要素数を超える場合はリスト中最初の曲を設定
                    0
                }
                else -> {
                    // それ以外は指定位置の曲を設定
                    sumIndex
                }
            }
        }
    }

    /**
     * 再生する曲を一つ進める
     */
    fun skipNextTrack() {
        movePlayingTrack(1)
        playTrack()
    }

    /**
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

    /**
     * 曲を再生する
     */
    fun playTrack() {

        try {
            stopTrack()

            mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes
                        .Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )

                setDataSource(tracklist.value!!.get(playingTrackIndex.value!!).previewUrl)
                prepare()
                start()
            }
            _isPlaying.value = true

        } catch (e: Exception) {
            val msg =
                getApplication<Application>().getString(R.string.general_error_message, e.javaClass)
            showMessageDialog(msg)
        }
    }

    /**
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

    /**
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
    fun setPlayingTrack(index: Int) {
        _playingTrackIndex.value = index
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
                return ResultViewModel(
                    app,
                    (app.applicationContext as JigokumimiApplication).authRepository,
                    (app.applicationContext as JigokumimiApplication).musicRepository
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

}