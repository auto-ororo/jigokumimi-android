package com.ororo.auto.jigokumimi.util

class Constants {
    companion object {
        const val CLIENT_ID = "664aaf9b8611444ba447fd77e2faa0fc"
        const val CLIENT_SECRET = "07c946086c3746dcb7a4555c60872d13"
        const val AUTH_TOKEN_REQUEST_CODE = 0x10
        const val SPOTIFY_SDK_REDIRECT_SCHEME = "yourscheme"
        const val SPOTIFY_SDK_REDIRECT_HOST = "yourhost"

        const val SP_JIGOKUMIMI_USER_ID_KEY = "jigokumimiUserId"
        const val SP_JIGOKUMIMI_EMAIL_KEY = "jigokumimiEmail"
        const val SP_JIGOKUMIMI_PASSWORD_KEY = "jigokumimiPassword"
        const val SP_JIGOKUMIMI_TOKEN_KEY = "jigokumimiToken"
        const val SP_JIGOKUMIMI_TOKEN_EXPIRE_KEY = "jigokumimiTokenExpire"

        const val SP_SPOTIFY_EMAIL_KEY = "spotifyEmail"
        const val SP_SPOTIFY_PASSWORD_KEY = "spotifyPassword"
        const val SP_SPOTIFY_TOKEN_KEY = "spotifyToken"

    }

    enum class SearchType {
        TRACK,
        ARTIST
    }
}