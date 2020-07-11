package com.ororo.auto.jigokumimi.repository.demo

import android.app.Application
import com.github.javafaker.Faker
import com.ororo.auto.jigokumimi.network.*
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.util.demo.CreateDemoDataUtil
import kotlinx.coroutines.delay
import java.util.*

class DemoAuthRepository(
    private val app: Application
) : IAuthRepository {

    val faker = Faker(Locale("jp_JP"))

    val cd = CreateDemoDataUtil(app)

    override suspend fun signUpJigokumimi(signUpRequest: SignUpRequest): SignUpResponse {
        delay(1000)
        return SignUpResponse(
            data = null,
            message = faker.lorem().characters()
        )
    }

    override suspend fun loginJigokumimi(email: String, password: String) {
        delay(1000)
    }

    override fun getSavedLoginInfo(): Pair<String, String> {
        return Pair(faker.internet().emailAddress(), faker.random().hex())
    }

    override fun getSavedJigokumimiUserId(): String {
        return faker.random().hex()
    }

    override suspend fun logoutJigokumimi() {
        delay(1000)
    }

    override suspend fun getJigokumimiUserProfile(): GetMeResponse {
        delay(1000)
        return GetMeResponse(
            data = cd.createDummyJigokumimiUserProfile(),
            message = faker.lorem().characters()
        )
    }

    override suspend fun unregisterJigokumimiUser(): CommonResponse {
        delay(1000)
        return CommonResponse(
            data = null,
            message = faker.lorem().characters()
        )
    }

    override suspend fun changeJigokumimiPassword(changePasswordRequest: ChangePasswordRequest): CommonResponse {
        delay(1000)
        return CommonResponse(
            data = null,
            message = faker.lorem().characters()
        )
    }

    override suspend fun refreshSpotifyAuthToken(token: String) {
        delay(1000)
    }

    override suspend fun getSpotifyUserProfile(): SpotifyUserResponse {
        delay(1000)
        return cd.createDummySpotifyUserResponse()
    }
}