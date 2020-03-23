package com.ororo.auto.jigokumimi.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import androidx.lifecycle.observe
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController

import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.databinding.FragmentSplashBinding
import com.ororo.auto.jigokumimi.viewmodels.SplashViewModel

/**
 * スプラッシュ画面
 */
class SplashFragment : Fragment() {

    lateinit var viewModel: SplashViewModel

    lateinit var binding: FragmentSplashBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        activity?.run {
            val viewModelFactory = SplashViewModel.Factory(this.application)

            viewModel = ViewModelProvider(
                viewModelStore,
                viewModelFactory
            ).get(SplashViewModel::class.java)
        }

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_splash,
            container,
            false
        )

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        viewModel.isReady.observe(this) {
            if(it) {
                when (viewModel.isLogin.value) {
                    true -> onLoginSucceed()
                    else -> onLoginFailed()
                }
            }
        }

        viewModel.loginBySavedInput()

        return binding.root
    }

    /**
     * ログインに成功した場合周辺曲情報画面に遷移
     */
    private fun onLoginSucceed() {
        this.findNavController().navigate(R.id.action_splashFragment_to_detectedSongListFragment)
    }

    /**
     * ログインに失敗した場合ログイン画面に遷移
     */
    private fun onLoginFailed() {
        this.findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
    }

}
