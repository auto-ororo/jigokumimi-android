package com.ororo.auto.jigokumimi.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

/**
 * Retrofitを用いてJigokumimiバックエンドとのAPI通信を行うサービスを定義
 */

/**
 * APIインターフェース
 * 以下を定義
 * ・ホストURL以降のパス
 * ・パラメータ
 * ・HTTPメソッド
 * ・戻り地(エンティティ)
 */
interface JigokumimiApiService {
    @POST("auth/create")
    suspend fun signUp(
        @Body info: SignUpRequest
    ): SignUpResponse

    @POST("auth/login")
    suspend fun login(
        @Body info: LoginRequest
    ): LoginResponse

    @POST("auth/logout")
    suspend fun logout(
        @Header("Authorization") authorization: String
    ): LogoutResponse

    @GET("auth/me")
    suspend fun getProfile(
        @Header("Authorization") authorization: String
    ): GetMeResponse

    @POST("auth/refresh")
    suspend fun refreshToken(
        @Header("Authorization") authorization: String
    ): RefreshResponse

    @PUT("auth/changePassword")
    suspend fun changePassword(
        @Header("Authorization") authorization: String,
        @Body info: ChangePasswordRequest
    ): CommonResponse

    @DELETE("auth/delete")
    suspend fun unregisterUser(
        @Header("Authorization") authorization: String
    ): CommonResponse

    @POST("tracks")
    suspend fun postTracks(
        @Header("Authorization") authorization: String,
        @Body songs: List<PostMyFavoriteTracksRequest>
    ): CommonResponse

    @POST("artists")
    suspend fun postArtists(
        @Header("Authorization") authorization: String,
        @Body songs: List<PostMyFavoriteArtistsRequest>
    ): CommonResponse

    @GET("tracks")
    suspend fun getTracksAround(
        @Header("Authorization") authorization: String,
        @Query("userId") userId: String?,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("distance") distance: Int?
    ): GetTracksAroundResponse

    @GET("artists")
    suspend fun getArtistsAround(
        @Header("Authorization") authorization: String,
        @Query("userId") userId: String?,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("distance") distance: Int?
    ): GetArtistsAroundResponse

    @GET("artists/history")
    suspend fun getArtistsAroundSearchHistories(
        @Header("Authorization") authorization: String,
        @Query("userId") userId: String
    ): GetArtistSearchHistoryResponse

    @GET("tracks/history")
    suspend fun getTracksAroundSearchHistories(
        @Header("Authorization") authorization: String,
        @Query("userId") userId: String
    ): GetTrackSearchHistoryResponse

    @DELETE("artists/history")
    suspend fun deleteArtistsAroundSearchHistories(
        @Header("Authorization") authorization: String,
        @Query("historyId") historyId: String
    ): CommonResponse

    @DELETE("tracks/history")
    suspend fun deleteTracksAroundSearchHistories(
        @Header("Authorization") authorization: String,
        @Query("historyId") historyId: String
    ): CommonResponse


}

/**
 * シングルトンでインターフェースを実装
 */
object JigokumimiApi {

    // 通信先ホストのURL
    private const val BASE_URL = "https://ororoauto.com/api/"

    // Moshi(レスポンスJSONをエンティティに詰め込むライブラリ)を初期化
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Retrofit初期化
    // Moshi、ホストURLを設定
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl(BASE_URL)
        .client(
            OkHttpClient.Builder()
                .addInterceptor {
                    // Header追加
                    val original = it.request()
                    val request = original.newBuilder().header("Accept", "application/json")
                        .method(original.method(), original.body())
                        .build();
                    it.proceed(request)
                }
                .build()
        )
        .build()

    val retrofitService: JigokumimiApiService by lazy {
        retrofit.create(JigokumimiApiService::class.java)
    }
}