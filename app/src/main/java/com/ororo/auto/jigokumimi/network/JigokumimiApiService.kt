package com.ororo.auto.jigokumimi.network

/**
 * Retrofitを用いてJigokumimiバックエンドとのAPI通信を行うサービスを定義
 *
 *
 */
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

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
interface JigokumimiApiService {
    @POST("tracks")
    suspend fun postTracks(
        @Header("Authorization") authorization: String?,
        @Body songs: List<PostMyFavoriteTracksRequest>
    ): PostMyFavoriteTracksResponse

    @GET("tracks")
    suspend fun getTracksAround(
        @Header("Authorization") authorization: String?,
        @Query("userId") userId: String?,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("distance") distance: Int?
    ): GetTracksAroundResponse
}

// シングルトンでインターフェースを実装する
object JigokumimiApi {

    // 通信先ホストのURL
    private const val BASE_URL = "http://10.229.72.152:10080/api/"

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

    val retrofitService: JigokumimiApiService by lazy {
        retrofit.create(JigokumimiApiService::class.java)
    }
}