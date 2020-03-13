package com.ororo.auto.jigokumimi.ui

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.database.getDatabase
import com.ororo.auto.jigokumimi.databinding.ActivityMainBinding
import com.ororo.auto.jigokumimi.repository.SongsRepository
import com.ororo.auto.jigokumimi.util.Constants.Companion.AUTH_TOKEN_REQUEST_CODE
import com.spotify.sdk.android.auth.AuthorizationClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber


class MainActivity : AppCompatActivity() {




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)

        GlobalScope.launch(Dispatchers.IO) {
            val database = getDatabase(application)
            val token = database.spotifyTokenDao.getToken()
            Timber.d("token: ${token}")
        }

        // 音量調整を端末のボタンに任せる
        volumeControlStream = AudioManager.STREAM_MUSIC


        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.lifecycleOwner = this

    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        val response = AuthorizationClient.getResponse(resultCode, data)


        if (requestCode == AUTH_TOKEN_REQUEST_CODE) {
            val repo = SongsRepository(getDatabase(application))

            GlobalScope.launch(Dispatchers.Main) {

                try {
                    repo.refreshSpotifyAuthToken(response.accessToken)
                    repo.refreshSongs()
                } catch (e: Exception) {
                    Timber.e(e.message)
                }
            }
        }
    }
}
