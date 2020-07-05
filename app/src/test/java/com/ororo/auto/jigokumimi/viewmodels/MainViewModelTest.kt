package com.ororo.auto.jigokumimi.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.util.MockkHelper.Companion.any
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
class MainViewModelTest {

    lateinit var authRepository: IAuthRepository
    lateinit var viewModel: MainViewModel

    // LiveDataのテストに必要なルールを設定
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createViewModel() {
        authRepository = mockk(relaxed = true)
        viewModel = MainViewModel(
            ApplicationProvider.getApplicationContext(),
            authRepository
        )
    }

    @After
    fun shutDownWebServer() {
        stopKoin()
    }

    @Test
    fun logout_authrepositoryのlogoutが呼ばれること() {

        every { runBlocking { authRepository.logoutJigokumimi() } } returns any()

        viewModel.logout()

        verify { runBlocking { authRepository.logoutJigokumimi() } }
    }
}