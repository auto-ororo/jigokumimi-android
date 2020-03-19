package com.ororo.auto.jigokumimi.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofitを用いてSpotifyとのAPI通信を行うサービスを定義
 *
 *
 */

// APIインターフェース
// 以下を定義
// ・ホストURL以降のパス
// ・パラメータ
// ・HTTPメソッド
// ・戻り地(エンティティ)
interface SpotifyApiService {
    @GET("me/top/tracks")
    suspend fun getTracks(
        @Header("Authorization") authorization: String?,
        @Query("limit") limit: Int?,
        @Query("offset") offset: Int?
    ): GetMyFavoriteSongsResponse

    @GET("me")
    suspend fun getUserProfile(
        @Header("Authorization") authorization: String?
    ): SpotifyUserResponse

    @GET("tracks/{id}")
    suspend fun getTrackDetail(
        @Header("Authorization") authorization: String?,
        @Path("id") id : String
    ): GetTrackDetailResponse
}

// シングルトンでインターフェースを実装する
object SpotifyApi {

    // 通信先ホストのURL
    private const val BASE_URL = "https://api.spotify.com/v1/"

    // Moshi(レスポンスJSONをエンティティに詰め込むライブラリ)を初期化
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Retrofit初期化
    // Moshi、ホストURLを設定
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl(BASE_URL)
        .build()

    val retrofitService: SpotifyApiService by lazy {
        retrofit.create(SpotifyApiService::class.java)
    }
}

