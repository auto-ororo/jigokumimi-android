package com.ororo.auto.jigokumimi.util

import java.util.*

class Constants {
    companion object {
        const val CLIENT_ID = "66c88d5613964babb7d6ef7d63ffe5c2"
        const val AUTH_TOKEN_REQUEST_CODE = 0x10
        const val SPOTIFY_SDK_REDIRECT_SCHEME = "jigokumimi"
        const val SPOTIFY_SDK_REDIRECT_HOST = "main"

        const val SP_JIGOKUMIMI_USER_ID_KEY = "jigokumimiUserId"
        const val SP_JIGOKUMIMI_EMAIL_KEY = "jigokumimiEmail"
        const val SP_JIGOKUMIMI_PASSWORD_KEY = "jigokumimiPassword"
        const val SP_JIGOKUMIMI_TOKEN_KEY = "jigokumimiToken"
        const val SP_JIGOKUMIMI_TOKEN_EXPIRE_KEY = "jigokumimiTokenExpire"
        const val SP_JIGOKUMIMI_POSTED_FAVORITE_TRACKS_DATETIME_KEY =
            "jigokumimiPostedTracksDatetime"
        const val SP_JIGOKUMIMI_POSTED_FAVORITE_ARTISTS_DATETIME_KEY =
            "jigokumimiPostedArtistsDatetime"

        const val SP_SPOTIFY_EMAIL_KEY = "spotifyEmail"
        const val SP_SPOTIFY_PASSWORD_KEY = "spotifyPassword"
        const val SP_SPOTIFY_TOKEN_KEY = "spotifyToken"

        const val REQUEST_PERMISSION = 1000
        const val DEBUG_JIGOKUMIMI_BASE_URL = "http://192.168.0.12:10080/api/"
        const val RELEASE_JIGOKUMIMI_BASE_URL = "https://jigokumimi.net/api/"

        const val POST_MUSIC_PERIOD = 600000L

        const val DELETED_TRACK = "Deleted Track"
        const val DELETED_ARTIST = "Deleted Artist"

        const val MUSIC_LIST_SIZE = 25

    }

    enum class Type(val pathName: String) {
        TRACK("track"),
        ARTIST("artist");
    }
}