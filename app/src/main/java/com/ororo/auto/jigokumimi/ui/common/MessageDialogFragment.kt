package com.ororo.auto.jigokumimi.ui.common

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.ororo.auto.jigokumimi.databinding.FragmentMessageDialogBinding

/**
 * メッセージダイアログ
 */
class MessageDialogFragment(private val title: String, private val message: String) :
    DialogFragment() {

    private lateinit var okClickListener: View.OnClickListener

    private lateinit var binding: FragmentMessageDialogBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // コンストラクタで受け取ったタイトル､メッセージを設定
        binding = FragmentMessageDialogBinding.inflate(layoutInflater).apply {
            titleText.text = title

            messageText.text = message
            // 呼び出し側で設定した｢OK｣ボタンクリック時の動作を設定
            okButton.setOnClickListener(okClickListener)
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

    override fun onPause() {
        super.onPause()

        dismiss()
    }

}