package com.ororo.auto.jigokumimi.network

class FakeAndroidTestJigokumimiApiService : JigokumimiApiService {

    lateinit var signUpResponse:SignUpResponse
    lateinit var loginResponse:LoginResponse
    lateinit var logoutResponse: LogoutResponse
    lateinit var getMeResponse: GetMeResponse
    lateinit var refreshResponse: RefreshResponse
    lateinit var commonResponse: CommonResponse
    lateinit var getTracksAroundResponse: GetTracksAroundResponse
    lateinit var getArtistsAroundResponse: GetArtistsAroundResponse


    override suspend fun signUp(info: SignUpRequest): SignUpResponse {
        return signUpResponse
    }

    override suspend fun login(info: LoginRequest): LoginResponse {
        return loginResponse
    }

    override suspend fun logout(authorization: String): LogoutResponse {
        return logoutResponse
    }

    override suspend fun getProfile(authorization: String): GetMeResponse {
        return getMeResponse
    }

    override suspend fun refreshToken(authorization: String): RefreshResponse {
        return refreshResponse
    }

    override suspend fun postTracks(
        authorization: String,
        songs: List<PostMyFavoriteTracksRequest>
    ): CommonResponse {
        return commonResponse
    }

    override suspend fun postArtists(
        authorization: String,
        songs: List<PostMyFavoriteArtistsRequest>
    ): CommonResponse {
        return commonResponse
    }

    override suspend fun getTracksAround(
        authorization: String,
        userId: String?,
        latitude: Double,
        longitude: Double,
        distance: Int?
    ): GetTracksAroundResponse {
        return getTracksAroundResponse
    }

    override suspend fun getArtistsAround(
        authorization: String,
        userId: String?,
        latitude: Double,
        longitude: Double,
        distance: Int?
    ): GetArtistsAroundResponse {
        return getArtistsAroundResponse
    }
}