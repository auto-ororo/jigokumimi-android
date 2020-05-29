package com.ororo.auto.jigokumimi.util.demo

import android.content.Context
import android.content.res.AssetManager
import com.github.javafaker.Faker
import com.ororo.auto.jigokumimi.domain.Artist
import com.ororo.auto.jigokumimi.domain.History
import com.ororo.auto.jigokumimi.domain.HistoryItem
import com.ororo.auto.jigokumimi.domain.Track
import com.ororo.auto.jigokumimi.network.*
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*


class CreateDemoDataUtil(val context: Context) {
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

    fun createDummyHistoryItem(
        rank: Int = faker.number().randomDigit(),
        spotifyItemId: String = faker.random().hex()
    ): HistoryItem {
        return HistoryItem(
            rank = rank,
            popularity = faker.number().randomDigit(),
            spotifyItemId = spotifyItemId
        )
    }


    fun createDummyHistory(): History {
        return History(
            id = faker.random().hex(),
            latitude = faker.number().randomDouble(10, 2, 5),
            longitude = faker.number().randomDouble(10, 2, 5),
            distance = faker.number().randomDigit(),
            place = faker.lorem().characters(),
            createdAt = faker.date().toString(),
            historyItems = listOf(
                createDummyHistoryItem(),
                createDummyHistoryItem(),
                createDummyHistoryItem()
            )
        )
    }

    fun createDummyTrackHistoryItem(): TrackHistoryItem {
        return TrackHistoryItem(
            rank = faker.number().randomDigit(),
            popularity = faker.number().randomDigit(),
            spotifyTrackId = faker.random().hex()
        )
    }

    fun createDummyArtistHistoryItem(): ArtistHistoryItem {
        return ArtistHistoryItem(
            rank = faker.number().randomDigit(),
            popularity = faker.number().randomDigit(),
            spotifyArtistId = faker.random().hex()
        )
    }

    fun createDummyTrackHistory(): TrackHistory {
        return TrackHistory(
            id = faker.random().hex(),
            latitude = faker.number().randomDouble(10, 2, 5),
            longitude = faker.number().randomDouble(10, 2, 5),
            distance = faker.number().randomDigit(),
            createdAt = faker.date().toString(),
            tracksAroundHistories = listOf(
                createDummyTrackHistoryItem(),
                createDummyTrackHistoryItem(),
                createDummyTrackHistoryItem()
            ),
            userId = faker.random().hex()
        )
    }

