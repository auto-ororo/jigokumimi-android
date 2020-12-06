package com.ororo.auto.jigokumimi.ui.common

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.ororo.auto.jigokumimi.databinding.FragmentConfirmDialogBinding

/**
 * 確認ダイアログ
 */
class ConfirmDialogFragment(private val title:String, private val message:String) : DialogFragment() {

    private lateinit var okClickListener : View.OnClickListener
    private lateinit var cancelClickListener : View.OnClickListener

    private lateinit var binding : FragmentConfirmDialogBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // コンストラクタで受け取ったタイトル､メッセージを設定
        binding = FragmentConfirmDialogBinding.inflate(requireActivity().layoutInflater).apply {

            titleText.text = title
            messageText.text = message
            // 呼び出し側で設定した｢OK｣ボタンクリック時の動作を設定
            okButton.setOnClickListener(okClickListener)

            // 呼び出し側で設定した｢CANCEL｣ボタンクリック時の動作を設定
            cancelButton.setOnClickListener(cancelClickListener)
        }

        return AlertDialog.Builder(context)
            .setView(binding.root)
            .create()
    }

    /**
     * ｢OK｣ボタンのクリックリスナーを設定
     * ※呼び出し側で設定
     */
    fun setOnOkButtonClickListener(listener: View.OnClickListener) {
        this.okClickListener = listener
    }

    /**
     * ｢CANCEL｣ボタンのクリックリスナーを設定
     * ※呼び出し側で設定
     */
    fun setOnCancelButtonClickListener(listener: View.OnClickListener) {
        this.cancelClickListener = listener
    }

    override fun onPause() {
        super.onPause()
        dismiss()
    }

}