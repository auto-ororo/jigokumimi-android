package com.ororo.auto.jigokumimi.ui.result

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.repository.IMusicRepository
import com.ororo.auto.jigokumimi.ui.common.BaseAndroidViewModel
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
    BaseAndroidViewModel(application, authRepository) {

    /**
     * 取得した周辺曲情報の一覧
     */
    val tracklist = musicRepository.tracks

    /**
     * 取得した周辺アーティスト情報の一覧
     */
    val artistlist = musicRepository.artists

    /**
     * 再生中の曲情報インデックス
     */
    private var _playingTrackIndex = MutableLiveData<Int>()
    val playingTrackIndex: LiveData<Int>
        get() = _playingTrackIndex

    /**
     * 検索種別
     */
    private val _searchType = MutableLiveData(Constants.Type.TRACK)
    val searchType: LiveData<Constants.Type>
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
    fun setSearchType(searchType: Constants.Type) {
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

                    _changeDataIndex.value = trackIndex
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

                    _changeDataIndex.value = artistIndex
                    showSnackbar(msg)

                } catch (e: Exception) {
                    handleConnectException(e)
                }

            }
        }
    }
}