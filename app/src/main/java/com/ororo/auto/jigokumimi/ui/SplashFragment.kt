package com.ororo.auto.jigokumimi.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.databinding.FragmentSplashBinding
import com.ororo.auto.jigokumimi.viewmodels.SplashViewModel

/**
 * スプラッシュ画面
 */
class SplashFragment : BaseFragment() {

    private val REQUEST_PERMISSION = 1000

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

        viewModel.isReady.observe(viewLifecycleOwner) {
            if (it) {
                when (viewModel.isLogin.value) {
                    true -> onLoginSucceed()
                    else -> onLoginFailed()
                }
            }
        }

        viewModel.loginBySavedInput()

        // Android 6, API 23以上でパーミッションの確認
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermission()
        }

        baseInit(viewModel)

        return binding.root
    }

    // 許可を求める
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

    // 結果の受け取り
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION) { // 使用が許可された
            for (grantResult in grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(activity, "許可されないとアプリが実行できません", Toast.LENGTH_SHORT).show()
                    activity?.finish()
                    return
                }
            }
        } else {
            Toast.makeText(activity, "許可されないとアプリが実行できません", Toast.LENGTH_SHORT).show()
            activity?.finish()
        }
    }

    /**
     * ログインに成功した場合周辺曲情報画面に遷移
     */
    private fun onLoginSucceed() {
        authenticateSpotify(viewModel)
        this.findNavController()
            .navigate(SplashFragmentDirections.actionSplashFragmentToSearchFragment())
    }

    /**
     * ログインに失敗した場合ログイン画面に遷移
     */
    private fun onLoginFailed() {
        this.findNavController()
            .navigate(SplashFragmentDirections.actionSplashFragmentToLoginFragment())
    }
}
