package com.ororo.auto.jigokumimi.util

class Constants {
    companion object {
        const val SPOTIFY_SDK_REDIRECT_SCHEME = "jigokumimi"
        const val SPOTIFY_SDK_REDIRECT_HOST = "main"

        const val SP_SPOTIFY_TOKEN_KEY = "spotifyToken"

        const val DELETED_TRACK = "Deleted Track"
        const val DELETED_ARTIST = "Deleted Artist"
        const val MUSIC_LIST_SIZE = 25
    }

    enum class Type(val pathName: String) {
        TRACK("track"),
        ARTIST("artist");
    }
}