package com.ororo.auto.jigokumimi.ui.common

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.ororo.auto.jigokumimi.R

open class BaseFragment(resId: Int) : Fragment(resId) {

    /**
     * 画面共通初期化処理
     * 継承先のOnCreatedViewにて呼び出す
     */
    protected fun baseInit(viewModel: BaseAndroidViewModel) {

        // エラー時にメッセージダイアログを表示
        // ｢OK｣タップ時の処理を併せて設定する
        viewModel.isErrorDialogShown.observe(viewLifecycleOwner) { isErrorDialogShown ->
            if (isErrorDialogShown) {
                val dialog =
                    MessageDialogFragment(
                        getString(R.string.title_dialog_error),
                        viewModel.errorMessage.value!!
                    )
                dialog.setOnOkButtonClickListener {
                    viewModel.isErrorDialogShown.value = false
                    dialog.dismiss()
                }
                dialog.show(parentFragmentManager, "onFragment")
            }
        }

        // Snackbarメッセージが設定された際にSnackbarを表示
        viewModel.snackbarMessage.observe(viewLifecycleOwner) {
            if (it != "") {
                Snackbar.make(this.requireView(), it, Snackbar.LENGTH_SHORT).show()
                viewModel.showedSnackbar()
            }
        }

        viewModel.isTokenExpired.observe(viewLifecycleOwner) {
            if (it) onTokenExpired(viewModel)
        }
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