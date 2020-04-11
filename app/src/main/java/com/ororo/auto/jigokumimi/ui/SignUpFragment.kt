package com.ororo.auto.jigokumimi.ui

import android.opengl.Visibility
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
import com.ororo.auto.jigokumimi.databinding.FragmentSignUpBinding
import com.ororo.auto.jigokumimi.viewmodels.SignUpViewModel

/**
 * 新規登録画面
 */
class SignUpFragment : BaseFragment() {

    lateinit var viewModel: SignUpViewModel

    lateinit var binding: FragmentSignUpBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // ViewModel取得or生成
        activity?.run {
            val viewModelFactory = SignUpViewModel.Factory(this.application)

            viewModel = ViewModelProvider(
                viewModelStore,
                viewModelFactory
            ).get(SignUpViewModel::class.java)
        }

        // データバインディング設定
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_sign_up,
            container,
            false
        )
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        // リスナー設定
        binding.signUpButton.setOnClickListener {
            onSignUpClicked()
        }

        // LiveDataの監視
        viewModel.isLogin.observe(viewLifecycleOwner) {
            if (it) onLoginSucceed()
        }
        viewModel.isSignUp.observe(viewLifecycleOwner) {
            if (it) onSignUpSucceed()
        }
        viewModel.signUpButtonEnabledState.observe(viewLifecycleOwner) {
            binding.signUpButton.isEnabled = it
        }

        // 入力項目のクリア
        viewModel.clearInput()

        // 共通初期化処理
        baseInit(viewModel)

        // タイトル設定
        (activity as AppCompatActivity).supportActionBar?.run {
            show()
            title = context?.getString(R.string.title_sign_up)
        }

        return binding.root
    }

    /**
     * 新規登録ボタンタップ時の処理
     * 新規登録を実行する
     */
    private fun onSignUpClicked() {
        viewModel.signUp()
    }

    /**
     * 新規登録が完了した際の処理
     * 完了メッセージ表示後、ログインを試みる
     */
    private fun onSignUpSucceed() {
        val dialog = MessageDialogFragment(
            getString(R.string.title_dialog_info),
            getString(R.string.success_sign_up)
        )
        dialog.setOnOkButtonClickListener(
            View.OnClickListener {
                dialog.dismiss()
                viewModel.doneSignUp()
                viewModel.login()
            }
        )
        dialog.show(parentFragmentManager, getString(R.string.title_dialog_info))
    }

    /**
     * ログイン成功時の処理
     * 周辺局情報画面に遷移する
     */
    private fun onLoginSucceed() {
        authenticateSpotify(viewModel)
        viewModel.doneLogin()
    }

}
