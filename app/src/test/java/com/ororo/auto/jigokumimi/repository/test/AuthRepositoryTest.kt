package com.ororo.auto.jigokumimi.repository.test

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.javafaker.Faker
import com.ororo.auto.jigokumimi.network.*
import com.ororo.auto.jigokumimi.repository.AuthRepository
import com.ororo.auto.jigokumimi.util.Constants
import com.ororo.auto.jigokumimi.util.MockkHelper.Companion.any
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import java.util.*

@RunWith(AndroidJUnit4::class)
class AuthRepositoryTest {


    lateinit var prefData: SharedPreferences
    lateinit var jigokumimiApiService: JigokumimiApiService
    lateinit var spotifyApiService: SpotifyApiService
    lateinit var authRepository: AuthRepository
    lateinit var faker: Faker


    @Before
    fun createRepository() {
        prefData =
            PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
        jigokumimiApiService = mockk(relaxed = true)
        spotifyApiService = mockk(relaxed = true)
        // Get a reference to the class under test
        authRepository = AuthRepository(
            prefData,
            jigokumimiApiService,
            spotifyApiService
        )
        faker = Faker(Locale("ja_JP"))
    }

    @After
    fun shutDownWebServer() {
        stopKoin()
    }


    @Test
    fun signUpJigokumimi_新規登録リクエストが呼ばれること() = runBlocking {

        authRepository.signUpJigokumimi(any())

        verify {
            runBlocking {
                jigokumimiApiService.signUp(any())
            }
        }
    }

    @Test
    fun loginJigokumimi_SharedPreferencesにログイン情報が保存されること() = runBlocking {

        // APIのレスポンス作成
        val expectedResponse = LoginResponse(
            message = faker.lorem().word(),
            data = JigokumimiToken(
                id = faker.random().hex(),
                tokenType = faker.lorem().word(),
                accessToken = faker.random().hex(),
                expiresIn = faker.number().randomDigit()
            )
        )

        val userEmail = faker.internet().safeEmailAddress()
        val userPassword = faker.internet().password()

        every {
            runBlocking {
                jigokumimiApiService.login(any())
            }
        } returns expectedResponse

        authRepository.loginJigokumimi(userEmail, userPassword)

        // ログインリクエストが呼ばれることを確認
        verify {
            runBlocking {
                jigokumimiApiService.login(any())
            }
        }

        // SharedPreferenceに保存した値を確認
        assertThat(
            expectedResponse.data.id,
            IsEqual(prefData.getString(Constants.SP_JIGOKUMIMI_USER_ID_KEY, ""))
        )
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
    fun getSavedJigokumimiUserId_ユーザーIDをSharedPreferencesから取得できること() {

        val userId = faker.random().hex()

        // SharedPreferencesを設定
        prefData.edit().let {
            it.putString(Constants.SP_JIGOKUMIMI_USER_ID_KEY, userId)!!
            it.apply()
        }

        // メソッド呼び出し
        val resUserId = authRepository.getSavedJigokumimiUserId()

        // 取得したEmail、PasswordがSharedPreferencesに設定した値と等しいことを確認
        assertThat(resUserId, IsEqual(userId))

    }

    @Test
    fun logoutJigokumimi_SharedPreferencesに保存されたログイン情報が初期化されること() = runBlocking {

        // SharedPreferencesを設定
        prefData.edit().let {
            it.putString(Constants.SP_JIGOKUMIMI_USER_ID_KEY, faker.random().hex())!!
            it.putString(Constants.SP_JIGOKUMIMI_EMAIL_KEY, faker.internet().safeEmailAddress())!!
            it.putString(Constants.SP_JIGOKUMIMI_PASSWORD_KEY, faker.internet().password())!!
            it.putString(Constants.SP_SPOTIFY_TOKEN_KEY, faker.random().hex())!!
            it.putInt(Constants.SP_JIGOKUMIMI_TOKEN_EXPIRE_KEY, faker.number().randomDigit())!!
            it.apply()
        }

        // モック設定
        val expectedResponse = LogoutResponse(
            message = faker.lorem().word(),
            data = null
        )
        every {
            runBlocking {
                jigokumimiApiService.logout(any())
            }
        } returns expectedResponse

        // メソッド呼び出し
        authRepository.logoutJigokumimi()

        // ログインリクエストが呼ばれることを確認
        verify {
            runBlocking {
                jigokumimiApiService.logout(any())
            }
        }

        // SharedPreferencesが初期化されていることを確認
        assertThat(
            prefData.getString(Constants.SP_JIGOKUMIMI_USER_ID_KEY, ""),
            IsEqual("")
        )
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
    fun getJigokumimiUserProfile_APIリクエストが呼ばれること() = runBlocking {

        authRepository.getJigokumimiUserProfile()

        // APIリクエストが呼ばれることを確認
        verify {
            runBlocking {
                jigokumimiApiService.getProfile(any())
            }
        }
    }

    @Test
    fun changeJigokumimiPassword_APIリクエストが呼ばれること() = runBlocking {

        authRepository.changeJigokumimiPassword(any())

        // APIリクエストが呼ばれることを確認
        verify {
            runBlocking {
                jigokumimiApiService.changePassword(any(),any())
            }
        }
    }


    @Test
    fun unregisterJigokumimiUser_SharedPreferencesに保存されたログイン情報が初期化されること() = runBlocking {

        // SharedPreferencesを設定
        prefData.edit().let {
            it.putString(Constants.SP_JIGOKUMIMI_USER_ID_KEY, faker.random().hex())!!
            it.putString(Constants.SP_JIGOKUMIMI_EMAIL_KEY, faker.internet().safeEmailAddress())!!
            it.putString(Constants.SP_JIGOKUMIMI_PASSWORD_KEY, faker.internet().password())!!
            it.putString(Constants.SP_SPOTIFY_TOKEN_KEY, faker.random().hex())!!
            it.putInt(Constants.SP_JIGOKUMIMI_TOKEN_EXPIRE_KEY, faker.number().randomDigit())!!
            it.apply()
        }

        // モック設定
        every {
            runBlocking {
                jigokumimiApiService.unregisterUser(any())
            }
        } returns any()

        // メソッド呼び出し
        authRepository.unregisterJigokumimiUser()

        // APIリクエストが呼ばれることを確認
        verify {
            runBlocking {
                jigokumimiApiService.unregisterUser(any())
            }
        }

        // SharedPreferencesが初期化されていることを確認
        assertThat(
            prefData.getString(Constants.SP_JIGOKUMIMI_USER_ID_KEY, ""),
            IsEqual("")
        )
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

    @Test
    fun getSpotifyUserProfile_APIリクエストが呼ばれること() = runBlocking {

        authRepository.getSpotifyUserProfile()

        // APIリクエストが呼ばれることを確認
        verify {
            runBlocking {
                spotifyApiService.getUserProfile(any())
            }
        }
    }
}