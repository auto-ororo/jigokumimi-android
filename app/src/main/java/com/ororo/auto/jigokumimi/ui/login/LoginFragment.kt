package com.ororo.auto.jigokumimi.ui.login

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.databinding.FragmentLoginBinding
import com.ororo.auto.jigokumimi.ui.common.BaseFragment
import com.ororo.auto.jigokumimi.util.Constants.Companion.REQUEST_PERMISSION
import com.ororo.auto.jigokumimi.util.dataBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * ログイン画面
 */
class LoginFragment : BaseFragment(R.layout.fragment_login) {

    private val viewModel: LoginViewModel by viewModel()

    private val binding by dataBinding<FragmentLoginBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        // リスナー設定
        binding.demoButton.setOnClickListener {
            onDemoButtonClicked()
        }
        binding.loginButton.setOnClickListener {
            onLoginButtonClicked()
        }
        binding.signupButton.setOnClickListener {
            onSignUpButtonClicked()
        }

        // LiveDataの監視
        viewModel.isLogin.observe(viewLifecycleOwner) {
            if (it) onLoginSucceed()
        }
        viewModel.isDemo.observe(viewLifecycleOwner) {
            if (it) onDemoSucceed()
        }
        viewModel.loginButtonEnabledState.observe(viewLifecycleOwner) {
            binding.loginButton.isEnabled = it
        }

        // 共通初期化処理
        baseInit(viewModel)

        // リポジトリを初期化
        viewModel.initRepository()

        // ドロワーアイコンを非表示
        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity).supportActionBar?.run {
                hide()
            }
        }

        // Android 6, API 23以上でパーミッションの確認
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermission()
        }
    }

    /**
     * パーミッションのリクエスト
     */
    private fun requestPermission() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            REQUEST_PERMISSION
        )
    }

    /**
     * パーミッションのリクエスト結果を捕捉
     * 権限が不十分の場合はエラーメッセージを表示してアプリを終了する
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION) {
            for (grantResult in grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        activity,
                        getString(R.string.permission_denied_message),
                        Toast.LENGTH_SHORT
                    ).show()
                    activity?.finish()
                    return
                }
            }
        } else {
            Toast.makeText(
                activity,
                getString(R.string.permission_denied_message),
                Toast.LENGTH_SHORT
            ).show()
            activity?.finish()
        }
    }

    /**
     *  ログイン成功時の処理
     *  Spotifyへ認証リクエストを送信しトークンを取得する
     */
    private fun onLoginSucceed() {
        authenticateSpotify(viewModel)
        viewModel.doneLogin()
    }

    /**
     * デモボタンタップ時の処理
     * デモ用ログイン情報を設定する
     */
    private fun onDemoButtonClicked() {
        // デモ用ログイン情報
        viewModel.email.value = getString(R.string.test_email_text)
        viewModel.password.value = getString(R.string.test_password_text)
    }

    /**
     *  デモモード切り替え成功時の処理
     *  検索画面に遷移する
     */
    private fun onDemoSucceed() {
        this.findNavController()
            .navigate(LoginFragmentDirections.actionLoginFragmentToSearchFragment())
        viewModel.doneDemo()
    }

    /**
     * ログインボタンタップ時の処理
     * ログインを実行
     */
    private fun onLoginButtonClicked() {
        viewModel.login()
    }

    /**
     * 新規登録ボタンタップ時の処理
     * 新規登録画面に遷移
     */
    private fun onSignUpButtonClicked() {
        this.findNavController()
            .navigate(LoginFragmentDirections.actionLoginFragmentToSignUpFragment())
    }

}
