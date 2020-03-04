package com.ororo.auto.jigokumimi.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import com.ororo.auto.jigokumimi.database.getDatabase
import com.ororo.auto.jigokumimi.repository.SongsRepository
import com.ororo.auto.jigokumimi.util.Constants
import com.ororo.auto.jigokumimi.util.Constants.Companion.SPOTIFY_SDK_REDIRECT_HOST
import com.ororo.auto.jigokumimi.util.Constants.Companion.SPOTIFY_SDK_REDIRECT_SCHEME
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 検知した曲の一覧を表示するDetectedSongListFragmentのViewModel
 *
 *
 */

class DetectedSongListViewModel(application: Application) : AndroidViewModel(application) {

    val songsRepository = SongsRepository(getDatabase(application))

    /**
     * A playlist of songs displayed on the screen.
     */
    val songlist = songsRepository.songs


    /**
     * Event triggered for network error. This is private to avoid exposing a
     * way to set this value to observers.
     */
    private var _eventNetworkError = MutableLiveData<Boolean>(false)

    /**
     * Event triggered for network error. Views should use this to get access
     * to the data.
     */
    val eventNetworkError: LiveData<Boolean>
        get() = _eventNetworkError

    /**
     * Flag to display the error message. This is private to avoid exposing a
     * way to set this value to observers.
     */
    private var _isNetworkErrorShown = MutableLiveData<Boolean>(false)

    /**
     * Flag to display the error message. Views should use this to get access
     * to the data.
     */
    val isNetworkErrorShown: LiveData<Boolean>
        get() = _isNetworkErrorShown

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

    fun getAuthenticationRequest(type: AuthorizationResponse.Type): AuthorizationRequest {
        return AuthorizationRequest.Builder(
            Constants.CLIENT_ID,
            type,
            getRedirectUri().toString()
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

    private fun getRedirectUri(): Uri {

        return Uri.Builder()
            .scheme(SPOTIFY_SDK_REDIRECT_SCHEME)
            .authority(SPOTIFY_SDK_REDIRECT_HOST)
            .build()
    }

    /**
     * Resets the network error flag.
     */
    fun onNetworkErrorShown() {
        _isNetworkErrorShown.value = true
    }

    /**
     * Factory for constructing DetectedSongListViewModel
     */
    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DetectedSongListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DetectedSongListViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

}