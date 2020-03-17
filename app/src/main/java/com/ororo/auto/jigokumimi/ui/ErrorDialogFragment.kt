package com.ororo.auto.jigokumimi.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.ororo.auto.jigokumimi.R
import kotlinx.android.synthetic.main.fragment_ok_dialog.view.*


class ErrorDialogFragment(private val title:String,private val message:String) : DialogFragment() {

    private lateinit var okClickListener : View.OnClickListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val view : View = activity!!.layoutInflater.inflate(R.layout.fragment_ok_dialog, null, false)
        view.title_text.text = title
        view.message_text.text = message
        view.ok_button.setOnClickListener(okClickListener)

        return AlertDialog.Builder(context)
            .setView(view)
            .create()
    }

    fun setOnOkButtonClickListener(listener: View.OnClickListener) {
        this.okClickListener = listener
    }
    override fun onPause() {
        super.onPause()

        dismiss()
    }

}