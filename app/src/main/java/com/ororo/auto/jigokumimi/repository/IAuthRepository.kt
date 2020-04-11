package com.ororo.auto.jigokumimi.repository

import android.app.Application
import com.ororo.auto.jigokumimi.network.*

interface IAuthRepository {
    /**
     * Jigokumiminiに対して新規登録リクエストを行う
     */
    suspend fun signUpJigokumimi(signUpRequest: SignUpRequest): SignUpResponse

    /**
     * Jigokumiminiに対してログインリクエストを行う
     */
    suspend fun loginJigokumimi(email: String, password: String)

    /**
     * SharedPreferencesからログイン情報を取得する
     */
    fun getSavedLoginInfo(): Pair<String, String>

    /**
     * SharedPreferencesからJigokumimiのユーザーIDを取得する
     */
    fun getSavedJigokumimiUserId(): String

    /**
     * Jigokumiminiに対してログアウトリクエストを行う
     */
    suspend fun logoutJigokumimi()

    /**
     * Jigokumiminiのユーザー情報の取得リクエストを行う
     */
    suspend fun getJigokumimiUserProfile(): GetMeResponse

    /**
     * Jigokumiminiのユーザーを登録解除する
     */
    suspend fun unregisterJigokumimiUser(): CommonResponse

    /**
     * Jigokumiminiのパスワードを変更する
     */
    suspend fun changeJigokumimiPassword(changePasswordRequest: ChangePasswordRequest): CommonResponse

    /**
     * 端末に保存したSpotifyのアクセストークンを更新する
     */
    suspend fun refreshSpotifyAuthToken(token: String)

    /**
     * Spotifyのユーザープロフィールを取得する
     */
    suspend fun getSpotifyUserProfile(): SpotifyUserResponse

}