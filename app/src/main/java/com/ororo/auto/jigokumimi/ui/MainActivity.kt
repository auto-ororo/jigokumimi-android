package com.ororo.auto.jigokumimi.ui

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.navigation.NavigationView
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.databinding.ActivityMainBinding
import com.ororo.auto.jigokumimi.util.Constants.Companion.AUTH_TOKEN_REQUEST_CODE
import com.ororo.auto.jigokumimi.util.dataBinding
import com.ororo.auto.jigokumimi.viewmodels.MainViewModel
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationResponse
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(R.layout.activity_main),
    NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private val viewModel: MainViewModel by viewModel()
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val binding by dataBinding<ActivityMainBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 音量調整を端末のボタンで行わせる
        volumeControlStream = AudioManager.STREAM_MUSIC

        // エラー時にメッセージダイアログを表示
        viewModel.isErrorDialogShown.observe(this) { isErrorDialogShown ->
            if (isErrorDialogShown) {
                val dialog = MessageDialogFragment(
                    getString(R.string.title_dialog_error),
                    viewModel.errorMessage.value!!
                )
                dialog.setOnOkButtonClickListener(
                    View.OnClickListener {
                        viewModel.isErrorDialogShown.value = false
                        dialog.dismiss()
                    }
                )
                dialog.show(supportFragmentManager, "onActivity")
            }
        }

        drawerLayout = binding.drawerLayout

        // ドロワーアイコンの表示設定
        appBarConfiguration = AppBarConfiguration(
            //Set内に格納したFragment上でドロワーアイコンを表示する
            setOf(
                R.id.loginFragment,
                R.id.searchFragment,
                R.id.historyFragment,
                R.id.settingFragment
            ), drawerLayout
        )
        val navController = Navigation.findNavController(this, R.id.myNavHostFragment).also {
            setupActionBarWithNavController(
                it,
                appBarConfiguration
            )
        }
        NavigationUI.setupWithNavController(binding.navView, navController)

        binding.navView.setNavigationItemSelectedListener(this);
    }

    /**
     * ActionBarに戻る(←)を表示､Navigationと対応付ける
     */
    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.myNavHostFragment)
        return navController.navigateUp(appBarConfiguration)
    }

    /**
     * NavigationDrawer内のメニューがタップされた時の動作
     * タップした項目に対応する画面に遷移させる
     * ※ログアウトタップ時のみ､ログアウト処理を画面遷移前に行う
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.searchFragment ->
                NavigationUI.onNavDestinationSelected(
                    item,
                    this.findNavController(R.id.myNavHostFragment)
                )
            R.id.historyFragment ->
                NavigationUI.onNavDestinationSelected(
                    item,
                    this.findNavController(R.id.myNavHostFragment)
                )
            R.id.settingFragment ->
                NavigationUI.onNavDestinationSelected(
                    item,
                    this.findNavController(R.id.myNavHostFragment)
                )
            R.id.loginFragment -> {
                // ログアウト
                viewModel.logout()
                NavigationUI.onNavDestinationSelected(
                    item,
                    this.findNavController(R.id.myNavHostFragment)
                )
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true
    }

    /**
     * Spotify認証リクエストのコールバックを捕捉するイベントハンドラ
     * ※Spotify SDKの仕様上、ActivityResultイベントが(Fragmentではなく)Activityに対して発火されるため、ここに実装
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
                // 認証に成功した場合(トークンを取得できた場合)､端末内のSpotifyトークンを更新し､検索画面に遷移する
                AuthorizationResponse.Type.TOKEN -> {

                    viewModel.refreshSpotifyAuthToken(response.accessToken)

                    // 検索画面に遷移
                    this.findNavController(R.id.myNavHostFragment).navigate(R.id.searchFragment)
                }

                // 認証に失敗した場合(トークンを取得できなかった場合)､エラーメッセージを表示しログイン画面に遷移する
                else -> {
                    // エラーメッセージ表示
                    viewModel.showMessageDialog(getString(R.string.spotify_auth_error_message))
                    // ログイン画面に遷移
                    this.findNavController(R.id.myNavHostFragment).navigate(R.id.loginFragment)
                }
            }
        } else {
            // ユーザーがSpotify認証を行わなかった等の理由でリクエストコードが帰ってこなかった場合､エラーメッセージを表示しログイン画面に遷移する

            // エラーメッセージ表示
            viewModel.showMessageDialog(getString(R.string.spotify_auth_error_message))
            // ログイン画面に遷移
            this.findNavController(R.id.myNavHostFragment).navigate(R.id.loginFragment)

        }
    }
}
