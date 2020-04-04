package com.ororo.auto.jigokumimi.ui

import ServiceLocator
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.databinding.ActivityMainBinding
import com.ororo.auto.jigokumimi.util.Constants.Companion.AUTH_TOKEN_REQUEST_CODE
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 音量調整を端末のボタンに任せる
        volumeControlStream = AudioManager.STREAM_MUSIC

        @Suppress("UNUSED_VARIABLE")
        val binding =
            DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        drawerLayout = binding.drawerLayout

        appBarConfiguration = AppBarConfiguration(
            //navigation.xmlに設定したidのセットを渡す
            setOf(R.id.loginFragment, R.id.searchFragment), drawerLayout
        )

        val navController = Navigation.findNavController(this, R.id.myNavHostFragment).also {
            setupActionBarWithNavController(
                it,
                appBarConfiguration
            )
        }

        NavigationUI.setupWithNavController(binding.navView, navController)

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.myNavHostFragment)
        return navController.navigateUp(appBarConfiguration)
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
                    val authRepository = ServiceLocator.getAuthRepository(application)

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
            }
        }
    }
}
