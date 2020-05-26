package com.ororo.auto.jigokumimi.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.javafaker.Faker
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.domain.Track
import com.ororo.auto.jigokumimi.network.GetArtistSearchHistoryResponse
import com.ororo.auto.jigokumimi.network.GetTrackSearchHistoryResponse
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.repository.ILocationRepository
import com.ororo.auto.jigokumimi.repository.IMusicRepository
import com.ororo.auto.jigokumimi.util.Constants
import com.ororo.auto.jigokumimi.util.CreateTestDataUtil
import getOrAwaitValue
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot
import org.hamcrest.core.IsNull
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import java.lang.Exception
import java.util.*

@RunWith(AndroidJUnit4::class)
class HistoryViewModelTest {

    lateinit var viewModel: HistoryViewModel
    lateinit var musicRepository: IMusicRepository
    lateinit var authRepository: IAuthRepository
    lateinit var locationRepository: ILocationRepository

    val faker = Faker(Locale("jp_JP"))

    val testDataUtil = CreateTestDataUtil()

    lateinit var returnTrackSearchHistoryResponse: GetTrackSearchHistoryResponse
    lateinit var returnArtistSearchHistoryResponse: GetArtistSearchHistoryResponse

    // LiveDataのテストに必要なルールを設定
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createViewModel() {
        val tracks = mutableListOf<Track>()


        musicRepository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
        locationRepository = mockk(relaxed = true)

        viewModel = spyk(
            HistoryViewModel(
                ApplicationProvider.getApplicationContext(),
                musicRepository,
                authRepository,
                locationRepository
            )
        )

        // ダミーデータ作成
        returnTrackSearchHistoryResponse = GetTrackSearchHistoryResponse(
            message = faker.lorem().characters(),
            data = listOf(
                testDataUtil.createDummyTrackHistory(),
                testDataUtil.createDummyTrackHistory(),
                testDataUtil.createDummyTrackHistory()
            )
        )
        returnArtistSearchHistoryResponse = GetArtistSearchHistoryResponse(
            message = faker.lorem().characters(),
            data = listOf(
                testDataUtil.createDummyArtistHistory(),
                testDataUtil.createDummyArtistHistory(),
                testDataUtil.createDummyArtistHistory()

            )
        )

        every { runBlocking { musicRepository.getTracksAroundSearchHistories(any()) } } returns returnTrackSearchHistoryResponse
        every { runBlocking { musicRepository.getArtistsAroundSearchHistories(any()) } } returns returnArtistSearchHistoryResponse
        every { runBlocking { authRepository.getSavedJigokumimiUserId() } } returns any()
    }

    @Test
    fun getSearchHistories_Track_Track検索履歴のLivedataが更新されること() = runBlocking {

        // メソッド呼び出し
        viewModel.getSearchHistories(Constants.SearchType.TRACK)

        // UserIdが取得されることを確認
        verify { runBlocking { authRepository.getSavedJigokumimiUserId() } }

        // 履歴情報が正しく設定されていることを確認
        val list = viewModel.trackHistoryList.getOrAwaitValue()
        list.mapIndexed { i, history ->
            returnTrackSearchHistoryResponse.data!![i].let {
                assertThat(history.id, IsEqual(it.id))
                assertThat(history.latitude, IsEqual(it.latitude))
                assertThat(history.longitude, IsEqual(it.longitude))
                assertThat(history.distance, IsEqual(it.distance))
                assertThat(history.createdAt, IsEqual(it.createdAt))
                assertThat(history.place, IsNot(IsNull()))

                it.tracksAroundHistories!!.mapIndexed { index, trackHistoryItem ->
                    assertThat(
                        history.historyItems!![index].spotifyItemId,
                        IsEqual(trackHistoryItem.spotifyTrackId)
                    )
                    assertThat(history.historyItems!![index].rank, IsEqual(trackHistoryItem.rank))
                    assertThat(
                        history.historyItems!![index].popularity,
                        IsEqual(trackHistoryItem.popularity)
                    )
                }
            }
        }
        return@runBlocking
    }

