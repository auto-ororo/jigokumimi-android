package com.ororo.auto.jigokumimi.repository

import android.content.SharedPreferences
import com.ororo.auto.jigokumimi.firebase.UserRequest
import com.ororo.auto.jigokumimi.firebase.FirestoreService
import com.ororo.auto.jigokumimi.network.*
import com.ororo.auto.jigokumimi.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber


class AuthRepository(
    private val prefData: SharedPreferences,
    private val spotifyApiService: SpotifyApiService,
    private val firestoreService: FirestoreService,
) : IAuthRepository {

    /**
     * 端末に保存したSpotifyのアクセストークンを更新する
     */
    override fun refreshSpotifyAuthToken(token: String) =
        prefData.edit().putString(Constants.SP_SPOTIFY_TOKEN_KEY, "Bearer $token").apply()

    /**
     * Spotifyのユーザープロフィールを取得する
     */
    override suspend fun getSpotifyUserProfile(): SpotifyUserResponse =
        withContext(Dispatchers.IO) {
            Timber.d("refresh spotify auth token is called")

            val spotifyToken = prefData.getString(Constants.SP_SPOTIFY_TOKEN_KEY, "")!!

            return@withContext spotifyApiService.getUserProfile(spotifyToken)
        }

    /**
     * Userの存在チェック
     */
    override suspend fun existsUser(spotifyUserId: String) =
        withContext(Dispatchers.IO) {
            return@withContext firestoreService.existsUser(UserRequest(spotifyUserId))
        }

    /**
     * UserIDの取得
     */
    override fun getUserId() =
        prefData.getString(Constants.SP_JIGOKUMIMI_USER_ID_KEY, "")!!

    /**
     * Userの作成
     */
    override suspend fun createUser(spotifyUserId: String) =
        withContext(Dispatchers.IO) {
            val userId = firestoreService.createUser(UserRequest(spotifyUserId))

            prefData.edit().let {
                it.putString(Constants.SP_JIGOKUMIMI_USER_ID_KEY, userId)
                it.apply()
            }

            return@withContext userId
        }
}