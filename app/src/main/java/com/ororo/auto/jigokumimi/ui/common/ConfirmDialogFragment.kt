package com.ororo.auto.jigokumimi.ui.common

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.ororo.auto.jigokumimi.R
import kotlinx.android.synthetic.main.fragment_confirm_dialog.view.*
import kotlinx.android.synthetic.main.fragment_message_dialog.view.*
import kotlinx.android.synthetic.main.fragment_message_dialog.view.messageText
import kotlinx.android.synthetic.main.fragment_message_dialog.view.okButton
import kotlinx.android.synthetic.main.fragment_message_dialog.view.titleText

/**
 * 確認ダイアログ
 */
class ConfirmDialogFragment(private val title:String, private val message:String) : DialogFragment() {

    private lateinit var okClickListener : View.OnClickListener
    private lateinit var cancelClickListener : View.OnClickListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // コンストラクタで受け取ったタイトル､メッセージを設定
        val view : View = requireActivity().layoutInflater.inflate(R.layout.fragment_confirm_dialog, null, false)
        view.titleText.text = title
        view.messageText.text = message
        // 呼び出し側で設定した｢OK｣ボタンクリック時の動作を設定
        view.okButton.setOnClickListener(okClickListener)

        // 呼び出し側で設定した｢CANCEL｣ボタンクリック時の動作を設定
        view.cancelButton.setOnClickListener(cancelClickListener)

        return AlertDialog.Builder(context)
            .setView(view)
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