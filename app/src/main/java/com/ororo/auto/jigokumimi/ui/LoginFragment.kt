package com.ororo.auto.jigokumimi.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        viewModel.loginButtonEnabledState.observe(viewLifecycleOwner) {
            binding.loginButton.isEnabled = it
        }

        viewModel.isLogin.observe(viewLifecycleOwner) {
            if (it) onLoginSucceed()
        }

        baseInit(viewModel)

        binding.loginButton.setOnClickListener {
            viewModel.login()
        }

        binding.signupButton.setOnClickListener {
            this.findNavController()
                .navigate(LoginFragmentDirections.actionLoginFragmentToSignUpFragment())
        }

        // デモ用ログイン情報
        viewModel.email.value = getString(R.string.test_email_text)
        viewModel.password.value = getString(R.string.test_password_text)

        return binding.root
    }

    /**
     *  ログイン成功時の処理
     */
    private fun onLoginSucceed() {
        authenticateSpotify(viewModel)
        viewModel.doneLogin()
        this.findNavController()
            .navigate(R.id.searchFragment)
    }

}
