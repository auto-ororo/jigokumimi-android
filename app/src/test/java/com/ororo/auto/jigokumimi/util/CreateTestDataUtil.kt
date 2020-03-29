package com.ororo.auto.jigokumimi.util

import com.github.javafaker.Faker
import com.ororo.auto.jigokumimi.network.*
import java.util.*

class CreateTestDataUtil {
    val faker = Faker(Locale("jp_JP"))

    fun createDummySpotifyArtist(): SpotifyArtist {
        return SpotifyArtist(
            externalUrls = mapOf(
                Pair(faker.lorem().word(), faker.internet().url())
            ),
            href = faker.internet().url(),
            id = faker.random().hex(),
            name = faker.name().fullName(),
            type = faker.lorem().word(),
            uri = faker.internet().url()
        )
    }

    fun createDummySpotifyImage(): SpotifyImage {
        return SpotifyImage(
            height = faker.number().randomDigit(),
            url = faker.internet().url(),
            width = faker.number().randomDigit()
        )
    }

    fun createDummyTrackAroundNetwork(
        rank: Int = faker.number().randomDigit(),
        spotifyTrackId: String = faker.random().hex()
    ): TrackAroundNetwork {
        return TrackAroundNetwork(
            rank = rank,
            popularity = faker.number().randomDigit(),
            spotifyTrackId = spotifyTrackId
        )
    }

    fun createDummyArtistAroundNetwork(
        rank: Int = faker.number().randomDigit(),
        spotifyArtistId: String = faker.random().hex()
    ): ArtistAroundNetwork {
        return ArtistAroundNetwork(
            rank = rank,
            popularity = faker.number().randomDigit(),
            spotifyArtistId = spotifyArtistId
        )
    }

    fun createDummyGetTrackDetailResponse(
        spotifyTrackId: String = faker.random().hex()
    ): GetTrackDetailResponse {
        return GetTrackDetailResponse(
            album = SpotifyAlbum(
                albumType = faker.lorem().word(),
                artists = listOf(
                    createDummySpotifyArtist(),
                    createDummySpotifyArtist(),
                    createDummySpotifyArtist(),
                    createDummySpotifyArtist()
                ),
                availableMarkets = listOf(faker.random().hex()),
                externalUrls = mapOf(
                    Pair(faker.lorem().word(), faker.internet().url())
                ),
                href = faker.internet().url(),
                id = faker.random().hex(),
                images = listOf(
                    createDummySpotifyImage()
                ),
                name = faker.name().fullName(),
                releaseDate = faker.date().toString(),
                releaseDatePrecision = faker.lorem().word(),
                totalTracks = faker.number().randomDigit(),
                type = faker.lorem().word(),
                uri = faker.internet().url()
            ),
            artists = listOf(
                createDummySpotifyArtist(),
                createDummySpotifyArtist(),
                createDummySpotifyArtist(),
                createDummySpotifyArtist()
            ),
            availableMarkets = listOf(faker.lorem().word()),
            discNumber = faker.number().randomDigit(),
            durationMs = faker.number().randomDigit(),
            explicit = faker.bool().bool(),
            externalUrls = mapOf(
                Pair(faker.lorem().word(), faker.internet().url())
            ),
            externalIds = mapOf(
                Pair(faker.lorem().word(), faker.internet().url())
            ),
            href = faker.internet().url(),
            id = spotifyTrackId,
            isLocal = faker.bool().bool(),
            name = faker.name().fullName(),
            popularity = faker.number().randomDigit(),
            previewUrl = faker.internet().url(),
            trackNumber = faker.number().randomDigit(),
            type = faker.lorem().word(),
            uri = faker.internet().url()
        )
    }

    fun createDummySpotifyArtistFull(
        spotifyArtistId: String = faker.random().hex()
    ): SpotifyArtistFull {
        return SpotifyArtistFull(
            images = listOf(
                createDummySpotifyImage()
            ),
            externalUrls = mapOf(
                Pair(faker.lorem().word(), faker.internet().url())
            ),
            href = faker.internet().url(),
            id = spotifyArtistId,
            name = faker.name().fullName(),
            popularity = faker.number().randomDigit(),
            type = faker.lorem().word(),
            uri = faker.internet().url(),
            followers = createDummySpotifyFollower(),
            genres = listOf(
                faker.lorem().word(),
                faker.lorem().word(),
                faker.lorem().word()
            )
        )
    }

