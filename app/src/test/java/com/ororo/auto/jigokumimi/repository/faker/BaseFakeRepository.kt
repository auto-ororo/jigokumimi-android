package com.ororo.auto.jigokumimi.repository.faker

import com.github.javafaker.Faker
import com.ororo.auto.jigokumimi.util.CreateTestDataUtil
import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.util.*

open class BaseFakeRepository(private val exception: Exception? = null) {

    protected val faker = Faker(Locale("jp_JP"))

    protected val testDataUtil = CreateTestDataUtil()

    protected fun launchExceptionByErrorMode() {
        if (exception != null ) {
            throw exception
        }
    }
}