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

        viewModel.signUpButtonEnabledState.observe(viewLifecycleOwner) {
            binding.signUpButton.isEnabled = it
        }

        binding.signUpButton.setOnClickListener {
            viewModel.signUp()
        }

        viewModel.isLogin.observe(viewLifecycleOwner) {
            if (it) onLoginSucceed()
        }

        // 新規登録完了後、完了メッセージを表示し、ログインを試みる
        viewModel.isSignUp.observe(viewLifecycleOwner) {
            if (it) onSignUpSucceed()
        }

        baseInit(viewModel)

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
        authenticateSpotify(viewModel)
        viewModel.doneLogin()
        this.findNavController()
            .navigate(R.id.searchFragment)
    }

}