    fun createDummyArtistHistory(): ArtistHistory {
        return ArtistHistory(
            id = faker.random().hex(),
            latitude = faker.number().randomDouble(10, 2, 5),
            longitude = faker.number().randomDouble(10, 2, 5),
            distance = faker.number().randomDigit(),
            createdAt = faker.date().toString(),
            artistsAroundHistories = listOf(
                createDummyArtistHistoryItem(),
                createDummyArtistHistoryItem(),
                createDummyArtistHistoryItem()
            ),
            userId = faker.random().hex()
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
            id = faker.random().hex(),
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

    fun createDummyTrack(): Track {
        return Track(
            album = faker.lorem().word(),
            artists = faker.name().fullName(),
            id = faker.random().hex(),
            imageUrl = faker.internet().url(),
            name = faker.name().fullName(),
            popularity = faker.number().randomDigit(),
            previewUrl = faker.internet().url(),
            rank = faker.number().randomDigit(),
            isSaved = faker.bool().bool(),
            isDeleted = false
        )
    }

    fun createDummyArtist(): Artist {
        return Artist(
            id = faker.random().hex(),
            imageUrl = faker.internet().url(),
            name = faker.name().fullName(),
            popularity = faker.number().randomDigit(),
            rank = faker.number().randomDigit(),
            genres = faker.lorem().word(),
            isFollowed = faker.bool().bool(),
            isDeleted = false
        )
    }

    fun createDummySavedList(): List<Boolean> {
        return listOf(
            faker.bool().bool()
        )
    }

    fun createDummyTrackList(): List<Track> {

        val trackList = mutableListOf<Track>()

        // Csv読み込み
        val assetManager: AssetManager = context.resources.assets
        val inputStream: InputStream = assetManager.open("demoTrackList.csv")
        val inputStreamReader = InputStreamReader(inputStream)
        val bufferReader = BufferedReader(inputStreamReader)
        var line: String

        // 読み込んだCSVを元にデモ用Trackリストを作成
        try {
            while (bufferReader.readLine().also { line = it } != null) {
                val rowData = line.split(",").toTypedArray()

                trackList.add(
                    Track(
                        rank = rowData[0].toInt(),
                        name = rowData[1],
                        album = rowData[2],
                        artists = rowData[3],
                        imageUrl = rowData[4],
                        previewUrl = rowData[5],
                        popularity = faker.number().randomDigit(),
                        id = faker.random().hex(),
                        isSaved = false,
                        isDeleted = false
                    )
                )
            }
            bufferReader.close()

        } catch (e: Exception) {
            Timber.d(e.message)
        }

        return trackList
    }


    fun createDummyArtistList(): List<Artist> {

        val artistList = mutableListOf<Artist>()

        // Csv読み込み
        val assetManager: AssetManager = context.resources.assets
        val inputStream: InputStream = assetManager.open("demoArtistList.csv")
        val inputStreamReader = InputStreamReader(inputStream)
        val bufferReader = BufferedReader(inputStreamReader)
        var line: String

        // 読み込んだCSVを元にデモ用Trackリストを作成
        try {
            while (bufferReader.readLine().also { line = it } != null) {
                val rowData = line.split(",").toTypedArray()

                artistList.add(
                    Artist(
                        rank = rowData[0].toInt(),
                        name = rowData[1],
                        genres = rowData[2],
                        imageUrl = rowData[3],
                        popularity = faker.number().randomDigit(),
                        id = faker.random().hex(),
                        isFollowed = false,
                        isDeleted = false

                    )
                )
            }
            bufferReader.close()

        } catch (e: Exception) {
            Timber.d(e.message)
        }

        return artistList
    }

     fun createDummyTrackHistoryList(): List<TrackHistory> {

        val trackHistoryList = mutableListOf<TrackHistory>()

        // Csv読み込み
        val assetManager: AssetManager = context.resources.assets
        val inputStream: InputStream = assetManager.open("demoTrackHistoryList.csv")
        val inputStreamReader = InputStreamReader(inputStream)
        val bufferReader = BufferedReader(inputStreamReader)
        var line: String

        // 読み込んだCSVを元にデモ用履歴リストを作成
        try {
            while (bufferReader.readLine().also { line = it } != null) {
                val rowData = line.split(",").toTypedArray()

                trackHistoryList.add(
                    TrackHistory(
                        id = rowData[0],
                        userId = rowData[1],
                        latitude = rowData[2].toDouble(),
                        longitude = rowData[3].toDouble(),
                        distance = rowData[4].toInt(),
                        createdAt = rowData[5],
                        tracksAroundHistories = listOf()
                    )
                )
            }
            bufferReader.close()

        } catch (e: Exception) {
            Timber.d(e.message)
        }

        return trackHistoryList
    }

    fun createDummyArtistHistoryList(): List<ArtistHistory> {

        val artistHistoryList = mutableListOf<ArtistHistory>()

        // Csv読み込み
        val assetManager: AssetManager = context.resources.assets
        val inputStream: InputStream = assetManager.open("demoArtistHistoryList.csv")
        val inputStreamReader = InputStreamReader(inputStream)
        val bufferReader = BufferedReader(inputStreamReader)
        var line: String

        // 読み込んだCSVを元にデモ用履歴リストを作成
        try {
            while (bufferReader.readLine().also { line = it } != null) {
                val rowData = line.split(",").toTypedArray()

                artistHistoryList.add(
                    ArtistHistory(
                        id = rowData[0],
                        userId = rowData[1],
                        latitude = rowData[2].toDouble(),
                        longitude = rowData[3].toDouble(),
                        distance = rowData[4].toInt(),
                        createdAt = rowData[5],
                        artistsAroundHistories = listOf()
                    )
                )
            }
            bufferReader.close()

        } catch (e: Exception) {
            Timber.d(e.message)
        }

        return artistHistoryList
    }
}