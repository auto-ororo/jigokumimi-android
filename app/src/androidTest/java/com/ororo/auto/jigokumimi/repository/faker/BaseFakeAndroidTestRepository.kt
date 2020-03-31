package com.ororo.auto.jigokumimi.repository.faker

import com.github.javafaker.Faker
import com.ororo.auto.jigokumimi.util.CreateAndroidTestDataUtil
import java.util.*

open class BaseFakeAndroidTestRepository(private val exception: Exception? = null) {

    protected val faker = Faker(Locale("jp_JP"))

    protected val testDataUtil = CreateAndroidTestDataUtil()

    protected fun launchExceptionByErrorMode() {
        if (exception != null) {
            throw exception
        }
    }
}