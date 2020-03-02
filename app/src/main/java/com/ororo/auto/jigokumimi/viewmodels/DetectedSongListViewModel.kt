package com.ororo.auto.jigokumimi.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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


    fun refreshSongsFromRepository() {
        viewModelScope.launch {
            try {
                songsRepository.refreshSongs()

            } catch (e: Exception) {
                Timber.d(e.message)
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
     * Factory for constructing DevByteViewModel with parameter
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