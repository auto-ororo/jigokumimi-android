package com.ororo.auto.jigokumimi.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.database.getDatabase
import com.ororo.auto.jigokumimi.repository.SongsRepository
import com.ororo.auto.jigokumimi.repository.SpotifyRepository
import com.ororo.auto.jigokumimi.util.Constants.Companion.AUTH_TOKEN_REQUEST_CODE
import com.spotify.sdk.android.auth.AuthorizationClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber


class MainActivity : AppCompatActivity() {
    private val REQUEST_PERMISSION = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6, API 23以上でパーミッションの確認
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermission()
        }

        GlobalScope.launch(Dispatchers.IO) {
            val database = getDatabase(application)
            val token = database.spotifyTokenDao.getToken()
            Timber.d("token: ${token}")
        }

        // 音量調整を端末のボタンに任せる
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    // 許可を求める
    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            REQUEST_PERMISSION
        )
    }

    // 結果の受け取り
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION) { // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                startLocationActivity()
            } else { // それでも拒否された時の対応
                Toast.makeText(this, "許可されないとアプリが実行できません", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        val response = AuthorizationClient.getResponse(resultCode, data)

        if (requestCode == AUTH_TOKEN_REQUEST_CODE) {
            val spotifyRepository = SpotifyRepository(getDatabase(application))

            GlobalScope.launch(Dispatchers.Main) {

                try {
                    spotifyRepository.refreshSpotifyAuthToken(response.accessToken)
                } catch (e: Exception) {
                    Timber.e(e.message)
                }
            }
        }
    }
}
