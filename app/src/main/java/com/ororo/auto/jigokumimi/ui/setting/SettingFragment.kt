package com.ororo.auto.jigokumimi.ui.setting

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.databinding.FragmentSettingBinding
import com.ororo.auto.jigokumimi.ui.common.BaseFragment
import com.ororo.auto.jigokumimi.ui.common.ConfirmDialogFragment
import com.ororo.auto.jigokumimi.util.dataBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 設定画面
 */
class SettingFragment : BaseFragment(R.layout.fragment_setting) {

    private val viewModel: SettingViewModel by viewModel()

    private val binding by dataBinding<FragmentSettingBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        // リスナー設定
        binding.changePasswordButton.setOnClickListener {
            onChangePasswordClicked()
        }
        binding.unregisterButton.setOnClickListener {
            onUnregisterClicked()
        }

        // LiveDataの監視
        viewModel.changePasswordButtonEnabledState.observe(viewLifecycleOwner) {
            binding.changePasswordButton.isEnabled = it
        }
        viewModel.isUnregistered.observe(viewLifecycleOwner) {
            if (it) onUnregisterSucceed()
        }
        viewModel.isChangedPassword.observe(viewLifecycleOwner) {
            if (it) onChangePasswordSucceed()
        }

        // 入力項目のクリア
        viewModel.clearInput()

        // 共通初期化処理
        baseInit(viewModel)

        // タイトル設定
        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity).supportActionBar?.run {
                show()
                val titleStr =
                    "${context?.getString(R.string.title_setting)} ${if (viewModel.isDemo()) context?.getString(
                        R.string.title_demo
                    ) else ""}"
                title = titleStr
            }
        }
    }

    /**
     * パスワード変更ボタンがタップされた際の処理
     * パスワード変更を行う
     */
    private fun onChangePasswordClicked() {
        viewModel.changePassword()
    }

    /**
     * 登録解除ボタンがタップされた際の処理
     * 登録解除の確認メッセージを表示する
     */
    private fun onUnregisterClicked() {
        val dialog = ConfirmDialogFragment(
            getString(R.string.title_dialog_confirm),
            getString(R.string.confirm_unregister_message)
        )
        // ｢OK｣タップ時はログインユーザーの登録解除を行う
        dialog.setOnOkButtonClickListener(
            View.OnClickListener {
                dialog.dismiss()
                viewModel.unregister()
            }
        )
        // ｢CANCEL｣タップ時はダイアログを閉じ､何もしない
        dialog.setOnCancelButtonClickListener(
            View.OnClickListener {
                dialog.dismiss()
            }
        )
        dialog.show(parentFragmentManager, getString(R.string.title_dialog_info))
    }

    /**
     * 登録解除成功時の処理
     * 完了メッセージを表示後ログイン画面に遷移する
     */
    private fun onUnregisterSucceed() {
        viewModel.doneUnregister()
        viewModel.showSnackbar(getString(R.string.success_unregister_message))
        this.findNavController()
            .navigate(SettingFragmentDirections.actionSettingFragmentToLoginFragment())
    }

    /**
     * パスワード変更成功時の処理
     * 完了メッセージを表示後検索画面に遷移する
     */
    private fun onChangePasswordSucceed() {
        viewModel.doneChangePassword()
        viewModel.showSnackbar(getString(R.string.success_change_password_message))
        this.findNavController()
            .navigate(SettingFragmentDirections.actionSettingFragmentToSearchFragment())
    }

}
