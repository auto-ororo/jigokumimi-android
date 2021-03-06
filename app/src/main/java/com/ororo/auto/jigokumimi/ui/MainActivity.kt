package com.ororo.auto.jigokumimi.ui

import android.media.AudioManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
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
import com.ororo.auto.jigokumimi.ui.common.MessageDialogFragment
import com.ororo.auto.jigokumimi.util.dataBinding
import kotlinx.coroutines.flow.collect
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
                val dialog =
                    MessageDialogFragment(
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
                R.id.historyFragment
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

        lifecycleScope.launchWhenStarted {
            viewModel.isOnline.collect {
                binding.networkConnection.visibility = if (it) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }
        }
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
            R.id.loginFragment -> {
                if (viewModel.isDemo()) {
                    Toast.makeText(this, getString(R.string.finish_demo_message), Toast.LENGTH_LONG)
                        .show()
                    finishAndRemoveTask()
                } else {
                    viewModel.logout()
                    NavigationUI.onNavDestinationSelected(
                        item,
                        this.findNavController(R.id.myNavHostFragment)
                    )
                }
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true
    }

}
