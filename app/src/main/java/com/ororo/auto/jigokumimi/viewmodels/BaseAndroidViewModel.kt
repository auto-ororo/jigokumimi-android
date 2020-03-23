package com.ororo.auto.jigokumimi.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.util.Constants
import com.ororo.auto.jigokumimi.util.Constants.Companion.SPOTIFY_SDK_REDIRECT_HOST
import com.ororo.auto.jigokumimi.util.Constants.Companion.SPOTIFY_SDK_REDIRECT_SCHEME
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import org.json.JSONObject
import retrofit2.HttpException

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

    /**
     * HTTPExceptionを元にエラーメッセージを取得する
     */
    protected fun getMessageFromHttpException(e: HttpException): String {
        // レスポンスBody中の「message」を返却する
        // 「message」が存在しない場合はサーバー側でエラーが発生した旨のメッセージを返却する
        val responseJson = JSONObject(String((e.response()?.errorBody()?.bytes()!!)))
        return if (responseJson.has("message")) {
            responseJson.getString("message")
        } else {
            getApplication<Application>().getString(R.string.request_fail_message)
        }
    }

    /**
     * Spotifyに対して認証リクエストを行う
     *
     */
    fun getAuthenticationRequest(type: AuthorizationResponse.Type): AuthorizationRequest {
        return AuthorizationRequest.Builder(
                Constants.CLIENT_ID,
                type,
                Uri.Builder().scheme(SPOTIFY_SDK_REDIRECT_SCHEME)
                    .authority(SPOTIFY_SDK_REDIRECT_HOST).build().toString()
            )
            .setShowDialog(false)
            .setScopes(
                arrayOf(
                    "user-read-email",
                    "user-top-read",
                    "user-read-recently-played",
                    "user-library-modify",
                    "user-follow-modify",
                    "user-follow-read",
                    "user-library-read"
                )
            )
            .setCampaign("your-campaign-token")
            .build()
    }
}