    fun createDummySpotifyFollower(): SpotifyFollower {
        return SpotifyFollower(
            href = faker.internet().url(),
            total = faker.number().randomDigit()
        )
    }

    fun createDummySpotifyUserResponse(): SpotifyUserResponse {

        return SpotifyUserResponse(
            country = faker.country().name(),
            displayName = faker.name().fullName(),
            email = faker.internet().safeEmailAddress(),
            externalUrls = mapOf(
                Pair(faker.lorem().word(), faker.internet().url())
            ),
            followers = createDummySpotifyFollower(),
            href = faker.internet().url(),
            id = faker.random().hex(),
            images = listOf(
                createDummySpotifyImage()
            ),
            product = faker.lorem().word(),
            type = faker.lorem().word(),
            uri = faker.internet().url()
        )
    }


    fun createDummyGetMeResponse(): GetMeResponse {

        return GetMeResponse(
            data = createDummyJigokumimiUserProfile(),
            message = faker.lorem().word()
        )
    }

    fun createDummyJigokumimiUserProfile(): JigokumimiUserProfile {
        return JigokumimiUserProfile(
            id = faker.number().randomDigit(),
            email = faker.internet().safeEmailAddress(),
            emailVerifiedAt = null,
            name = faker.name().fullName(),
            createdAt = faker.date().toString(),
            updatedAt = faker.date().toString()
        )
    }


    fun createDummyGetMyFavoriteTracksResponse(): GetMyFavoriteTracksResponse {
        return GetMyFavoriteTracksResponse(
            href = faker.internet().url(),
            items = listOf(
                createDummySpotifyTrack(),
                createDummySpotifyTrack()
            ),
            limit = faker.number().randomDigit(),
            offset = faker.number().randomDigit(),
            total = faker.number().randomDigit(),
            previous = faker.internet().url(),
            next = faker.internet().url()
        )

    }

    fun createDummyGetMyFavoriteArtistsResponse(): GetMyFavoriteArtistsResponse {

        return GetMyFavoriteArtistsResponse(
            href = faker.internet().url(),
            items = listOf(
                createDummySpotifyArtistFull(),
                createDummySpotifyArtistFull(),
                createDummySpotifyArtistFull()
            ),
            limit = faker.number().randomDigit(),
            offset = faker.number().randomDigit(),
            total = faker.number().randomDigit(),
            previous = faker.internet().url(),
            next = faker.internet().url()
        )
    }

    fun createDummySpotifyTrack(): SpotifyTrack {
        return SpotifyTrack(
            album = SpotifyAlbum(
                albumType = faker.lorem().word(),
                artists = listOf(
                    createDummySpotifyArtist(),
                    createDummySpotifyArtist(),
                    createDummySpotifyArtist()
                ),
                availableMarkets = listOf(
                    faker.lorem().word(),
                    faker.lorem().word()
                ),
                externalUrls = mapOf(
                    Pair(faker.lorem().word(), faker.internet().url())
                ),
                href = faker.internet().url(),
                id = faker.random().hex(),
                images = listOf(
                    createDummySpotifyImage()
                ),
                name = faker.lorem().word(),
                releaseDate = faker.date().toString(),
                releaseDatePrecision = faker.lorem().sentence(),
                totalTracks = faker.number().randomDigit(),
                type = faker.lorem().word(),
                uri = faker.internet().url()
            ),
            availableMarkets = listOf(
                faker.lorem().word()
            ),
            artists = listOf(
                createDummySpotifyArtist(),
                createDummySpotifyArtist()
            ),
            discNumber = faker.number().randomDigit(),
            durationMs = faker.number().randomDigit(),
            explicit = faker.bool().bool(),
            externalUrls = mapOf(
                Pair(faker.lorem().word(), faker.internet().url())
            ),
            externalIds = mapOf(
                Pair(faker.lorem().word(), faker.internet().url())
            ),
            href = faker.internet().url(),
            id = faker.random().hex(),
            isLocal = faker.bool().bool(),
            name = faker.name().fullName(),
            popularity = faker.number().randomDigit(),
            previewUrl = faker.internet().url()
        )

    }
}