package com.ororo.auto.jigokumimi.repository

import android.app.Application
import com.ororo.auto.jigokumimi.network.GetMeResponse
import com.ororo.auto.jigokumimi.network.SignUpRequest
import com.ororo.auto.jigokumimi.network.SignUpResponse
import com.ororo.auto.jigokumimi.network.SpotifyUserResponse

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
     * Jigokumiminiに対してログアウトリクエストを行う
     */
    suspend fun logoutJigokumimi()

    /**
     * Jigokumiminiのユーザー情報の取得リクエストを行う
     */
    suspend fun getJigokumimiUserProfile(): GetMeResponse

    /**
     * 端末に保存したSpotifyのアクセストークンを更新する
     */
    suspend fun refreshSpotifyAuthToken(token: String)

    /**
     * Spotifyのユーザープロフィールを取得する
     */
    suspend fun getSpotifyUserProfile(): SpotifyUserResponse

}