package com.ororo.auto.jigokumimi.repository

import com.ororo.auto.jigokumimi.network.*

interface IAuthRepository {
    /**
     * 端末に保存したSpotifyのアクセストークンを更新する
     */
    fun refreshSpotifyAuthToken(token: String)

    /**
     * Spotifyのユーザープロフィールを取得する
     */
    suspend fun getSpotifyUserProfile(): SpotifyUserResponse

    /**
     * Userの存在チェック
     */
    suspend fun existsUser(spotifyUserId: String): Boolean

    /**
     * Userの作成
     */
    suspend fun createUser(spotifyUserId: String): String

    /**
     * UserIDの取得
     */
    fun getUserId(): String
}