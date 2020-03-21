package com.ororo.auto.jigokumimi.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

/**
 * AndroidViewModelベースクラス
 * エラーメッセージ機構を共通化
 *
 */
open class BaseAndroidViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * エラーメッセージダイアログの表示状態
     */
    var isErrorDialogShown = MutableLiveData<Boolean>(false)

    /**
     * エラーメッセージの内容(Private)
     */
    protected var _errorMessage = MutableLiveData<String>()

    /**
     * エラーメッセージの内容
     */
    val errorMessage: MutableLiveData<String>
        get() = _errorMessage

    /**
     * 例外を元にエラーメッセージを表示する
     */
    protected fun showMessageDialog(message: String) {
        _errorMessage.postValue(message)
        isErrorDialogShown.postValue(true)
    }

}