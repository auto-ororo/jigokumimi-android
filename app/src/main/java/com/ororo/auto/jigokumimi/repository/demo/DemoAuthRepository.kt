package com.ororo.auto.jigokumimi.repository.demo

import android.app.Application
import com.github.javafaker.Faker
import com.ororo.auto.jigokumimi.network.*
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.util.demo.CreateDemoDataUtil
import java.util.*

class DemoAuthRepository(
    private val app: Application
) : IAuthRepository {

    val faker = Faker(Locale("jp_JP"))

    val cd = CreateDemoDataUtil(app)

    override suspend fun signUpJigokumimi(signUpRequest: SignUpRequest): SignUpResponse {
        return SignUpResponse(
            data = null,
            message = faker.lorem().characters()
        )
    }

    override suspend fun loginJigokumimi(email: String, password: String) {
    }

    override fun getSavedLoginInfo(): Pair<String, String> {
        return Pair(faker.internet().emailAddress(), faker.random().hex())
    }

    override fun getSavedJigokumimiUserId(): String {
        return faker.random().hex()
    }

    override suspend fun logoutJigokumimi() {
    }

    override suspend fun getJigokumimiUserProfile(): GetMeResponse {
        return GetMeResponse(
            data = cd.createDummyJigokumimiUserProfile(),
            message = faker.lorem().characters()
        )
    }

    override suspend fun unregisterJigokumimiUser(): CommonResponse {
        return CommonResponse(
            data = null,
            message = faker.lorem().characters()
        )
    }

    override suspend fun changeJigokumimiPassword(changePasswordRequest: ChangePasswordRequest): CommonResponse {
        return CommonResponse(
            data = null,
            message = faker.lorem().characters()
        )
    }

    override suspend fun refreshSpotifyAuthToken(token: String) {
    }

    override suspend fun getSpotifyUserProfile(): SpotifyUserResponse {
        return cd.createDummySpotifyUserResponse()
    }
}