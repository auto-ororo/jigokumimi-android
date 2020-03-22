package com.ororo.auto.jigokumimi.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.databinding.FragmentSignUpBinding
import com.ororo.auto.jigokumimi.viewmodels.SignUpViewModel

/**
 * 新規登録画面
 */
class SignUpFragment : Fragment() {

    lateinit var viewModel: SignUpViewModel

    lateinit var binding: FragmentSignUpBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        activity?.run {
            val viewModelFactory = SignUpViewModel.Factory(this.application)

            viewModel = ViewModelProvider(
                viewModelStore,
                viewModelFactory
            ).get(SignUpViewModel::class.java)
        }

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_sign_up,
            container,
            false
        )

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        viewModel.signUpButtonEnabledState.observe(this) {
            binding.signUpButton.isEnabled = it
        }

        binding.signUpButton.setOnClickListener {
            viewModel.signUp()
        }

        viewModel.isLogin.observe(this) {
            if (it) onLoginSucceed()
        }

        // 新規登録完了後、完了メッセージを表示し、ログインを試みる
        viewModel.isSignUp.observe(this) {
            if (it) onSignUpSucceed()
        }

        // エラー時にメッセージダイアログを表示
        // 表示時に｢OK｣タップ時の処理を併せて設定する
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
                dialog.show(parentFragmentManager, getString(R.string.title_dialog_error))
            }
        }

        return binding.root
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
        viewModel.doneLogin()
        this.findNavController().navigate(R.id.action_signUpFragment_to_detectedSongListFragment)
    }
}
