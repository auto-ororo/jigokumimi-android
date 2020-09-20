package com.ororo.auto.jigokumimi.repository.faker

import com.ororo.auto.jigokumimi.network.*
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import java.lang.Exception

class FakeAuthRepository(exception: Exception? = null) : IAuthRepository, BaseFakeRepository(exception){

    override suspend fun signUpJigokumimi(signUpRequest: SignUpRequest): SignUpResponse {
        launchExceptionByErrorMode()
        return SignUpResponse(
            data = null,
            message = faker.lorem().word()
        )
    }

    override suspend fun loginJigokumimi(email: String, password: String) {
        launchExceptionByErrorMode()
    }

    override fun getSavedLoginInfo(): Pair<String, String> {
        launchExceptionByErrorMode()
        return Pair(faker.internet().url(), faker.random().hex())
    }

    override fun getSavedJigokumimiUserId(): String {
        return faker.random().hex()
    }

    override suspend fun logoutJigokumimi() {
        launchExceptionByErrorMode()
    }

    override suspend fun getJigokumimiUserProfile(): GetMeResponse {
        launchExceptionByErrorMode()
        return testDataUtil.createDummyGetMeResponse()
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
        launchExceptionByErrorMode()
    }

    override suspend fun getSpotifyUserId(): SpotifyUserResponse {
        launchExceptionByErrorMode()

        return testDataUtil.createDummySpotifyUserResponse()
    }
}