    @Test
    fun getSearchHistories_Artist_Artist検索履歴のLivedataが更新されること() = runBlocking {

        // メソッド呼び出し
        viewModel.getSearchHistories(Constants.SearchType.ARTIST)

        // UserIdが取得されることを確認
        verify { runBlocking { authRepository.getSavedJigokumimiUserId() } }

        // 履歴情報が正しく設定されていることを確認
        val list = viewModel.artistHistoryList.getOrAwaitValue()
        list.mapIndexed { i, history ->
            returnArtistSearchHistoryResponse.data!![i].let {
                assertThat(history.id, IsEqual(it.id))
                assertThat(history.latitude, IsEqual(it.latitude))
                assertThat(history.longitude, IsEqual(it.longitude))
                assertThat(history.distance, IsEqual(it.distance))
                assertThat(history.createdAt, IsEqual(it.createdAt))
                assertThat(history.place, IsNot(IsNull()))

                it.artistsAroundHistories!!.mapIndexed { index, artistHistoryItem ->
                    assertThat(
                        history.historyItems!![index].spotifyItemId,
                        IsEqual(artistHistoryItem.spotifyArtistId)
                    )
                    assertThat(history.historyItems!![index].rank, IsEqual(artistHistoryItem.rank))
                    assertThat(
                        history.historyItems!![index].popularity,
                        IsEqual(artistHistoryItem.popularity)
                    )
                }
            }
        }
        return@runBlocking
    }

    @Test
    fun getSearchHistories_例外発生_エラーメッセージが表示されること() = runBlocking {

        // 例外発生するように設定
        val exception = Exception()
        every { authRepository.getSavedJigokumimiUserId() } throws exception

        // メソッド呼び出し
        viewModel.getSearchHistories(Constants.SearchType.TRACK)

        // エラーメッセージが表示されることを確認
        val extectedMessage =
            InstrumentationRegistry.getInstrumentation().context.resources.getString(
                R.string.general_error_message, exception.javaClass
            )
        assertThat(viewModel.errorMessage.getOrAwaitValue(), IsEqual(extectedMessage))
        assertThat(viewModel.isErrorDialogShown.getOrAwaitValue(), IsEqual(true))
    }

    @Test
    fun deleteHistory_Track_Track検索履歴が削除されること() = runBlocking {
        // 削除データ位置を設定
        val deleteIndex = 1

        // 検索履歴情報を設定
        viewModel.getSearchHistories(Constants.SearchType.TRACK)

        // メソッド呼び出し
        viewModel.deleteHistory(Constants.SearchType.TRACK, deleteIndex)
        // 削除対象データを取得
        val history = viewModel.trackHistoryList.getOrAwaitValue()[deleteIndex]

        // musicRepositoryの削除メソッドが呼ばれることを確認
        verify { runBlocking { musicRepository.deleteTracksAroundSearchHistories(history.id) } }

        // SnackBarが表示されることを確認
        val expectedMessage =
            InstrumentationRegistry.getInstrumentation().context.resources.getString(
                R.string.remove_history_message, history.createdAt
            )
        verify { viewModel.showSnackbar(expectedMessage) }

        // 削除位置が更新されていることを確認
        assertThat(viewModel.deleteDataIndex.getOrAwaitValue(), IsEqual(deleteIndex))

        // 検索履歴が更新されることを確認
        verify { viewModel.getSearchHistories(Constants.SearchType.TRACK) }
    }

    @Test
    fun deleteHistory_Artist_Artist検索履歴が削除されること() = runBlocking {
        // 削除データ位置を設定
        val deleteIndex = 1

        // 検索履歴情報を設定
        viewModel.getSearchHistories(Constants.SearchType.ARTIST)

        // メソッド呼び出し
        viewModel.deleteHistory(Constants.SearchType.ARTIST, deleteIndex)
        // 削除対象データを取得
        val history = viewModel.artistHistoryList.getOrAwaitValue()[deleteIndex]

        // musicRepositoryの削除メソッドが呼ばれることを確認
        verify { runBlocking { musicRepository.deleteArtistsAroundSearchHistories(history.id) } }

        // SnackBarが表示されることを確認
        val expectedMessage =
            InstrumentationRegistry.getInstrumentation().context.resources.getString(
                R.string.remove_history_message, history.createdAt
            )
        verify { viewModel.showSnackbar(expectedMessage) }

        // 削除位置が更新されていることを確認
        assertThat(viewModel.deleteDataIndex.getOrAwaitValue(), IsEqual(deleteIndex))

        // 検索履歴が更新されることを確認
        verify { viewModel.getSearchHistories(Constants.SearchType.ARTIST) }
    }

