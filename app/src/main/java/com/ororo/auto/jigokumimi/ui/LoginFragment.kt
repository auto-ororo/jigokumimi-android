package com.ororo.auto.jigokumimi.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.databinding.FragmentLoginBinding
import com.ororo.auto.jigokumimi.viewmodels.LoginViewModel

/**
 * ログイン画面
 */
class LoginFragment : BaseFragment() {

    lateinit var viewModel: LoginViewModel

    lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // ViewModel取得or生成
        activity?.run {
            val viewModelFactory = LoginViewModel.Factory(this.application)

            viewModel = ViewModelProvider(
                viewModelStore,
                viewModelFactory
            ).get(LoginViewModel::class.java)
        }

        // データバインディング設定
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_login,
            container,
            false
        )
        binding.lifecycleOwner = viewLifecycleOwner
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
        viewModel.loginButtonEnabledState.observe(viewLifecycleOwner) {
            binding.loginButton.isEnabled = it
        }

        // 共通初期化処理
        baseInit(viewModel)


        // ドロワーアイコンを非表示
        (activity as AppCompatActivity).supportActionBar?.run {
            hide()
        }

        return binding.root
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
