package com.ororo.auto.jigokumimi.repository

interface IAuthRepository {
    /**
     * 端末に保存したSpotifyのアクセストークンを更新する
     */
    fun refreshSpotifyAuthToken(token: String)

    /**
     * SpotifyのユーザーIDを取得する
     */
    suspend fun getSpotifyUserId(): String

    /**
     * Userの取得
     */
    suspend fun existsUser(spotifyUserId: String): Boolean

    /**
     * Userの作成
     */
    suspend fun createUser(spotifyUserId: String)

    /**
     * UserIDの取得
     */
    suspend fun getUserId(spotifyUserId: String): String
}