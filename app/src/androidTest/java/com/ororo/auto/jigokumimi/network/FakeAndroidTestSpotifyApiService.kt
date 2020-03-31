package com.ororo.auto.jigokumimi.network

class FakeAndroidTestSpotifyApiService : SpotifyApiService {

    lateinit var getMyFavoriteArtistsResponse: GetMyFavoriteArtistsResponse
    lateinit var getMyFavoriteTracksResponse: GetMyFavoriteTracksResponse
    lateinit var spotifyUserResponse: SpotifyUserResponse
    lateinit var tracksDetail: MutableList<GetTrackDetailResponse>
    lateinit var artistsDetail: MutableList<SpotifyArtistFull>

    override suspend fun getTracks(
        authorization: String,
        limit: Int?,
        offset: Int?
    ): GetMyFavoriteTracksResponse {
        return getMyFavoriteTracksResponse
    }

    override suspend fun getArtists(
        authorization: String,
        limit: Int?,
        offset: Int?
    ): GetMyFavoriteArtistsResponse {
        return getMyFavoriteArtistsResponse
    }

    override suspend fun getUserProfile(authorization: String): SpotifyUserResponse {
        return spotifyUserResponse
    }

    override suspend fun getTrackDetail(authorization: String, id: String): GetTrackDetailResponse {
        return tracksDetail.first { it.id == id }
    }

    override suspend fun getArtistDetail(authorization: String, id: String): SpotifyArtistFull {
        return artistsDetail.first { it.id == id }
    }

}