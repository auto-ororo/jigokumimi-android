package com.ororo.auto.jigokumimi.viewmodels

import android.media.MediaPlayer
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.javafaker.Faker
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.domain.Artist
import com.ororo.auto.jigokumimi.domain.Track
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.repository.faker.FakeMusicRepository
import com.ororo.auto.jigokumimi.util.CreateTestDataUtil
import getOrAwaitValue
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.hamcrest.core.IsEqual
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.isAccessible


@RunWith(AndroidJUnit4::class)
class ResultViewModelTest {


    // LiveDataのテストに必要なルールを設定
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    lateinit var viewModel: ResultViewModel

    lateinit var authRepository: IAuthRepository
    lateinit var musicRepository: FakeMusicRepository

    val faker = Faker(Locale("jp_JP"))

    val testDataUtil = CreateTestDataUtil()

    lateinit var mockMp: MediaPlayer

    @Before
    fun createViewModel() {
        val tracks = mutableListOf<Track>()

        for (i in 1..3) {
            tracks.add(testDataUtil.createDummyTrack())
        }

        val artists = mutableListOf<Artist>()

        for (i in 1..3) {
            artists.add(testDataUtil.createDummyArtist())
        }

        musicRepository = spyk(FakeMusicRepository(_tracks = tracks, _artists = artists))
        authRepository = mockk(relaxed = true)

        viewModel = spyk(
            ResultViewModel(
                ApplicationProvider.getApplicationContext(),
                authRepository,
                musicRepository
            )
        )

        mockMp = mockk(relaxed = true)
        viewModel.mp = mockMp
    }

    @Test
    fun changeTrackFavoriteState_お気に入り曲が登録できること() {

        // 対象データのインデックスを設定
        val targetIndex = 0

        // 対象データのお気に入り状態をfalseに設定
        viewModel.tracklist.getOrAwaitValue()[targetIndex].isSaved = false

        // メソッド呼び出し
        viewModel.changeTrackFavoriteState(targetIndex)

        // Repositoryのメソッドが呼ばれることを確認
        verify { runBlocking { musicRepository.changeTrackFavoriteState(targetIndex, true) } }

        // Snackbarのメッセージが表示されることを確認
        val expectedMessage =
            InstrumentationRegistry.getInstrumentation().context.resources.getString(
                R.string.save_track_message, viewModel.tracklist.getOrAwaitValue()[targetIndex].name
            )
        verify { viewModel.showSnackbar(expectedMessage) }

        // 変更データのインデックスが対象データのインデックスに設定されていることを確認
        assertThat(viewModel.changeDataIndex.getOrAwaitValue(), IsEqual(targetIndex))
    }


    @Test
    fun changeTrackFavoriteState_お気に入り曲が登録解除できること() {

        // 対象データのインデックスを設定
        val targetIndex = 0

        // 対象データのお気に入り状態をtrueに設定
        viewModel.tracklist.getOrAwaitValue()[targetIndex].isSaved = true

        // メソッド呼び出し
        viewModel.changeTrackFavoriteState(targetIndex)

        // Repositoryのメソッドが呼ばれることを確認
        verify { runBlocking { musicRepository.changeTrackFavoriteState(targetIndex, false) } }

        // Snackbarのメッセージが表示されることを確認
        val expectedMessage =
            InstrumentationRegistry.getInstrumentation().context.resources.getString(
                R.string.remove_track_message,
                viewModel.tracklist.getOrAwaitValue()[targetIndex].name
            )
        verify { viewModel.showSnackbar(expectedMessage) }

        // 変更データのインデックスが対象データのインデックスに設定されていることを確認
        assertThat(viewModel.changeDataIndex.getOrAwaitValue(), IsEqual(targetIndex))
    }


    @Test
    fun changeTrackFavoriteState_例外発生_エラーメッセージが表示されること() {

        // 対象データのインデックスを設定
        val targetIndex = 0

        //例外が発生するように設定
        val exception = Exception()
        every {
            runBlocking {
                musicRepository.changeTrackFavoriteState(
                    any(),
                    any()
                )
            }
        } throws exception

        // メソッド呼び出し
        viewModel.changeTrackFavoriteState(targetIndex)

        // エラーメッセージが設定されることを確認
        val extectedMessage =
            InstrumentationRegistry.getInstrumentation().context.resources.getString(
                R.string.general_error_message, exception.javaClass
            )
        assertThat(viewModel.errorMessage.getOrAwaitValue(), IsEqual(extectedMessage))
        assertThat(viewModel.isErrorDialogShown.getOrAwaitValue(), IsEqual(true))
    }


    @Test
    fun changeArtistFollowState_フォローできること() {

        // 対象データのインデックスを設定
        val targetIndex = 0

        // 対象データのフォロー状態をfalseに設定
        viewModel.artistlist.getOrAwaitValue()[targetIndex].isFollowed = false

        // メソッド呼び出し
        viewModel.changeArtistFollowState(targetIndex)

        // Repositoryのメソッドが呼ばれることを確認
        verify { runBlocking { musicRepository.changeArtistFollowState(targetIndex, true) } }

        // Snackbarのメッセージが表示されることを確認
        val expectedMessage =
            InstrumentationRegistry.getInstrumentation().context.resources.getString(
                R.string.follow_artist_message,
                viewModel.artistlist.getOrAwaitValue()[targetIndex].name
            )
        verify { viewModel.showSnackbar(expectedMessage) }

        // 変更データのインデックスが対象データのインデックスに設定されていることを確認
        assertThat(viewModel.changeDataIndex.getOrAwaitValue(), IsEqual(targetIndex))
    }

