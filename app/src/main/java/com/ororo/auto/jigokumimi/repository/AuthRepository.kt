package com.ororo.auto.jigokumimi.repository

import android.content.SharedPreferences
import com.ororo.auto.jigokumimi.network.*
import com.ororo.auto.jigokumimi.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber


class AuthRepository(private val prefData: SharedPreferences) {

    /**
     * Jigokumiminiに対して新規登録リクエストを行う
     */
    suspend fun signUpJigokumimi(signUpRequest: SignUpRequest) =
        withContext(Dispatchers.IO) {
            Timber.d("sign up jigokumimi is called")

            // 新規登録リクエストを実施
            return@withContext JigokumimiApi.retrofitService.signUp(
                signUpRequest
            )

        }

    /**
     * Jigokumiminiに対してログインリクエストを行う
     */
    suspend fun loginJigokumimi(email: String, password: String) =
        withContext(Dispatchers.IO) {
            Timber.d("login jigokumimi is called")

            // ユーザーのお気に入り曲一覧を取得し､リクエストを作成
            val loginResponse = JigokumimiApi.retrofitService.login(
                LoginRequest(email, password)
            )

            prefData.edit().let {
                it.putString(Constants.SP_JIGOKUMIMI_EMAIL_KEY, email)
                it.putString(Constants.SP_JIGOKUMIMI_PASSWORD_KEY, password)
                it.putString(
                    Constants.SP_JIGOKUMIMI_TOKEN_KEY,
                    "Bearer ${loginResponse.data.accessToken}"
                )
                it.putInt(Constants.SP_JIGOKUMIMI_TOKEN_EXPIRE_KEY, loginResponse.data.expiresIn)
                it.apply()
            }

        }

    /**
     * SharedPreferencesからログイン情報を取得する
     */
    fun getSavedLoginInfo() : Pair<String, String> {
        val email = prefData.getString(Constants.SP_JIGOKUMIMI_EMAIL_KEY,"")!!
        val password = prefData.getString(Constants.SP_JIGOKUMIMI_PASSWORD_KEY,"")!!

        return Pair(email, password)
    }

    /**
     * Jigokumiminiに対してログアウトリクエストを行う
     */
    suspend fun logoutJigokumimi() =
        withContext(Dispatchers.IO) {
            Timber.d("login jigokumimi is called")

            val token = prefData.getString(Constants.SP_JIGOKUMIMI_TOKEN_KEY, "")

            // ユーザーのお気に入り曲一覧を取得し､リクエストを作成
            JigokumimiApi.retrofitService.logout(
                token!!
            )

            prefData.edit().let {
                it.putString(Constants.SP_JIGOKUMIMI_EMAIL_KEY, "")
                it.putString(Constants.SP_JIGOKUMIMI_PASSWORD_KEY, "")
                it.putString(
                    Constants.SP_JIGOKUMIMI_TOKEN_KEY, ""
                )
                it.putInt(Constants.SP_JIGOKUMIMI_TOKEN_EXPIRE_KEY, 0)
                it.apply()
            }
        }

    /**
     * Jigokumiminiのユーザー情報の取得リクエストを行う
     */
    suspend fun getJigokumimiUserProfile() =
        withContext(Dispatchers.IO) {
            Timber.d("get jigokumimi user profile is called")

            val token = prefData.getString(Constants.SP_JIGOKUMIMI_TOKEN_KEY, "")!!

            // ユーザーのお気に入り曲一覧を取得し､リクエストを作成
            return@withContext JigokumimiApi.retrofitService.getProfile(
                token
            )

        }

    suspend fun refreshSpotifyAuthToken(token: String) =
        withContext(Dispatchers.IO) {

            prefData.edit().putString(Constants.SP_SPOTIFY_TOKEN_KEY, "Bearer $token").apply()
            Timber.d("refresh spotify auth token is called : $token")
        }

    suspend fun getSpotifyUserProfile(): SpotifyUserResponse =
        withContext(Dispatchers.IO) {
            Timber.d("refresh spotify auth token is called")

            val spotifyToken = prefData.getString(Constants.SP_SPOTIFY_TOKEN_KEY, "")!!

            return@withContext SpotifyApi.retrofitService.getUserProfile(spotifyToken)
        }

}