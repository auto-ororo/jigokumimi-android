package com.ororo.auto.jigokumimi.ui

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.util.Constants
import com.ororo.auto.jigokumimi.viewmodels.BaseAndroidViewModel
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationResponse

open class BaseFragment(resId: Int) : Fragment(resId) {

    /**
     * 画面共通初期化処理
     * 継承先のOnCreatedViewにて呼び出す
     */
    protected fun baseInit(viewModel: BaseAndroidViewModel) {

        // エラー時にメッセージダイアログを表示
        // ｢OK｣タップ時の処理を併せて設定する
        viewModel.isErrorDialogShown.observe(
            viewLifecycleOwner,
            Observer<Boolean> { isErrorDialogShown ->
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
                    dialog.show(parentFragmentManager, "onFragment")
                }
            }
        )

        // Snackbarメッセージが設定された際にSnackbarを表示
        viewModel.snackbarMessage.observe(
            viewLifecycleOwner,
            Observer {
                if (it != "") {
                    Snackbar.make(this.requireView(), it, Snackbar.LENGTH_SHORT).show()
                    viewModel.showedSnackbar()
                }
            }
        )

        viewModel.isTokenExpired.observe(viewLifecycleOwner) {
            if (it) onTokenExpired(viewModel)
        }
    }

    /**
     * Spotifyに対して認証リクエストを投げる
     */
    protected fun authenticateSpotify(viewModel: BaseAndroidViewModel) {
        val request = viewModel.getAuthenticationRequest(AuthorizationResponse.Type.TOKEN)

        AuthorizationClient.openLoginActivity(
            activity,
            Constants.AUTH_TOKEN_REQUEST_CODE,
            request
        )
    }

    /**
     * トークンの認証期限が切れた際、ログイン画面に遷移する
     */
    protected fun onTokenExpired(viewModel: BaseAndroidViewModel) {
        viewModel.onMovedLogin()
        this.findNavController()
            .navigate(R.id.loginFragment)
    }
}