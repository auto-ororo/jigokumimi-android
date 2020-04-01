package com.ororo.auto.jigokumimi.repository.test

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.javafaker.Faker
import com.ororo.auto.jigokumimi.network.*
import com.ororo.auto.jigokumimi.repository.AuthRepository
import com.ororo.auto.jigokumimi.util.Constants
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class AuthRepositoryTest {


    lateinit var prefData: SharedPreferences
    lateinit var jigokumimiApiService: FakeJigokumimiApiService
    lateinit var spotifyApiService: FakeSpotifyApiService
    lateinit var authRepository: AuthRepository
    lateinit var faker: Faker


    @Before
    fun createRepository() {
        prefData =
            PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
        jigokumimiApiService = FakeJigokumimiApiService()
        spotifyApiService = FakeSpotifyApiService()
        // Get a reference to the class under test
        authRepository = AuthRepository(
            prefData,
            jigokumimiApiService,
            spotifyApiService
        )
        faker = Faker(Locale("ja_JP"))
    }

    @Test
    fun loginJigokumimi_SharedPreferencesにログイン情報が保存されること() = runBlocking {

        val expectedResponse = LoginResponse(
            message = faker.lorem().word(),
            data = Token(
                tokenType = faker.lorem().word(),
                accessToken = faker.random().hex(),
                expiresIn = faker.number().randomDigit()
            )
        )

        jigokumimiApiService.loginResponse = expectedResponse

        val userEmail = faker.internet().safeEmailAddress()
        val userPassword = faker.internet().password()

        authRepository.loginJigokumimi(userEmail, userPassword)

        // SharedPreferenceに保存した値を確認
        assertThat(userEmail, IsEqual(prefData.getString(Constants.SP_JIGOKUMIMI_EMAIL_KEY, "")))
        assertThat(
            userPassword,
            IsEqual(prefData.getString(Constants.SP_JIGOKUMIMI_PASSWORD_KEY, ""))
        )
        assertThat(
            "Bearer ${expectedResponse.data.accessToken}",
            IsEqual(prefData.getString(Constants.SP_JIGOKUMIMI_TOKEN_KEY, ""))
        )
        assertThat(
            expectedResponse.data.expiresIn,
            IsEqual(prefData.getInt(Constants.SP_JIGOKUMIMI_TOKEN_EXPIRE_KEY, 0))
        )
    }

    @Test
    fun getSavedLoginInfo_メールとパスワードをSharedPreferencesから取得できること() {

        val userEmail = faker.internet().safeEmailAddress()
        val userPassword = faker.internet().password()

        // SharedPreferencesを設定
        prefData.edit().let {
            it.putString(Constants.SP_JIGOKUMIMI_EMAIL_KEY, userEmail)!!
            it.putString(Constants.SP_JIGOKUMIMI_PASSWORD_KEY, userPassword)!!
            it.apply()
        }

        // メソッド呼び出し
        val (email, password) = authRepository.getSavedLoginInfo()

        // 取得したEmail、PasswordがSharedPreferencesに設定した値と等しいことを確認
        assertThat(email, IsEqual(userEmail))
        assertThat(password, IsEqual(userPassword))

    }

    @Test
    fun logoutJigokumimi_SharedPreferencesに保存されたログイン情報が初期化されること() = runBlocking {

        // SharedPreferencesを設定
        prefData.edit().let {
            it.putString(Constants.SP_JIGOKUMIMI_EMAIL_KEY, faker.internet().safeEmailAddress())!!
            it.putString(Constants.SP_JIGOKUMIMI_PASSWORD_KEY, faker.internet().password())!!
            it.putString(Constants.SP_SPOTIFY_TOKEN_KEY, faker.random().hex())!!
            it.putInt(Constants.SP_JIGOKUMIMI_TOKEN_EXPIRE_KEY, faker.number().randomDigit())!!
            it.apply()
        }

        val expectedResponse = LogoutResponse(
            message = faker.lorem().word(),
            data = null
        )
        jigokumimiApiService.logoutResponse = expectedResponse

        // メソッド呼び出し
        authRepository.logoutJigokumimi()

        // SharedPreferencesが初期化されていることを確認
        assertThat(
            prefData.getString(Constants.SP_JIGOKUMIMI_PASSWORD_KEY, ""),
            IsEqual("")
        )
        assertThat(
            prefData.getString(Constants.SP_JIGOKUMIMI_EMAIL_KEY, ""),
            IsEqual("")
        )
        assertThat(
            prefData.getString(Constants.SP_JIGOKUMIMI_TOKEN_KEY, ""),
            IsEqual("")
        )
        assertThat(
            prefData.getInt(Constants.SP_JIGOKUMIMI_TOKEN_EXPIRE_KEY, 0),
            IsEqual(0)
        )
    }

    @Test
    fun refreshSpotifyAuthToken_SharedPreferencesにSpotifyのアクセストークンが保存されていること() = runBlocking {

        val userAccessToken = faker.random().hex()

        // メソッド呼び出し
        authRepository.refreshSpotifyAuthToken(userAccessToken)

        // トークンがSharedPreferencesに設定されていることを確認
        assertThat(
            "Bearer $userAccessToken",
            IsEqual(prefData.getString(Constants.SP_SPOTIFY_TOKEN_KEY, ""))
        )
    }
}