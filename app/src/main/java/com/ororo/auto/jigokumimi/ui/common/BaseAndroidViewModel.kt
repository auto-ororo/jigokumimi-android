package com.ororo.auto.jigokumimi.ui.common

import android.app.Application
import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PROTECTED
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.repository.demo.DemoAuthRepository
import com.ororo.auto.jigokumimi.util.Constants
import com.ororo.auto.jigokumimi.util.Constants.Companion.SPOTIFY_SDK_REDIRECT_HOST
import com.ororo.auto.jigokumimi.util.Constants.Companion.SPOTIFY_SDK_REDIRECT_SCHEME
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

/**
 * AndroidViewModelベースクラス
 * エラーメッセージ機構を共通化
 *
 */
open class BaseAndroidViewModel(
    application: Application,
    var authRepository: IAuthRepository
) : AndroidViewModel(application) {

    /**
     * 処理中状態
     */
    protected var _isLoading = MutableLiveData<Boolean>()
    val isLoading: MutableLiveData<Boolean>
        get() = _isLoading

    /**
     * エラーメッセージダイアログの表示状態
     */
    var isErrorDialogShown = MutableLiveData<Boolean>(false)

    /**
     * エラーメッセージの内容
     */
    protected var _errorMessage = MutableLiveData<String>()
    val errorMessage: MutableLiveData<String>
        get() = _errorMessage

    /**
     * Snackbarのメッセージ内容
     */
    protected var _snackbarMessage = MutableLiveData<String>()
    val snackbarMessage: MutableLiveData<String>
        get() = _snackbarMessage

    /**
     *  トークン認証切れ状態
     */
    protected var _isTokenExpired = MutableLiveData(false)
    val isTokenExpired: LiveData<Boolean>
        get() = _isTokenExpired

    /**
     * ログイン画面遷移後の処理
     * トークン認証フラグをリセットする
     */
    fun onMovedLogin() {
        _isTokenExpired.postValue(false)
    }

    /**
     * 例外を元にエラーメッセージを表示する
     */
    fun showMessageDialog(message: String) {
        _errorMessage.postValue(message)
        isErrorDialogShown.postValue(true)
    }

    /**
     * Snackbarメッセージを表示する
     */
    fun showSnackbar(message: String) {
        _snackbarMessage.postValue(message)
    }


    /**
     * Snackbarメッセージを初期化する
     */
    fun showedSnackbar() {
        _snackbarMessage.postValue("")
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
     * ViewModel上で発生する通信系Exceptionのハンドリングを行う
     */
    @VisibleForTesting(otherwise = PROTECTED)
    fun handleConnectException(e: Exception) {
        val msg = when (e) {
            is HttpException -> {
                if (e.code() == 401) {
                    _isTokenExpired.postValue(true)
                    getApplication<Application>().getString(R.string.token_expired_error_message)
                } else {
                    getMessageFromHttpException(e)
                }
            }
            is IOException -> {
                getApplication<Application>().getString(R.string.no_connection_error_message)
            }
            else -> {
                getApplication<Application>().getString(
                    R.string.general_error_message,
                    e.javaClass
                )
            }
        }
        showMessageDialog(msg)
    }

    /**
     * デモ用ユーザーでログインしているかどうかを判別する
     */
    fun isDemo(): Boolean {
        return (authRepository is DemoAuthRepository)
    }

    /**
     * ViewModel上で発生する認証系Exceptionのハンドリングを行う
     */
    protected fun handleAuthException(e: Exception) {
        val msg = when (e) {
            is HttpException -> {
                getMessageFromHttpException(e)
            }
            is IOException -> {
                getApplication<Application>().getString(R.string.no_connection_error_message)
            }
            else -> {
                getApplication<Application>().getString(
                    R.string.general_error_message,
                    e.javaClass
                )
            }
        }
        showMessageDialog(msg)
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