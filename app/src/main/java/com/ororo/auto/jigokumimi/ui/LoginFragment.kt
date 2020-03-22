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
import com.ororo.auto.jigokumimi.databinding.FragmentLoginBinding
import com.ororo.auto.jigokumimi.viewmodels.LoginViewModel

/**
 * ログイン画面
 */
class LoginFragment : Fragment() {

    lateinit var viewModel: LoginViewModel

    lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        activity?.run {
            val viewModelFactory = LoginViewModel.Factory(this.application)

            viewModel = ViewModelProvider(
                viewModelStore,
                viewModelFactory
            ).get(LoginViewModel::class.java)
        }

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_login,
            container,
            false
        )

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        viewModel.loginButtonEnabledState.observe(this) {
            binding.loginButton.isEnabled = it
        }

        viewModel.isLogin.observe(this) {
            if (it) onLoginSucceed()
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

        binding.loginButton.setOnClickListener {
            viewModel.login()
        }

        binding.signupButton.setOnClickListener {
            this.findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        return binding.root
    }

    /**
     *  ログイン成功時の処理
     */
    private fun onLoginSucceed() {
        viewModel.doneLogin()
        this.findNavController().navigate(R.id.action_loginFragment_to_detectedSongListFragment)
    }

}
