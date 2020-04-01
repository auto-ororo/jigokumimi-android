package com.ororo.auto.jigokumimi.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.javafaker.Faker
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.domain.Artist
import com.ororo.auto.jigokumimi.domain.Track
import com.ororo.auto.jigokumimi.repository.faker.FakeAuthRepository
import com.ororo.auto.jigokumimi.repository.faker.FakeLocationRepository
import com.ororo.auto.jigokumimi.repository.faker.FakeMusicRepository
import com.ororo.auto.jigokumimi.util.CreateTestDataUtil
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.hamcrest.core.IsEqual
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.util.*

@RunWith(AndroidJUnit4::class)
class SearchViewModelTest {

    // LiveDataのテストに必要なルールを設定
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    lateinit var viewModel: SearchViewModel

    val faker = Faker(Locale("jp_JP"))

    val testDataUtil = CreateTestDataUtil()

    @Before
    fun createViewModel() {

        val tracks = mutableListOf<Track>()
        val artists = mutableListOf<Artist>()

        for (i in 1..3) {
            tracks.add(testDataUtil.createDummyTrack())
            artists.add(testDataUtil.createDummyArtist())
        }

        val latitude = faker.number().randomDouble(10, 2, 6)
        val longitude = faker.number().randomDouble(10, 2, 6)

        viewModel = SearchViewModel(
            ApplicationProvider.getApplicationContext(),
            FakeAuthRepository(),
            FakeMusicRepository(_tracks = tracks, _artists = artists),
            FakeLocationRepository(latitude, longitude)
        )
    }

    @Test
    fun searchMusic_Artist_アーティスト情報が送受信され検索完了フラグがtrueになること() = runBlocking {

        viewModel.setSearchTypeToArtist()
        viewModel.searchMusic()

        assertThat(viewModel.isSearchFinished.value, IsEqual(true))
    }

    @Test
    fun searchMusic_Track_曲情報が送受信され検索完了フラグがtrueになること() = runBlocking {

        viewModel.setSearchTypeToTrack()
        viewModel.searchMusic()

        assertThat(viewModel.isSearchFinished.value, IsEqual(true))
    }

    @Test
    fun searchMusic_HTTPException400_レスポンスのメッセージが設定され検索完了フラグがfalseになること() = runBlocking {

        val message = faker.lorem().sentence()

        // 発生させるExceptionを生成
        val exception = HttpException(
            Response.error<Any>(
                400, ResponseBody.create(
                    MediaType.parse("application/json"),
                    "{\"message\":\"$message\"}"
                )
            )
        )

        val latitude = faker.number().randomDouble(10, 2, 6)
        val longitude = faker.number().randomDouble(10, 2, 6)

        viewModel = SearchViewModel(
            ApplicationProvider.getApplicationContext(),
            FakeAuthRepository(),
            FakeMusicRepository(exception = exception),
            FakeLocationRepository(latitude, longitude)
        )

        viewModel.searchMusic()

        assertThat(viewModel.errorMessage.value, IsEqual(message))
        assertThat(viewModel.isErrorDialogShown.value, IsEqual(true))
        assertThat(viewModel.isSearchFinished.value, IsEqual(false))
    }


    @Test
    fun searchMusic_HTTPException401_定型メッセージが設定され検索完了フラグがfalseになること() = runBlocking {

        val message = faker.lorem().sentence()

        // 発生させるExceptionを生成
        val exception = HttpException(
            Response.error<Any>(
                401, ResponseBody.create(
                    MediaType.parse("application/json"),
                    "{\"message\":\"$message\"}"
                )
            )
        )

        val latitude = faker.number().randomDouble(10, 2, 6)
        val longitude = faker.number().randomDouble(10, 2, 6)

        viewModel = SearchViewModel(
            ApplicationProvider.getApplicationContext(),
            FakeAuthRepository(),
            FakeMusicRepository(exception = exception),
            FakeLocationRepository(latitude, longitude)
        )

        viewModel.searchMusic()

        val extectedMessage =
            InstrumentationRegistry.getInstrumentation().context.resources.getString(
                R.string.token_expired_error_message
            )

        assertThat(viewModel.errorMessage.value, IsEqual(extectedMessage))
        assertThat(viewModel.isErrorDialogShown.value, IsEqual(true))
        assertThat(viewModel.isSearchFinished.value, IsEqual(false))
    }

    @Test
    fun searchMusic_IOException_定型メッセージが設定され検索完了フラグがfalseになること() = runBlocking {

        // 発生させるExceptionを生成
        val exception = IOException()

        val latitude = faker.number().randomDouble(10, 2, 6)
        val longitude = faker.number().randomDouble(10, 2, 6)

        viewModel = SearchViewModel(
            ApplicationProvider.getApplicationContext(),
            FakeAuthRepository(),
            FakeMusicRepository(exception = exception),
            FakeLocationRepository(latitude, longitude)
        )

        viewModel.searchMusic()

        val extectedMessage =
            InstrumentationRegistry.getInstrumentation().context.resources.getString(
                R.string.no_connection_error_message
            )

        assertThat(viewModel.errorMessage.value, IsEqual(extectedMessage))
        assertThat(viewModel.isErrorDialogShown.value, IsEqual(true))
        assertThat(viewModel.isSearchFinished.value, IsEqual(false))
    }


    @Test
    fun searchMusic_Exception_定型メッセージが設定され検索完了フラグがfalseになること() = runBlocking {

        // 発生させるExceptionを生成
        val exception = Exception()

        val latitude = faker.number().randomDouble(10, 2, 6)
        val longitude = faker.number().randomDouble(10, 2, 6)

        viewModel = SearchViewModel(
            ApplicationProvider.getApplicationContext(),
            FakeAuthRepository(),
            FakeMusicRepository(exception = exception),
            FakeLocationRepository(latitude, longitude)
        )

        viewModel.searchMusic()

        val extectedMessage =
            InstrumentationRegistry.getInstrumentation().context.resources.getString(
                R.string.general_error_message, exception::class.java
            )

        assertThat(viewModel.errorMessage.value, IsEqual(extectedMessage))
        assertThat(viewModel.isErrorDialogShown.value, IsEqual(true))
        assertThat(viewModel.isSearchFinished.value, IsEqual(false))
    }

    @Test
    fun setDistanceFromSelectedSpinnerString_末尾文字列mが切り取られInt型に変換されること() {

        val testData = "100m"

        viewModel.setDistanceFromSelectedSpinnerString(testData)

        assertThat(viewModel.distance.value, IsEqual(100))
    }

}