    @Test
    fun changeArtistFollowState_フォロー解除できること() {

        // 対象データのインデックスを設定
        val targetIndex = 0

        // 対象データのフォロー状態をfalseに設定
        viewModel.artistlist.getOrAwaitValue()[targetIndex].isFollowed = true

        // メソッド呼び出し
        viewModel.changeArtistFollowState(targetIndex)

        // Repositoryのメソッドが呼ばれることを確認
        verify { runBlocking { musicRepository.changeArtistFollowState(targetIndex, false) } }

        // Snackbarのメッセージが表示されることを確認
        val expectedMessage =
            InstrumentationRegistry.getInstrumentation().context.resources.getString(
                R.string.un_follow_artist_message,
                viewModel.artistlist.getOrAwaitValue()[targetIndex].name
            )
        verify { viewModel.showSnackbar(expectedMessage) }

        // 変更データのインデックスが対象データのインデックスに設定されていることを確認
        assertThat(viewModel.changeDataIndex.getOrAwaitValue(), IsEqual(targetIndex))
    }

    @Test
    fun changeArtistFollowState_例外発生_エラーメッセージが表示されること() {

        // 対象データのインデックスを設定
        val targetIndex = 0

        //例外が発生するように設定
        val exception = Exception()
        every {
            runBlocking {
                musicRepository.changeArtistFollowState(
                    any(),
                    any()
                )
            }
        } throws exception

        // メソッド呼び出し
        viewModel.changeArtistFollowState(targetIndex)

        // エラーメッセージが設定されることを確認
        val extectedMessage =
            InstrumentationRegistry.getInstrumentation().context.resources.getString(
                R.string.general_error_message, exception.javaClass
            )
        assertThat(viewModel.errorMessage.getOrAwaitValue(), IsEqual(extectedMessage))
        assertThat(viewModel.isErrorDialogShown.getOrAwaitValue(), IsEqual(true))
    }

    @Test
    fun stopTrack_MediaPlayerが再生状態_Pauseが呼ばれ再生フラグがオフになること() {

        every {
            mockMp.isPlaying
        } returns true

        // メソッド呼び出し
        viewModel.stopTrack()

        verify { mockMp.pause() }
        assertThat(viewModel.isPlaying.value, IsEqual(false))

    }

    @Test
    fun stopTrack_MediaPlayerが停止状態_Pauseが呼ばれず再生フラグがオフになること() {

        every {
            mockMp.isPlaying
        } returns false

        // メソッド呼び出し
        viewModel.stopTrack()

        verify(inverse = true) { mockMp.pause() }
        assertThat(viewModel.isPlaying.value, IsEqual(false))

    }

    @Test
    fun resumeTrack_MediaPlayerが停止状態_Startが呼ばれること() {

        every {
            mockMp.isPlaying
        } returns false

        // メソッド呼び出し
        viewModel.resumeTrack()

        verify { mockMp.start() }
    }

    @Test
    fun resumeTrack_MediaPlayerが再生状態_Startが呼ばれないこと() {

        // 再生状態のMediaPlayerモックを作成
        every {
            mockMp.isPlaying
        } returns true

        // メソッド呼び出し
        viewModel.resumeTrack()

        verify(inverse = true) { mockMp.start() }
    }

    @Test
    fun movePlayingTrack_移動後の位置が0未満_最後のTrackList要素が再生曲に設定されること() {

        // 初期再生曲をTrackListの最初の要素に指定
        viewModel.setPlayingTrack(0)


        // privateメソッドを取得
        val method = viewModel::class.memberFunctions.find { it.name == "movePlayingTrack" }
        method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel, -1)
        }

        assertThat(viewModel.playingTrackIndex.value, IsEqual(viewModel.tracklist.value?.lastIndex))

    }

    @Test
    fun movePlayingTrack_移動後の位置がTrackList要素数を超える_最初のTrackList要素が再生曲に設定されること() {

        // 初期再生曲をTrackListの最初の要素に指定
        viewModel.setPlayingTrack(viewModel.tracklist.value!!.lastIndex)


        // privateメソッドを取得
        val method = viewModel::class.memberFunctions.find { it.name == "movePlayingTrack" }
        method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel, 1)
        }

        assertThat(viewModel.playingTrackIndex.value, IsEqual(0))

    }

    @Test
    fun movePlayingTrack_移動後の位置が0以上TrackList要素数以下_指定分移動した位置のTrackList要素が再生曲に設定されること() {

        // 初期再生曲をTrackListの最初の要素に指定
        viewModel.setPlayingTrack(0)


        // privateメソッドを取得
        val method = viewModel::class.memberFunctions.find { it.name == "movePlayingTrack" }
        method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel, 1)
        }

        assertThat(viewModel.playingTrackIndex.value, IsEqual(1))

    }

    @Test
    fun createTimeLabel_10000ミリ秒以上_時間表示文字列に変換されること() {

        val ms = 20000

        val ret = viewModel.createTimeLabel(ms)

        assertThat(ret, IsEqual("0:20"))
    }

    @Test
    fun createTimeLabel_10000ミリ秒未満_秒表示部分が2桁になるように0埋めされること() {

        val ms = 9000

        val ret = viewModel.createTimeLabel(ms)

        assertThat(ret, IsEqual("0:09"))
    }

}