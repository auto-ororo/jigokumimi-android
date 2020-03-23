package com.ororo.auto.jigokumimi.ui

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.repository.AuthRepository
import com.ororo.auto.jigokumimi.util.Constants.Companion.AUTH_TOKEN_REQUEST_CODE
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 音量調整を端末のボタンに任せる
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    /**
     * Spotifyの認証リクエストのイベントハンドラ
     * Spotify SDKの仕様上、イベントが(Fragmentではなく)Activityに対して発火されるため、ここに実装
     */
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AUTH_TOKEN_REQUEST_CODE) {
            val response = AuthorizationClient.getResponse(resultCode, data)

            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    val authRepository =
                        AuthRepository(PreferenceManager.getDefaultSharedPreferences(this))

                    GlobalScope.launch(Dispatchers.IO) {

                        try {
                            authRepository.refreshSpotifyAuthToken(response.accessToken)
                        } catch (e: Exception) {
                            Timber.e(e.message)
                        }
                    }
                }
                AuthorizationResponse.Type.ERROR -> {
                    Timber.e(response.error)
                }
                else -> {
                    Timber.e("no handled error")
                }
            }
        }
    }
}
