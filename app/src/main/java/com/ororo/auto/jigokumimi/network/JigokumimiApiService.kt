package com.ororo.auto.jigokumimi.network

/**
 * Retrofitを用いてJigokumimiバックエンドとのAPI通信を行うサービスを定義
 *
 *
 */
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

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

    @POST("tracks")
    suspend fun postTracks(
        @Header("Authorization") authorization: String,
        @Body songs: List<PostMyFavoriteTracksRequest>
    ): PostResponse

    @POST("artists")
    suspend fun postArtists(
        @Header("Authorization") authorization: String,
        @Body songs: List<PostMyFavoriteArtistsRequest>
    ): PostResponse

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
}

// シングルトンでインターフェースを実装する
object JigokumimiApi {

    // 通信先ホストのURL
    private const val BASE_URL = "http://192.168.0.4:10080/api/"

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
                // タイムアウトを60秒に設定(開発用)
                // TODO リリース時に外す
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS).build()
        )
        .build()

    val retrofitService: JigokumimiApiService by lazy {
        retrofit.create(JigokumimiApiService::class.java)
    }
}