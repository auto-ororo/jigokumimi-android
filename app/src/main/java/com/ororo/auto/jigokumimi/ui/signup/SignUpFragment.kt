package com.ororo.auto.jigokumimi.ui.signup

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.databinding.FragmentSignUpBinding
import com.ororo.auto.jigokumimi.ui.common.BaseFragment
import com.ororo.auto.jigokumimi.ui.common.MessageDialogFragment
import com.ororo.auto.jigokumimi.util.dataBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 新規登録画面
 */
class SignUpFragment : BaseFragment(R.layout.fragment_sign_up) {

    private val viewModel: SignUpViewModel by viewModel()

    private val binding by dataBinding<FragmentSignUpBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity).supportActionBar?.run {
                show()
                title = context?.getString(R.string.title_sign_up)
            }
        }
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