    @Test
    fun deleteHistory_例外発生_Track検索履歴が削除されること() = runBlocking {
        // 削除データ位置を設定
        val deleteIndex = 1

        // 検索履歴情報を設定
        viewModel.getSearchHistories(Constants.SearchType.TRACK)

        // 例外が発生するように設定
        val exception = Exception()
        every { runBlocking { musicRepository.deleteTracksAroundSearchHistories(any()) } } throws exception

        // メソッド呼び出し
        viewModel.deleteHistory(Constants.SearchType.TRACK, deleteIndex)

        // エラーメッセージが表示されることを確認
        val extectedMessage =
            InstrumentationRegistry.getInstrumentation().context.resources.getString(
                R.string.general_error_message, exception.javaClass
            )
        assertThat(viewModel.errorMessage.getOrAwaitValue(), IsEqual(extectedMessage))
        assertThat(viewModel.isErrorDialogShown.getOrAwaitValue(), IsEqual(true))
    }

    @Test
    fun searchHistoryDetails_Track_Track履歴詳細情報が更新されること() = runBlocking {

        // 検索履歴情報を設定
        viewModel.getSearchHistories(Constants.SearchType.TRACK)

        // データ取得位置を設定
        val searchIndex = 1

        // 対象データを取得
        val history = viewModel.trackHistoryList.getOrAwaitValue()[searchIndex]

        // メソッド呼び出し
        viewModel.searchHistoryDetails(Constants.SearchType.TRACK, searchIndex)

        // 検索履歴の更新処理が呼ばれることを確認
        verify { runBlocking { musicRepository.refreshTracksFromHistory(history) } }
        // 検索フラグがTrueになることを確認
        assertThat(viewModel.isSearchFinished.getOrAwaitValue(), IsEqual(true))

    }

    @Test
    fun searchHistoryDetails_Artist_Artist履歴詳細情報が更新されること() = runBlocking {

        // 検索履歴情報を設定
        viewModel.getSearchHistories(Constants.SearchType.ARTIST)

        // データ取得位置を設定
        val searchIndex = 1

        // 対象データを取得
        val history = viewModel.artistHistoryList.getOrAwaitValue()[searchIndex]

        // メソッド呼び出し
        viewModel.searchHistoryDetails(Constants.SearchType.ARTIST, searchIndex)

        // 検索履歴の更新処理が呼ばれることを確認
        verify { runBlocking { musicRepository.refreshArtistsFromHistory(history) } }
        // 検索フラグがTrueになることを確認
        assertThat(viewModel.isSearchFinished.getOrAwaitValue(), IsEqual(true))
    }

    @Test
    fun searchHistoryDetails_例外発生_エラーメッセージが表示されること() = runBlocking {

        // 検索履歴情報を設定
        viewModel.getSearchHistories(Constants.SearchType.ARTIST)

        // 例外が発生するように設定
        val exception = Exception()

        // 検索履歴の更新処理が呼ばれることを確認
        every { runBlocking { musicRepository.refreshArtistsFromHistory(any()) } } throws exception

        // データ取得位置を設定
        val searchIndex = 1

        // メソッド呼び出し
        viewModel.searchHistoryDetails(Constants.SearchType.ARTIST, searchIndex)

        // エラーメッセージが表示されることを確認
        val extectedMessage =
            InstrumentationRegistry.getInstrumentation().context.resources.getString(
                R.string.general_error_message, exception.javaClass
            )
        assertThat(viewModel.errorMessage.getOrAwaitValue(), IsEqual(extectedMessage))
        assertThat(viewModel.isErrorDialogShown.getOrAwaitValue(), IsEqual(true))
    }

    @Test
    fun doneSearchMusic_検索完了フラグがFalseになること() = runBlocking {

        // メソッド呼び出し
        viewModel.doneSearchMusic()

        // falseになることを確認
        assertThat(viewModel.isSearchFinished.getOrAwaitValue(), IsEqual(false))
    }
}
