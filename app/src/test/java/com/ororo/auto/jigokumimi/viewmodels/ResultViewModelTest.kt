package com.ororo.auto.jigokumimi.viewmodels

import android.media.MediaPlayer
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.javafaker.Faker
import com.ororo.auto.jigokumimi.domain.Track
import com.ororo.auto.jigokumimi.repository.faker.FakeMusicRepository
import com.ororo.auto.jigokumimi.util.CreateTestDataUtil
import org.hamcrest.core.IsEqual
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import java.util.*
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.isAccessible


@RunWith(AndroidJUnit4::class)
class ResultViewModelTest {


    // LiveDataのテストに必要なルールを設定
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    lateinit var viewModel: ResultViewModel

    val faker = Faker(Locale("jp_JP"))

    val testDataUtil = CreateTestDataUtil()

    @Before
    fun createViewModel() {
        val tracks = mutableListOf<Track>()

        for (i in 1..3) {
            tracks.add(testDataUtil.createDummyTrack())
        }

        viewModel = ResultViewModel(
            ApplicationProvider.getApplicationContext(),
            FakeMusicRepository(_tracks = tracks)
        )
    }

    @Test
    fun stopTrack_MediaPlayerが再生状態_Pauseが呼ばれ再生フラグがオフになること() {

        // 再生状態のMediaPlayerモックを作成
        val mockMp = mock(MediaPlayer::class.java)
        `when`(mockMp.isPlaying).thenReturn(true)
        viewModel.mp = mockMp

        // メソッド呼び出し
        viewModel.stopTrack()

        verify(mockMp, times(1)).pause()
        assertThat(viewModel.isPlaying.value, IsEqual(false))

    }

    @Test
    fun stopTrack_MediaPlayerが停止状態_Pauseが呼ばれず再生フラグがオフになること() {

        // 再生状態のMediaPlayerモックを作成
        val mockMp = mock(MediaPlayer::class.java)
        `when`(mockMp.isPlaying).thenReturn(false)
        viewModel.mp = mockMp

        // メソッド呼び出し
        viewModel.stopTrack()

        verify(mockMp, times(0)).pause()
        assertThat(viewModel.isPlaying.value, IsEqual(false))

    }

    @Test
    fun resumeTrack_MediaPlayerが停止状態_Startが呼ばれること() {

        // 停止状態のMediaPlayerモックを作成
        val mockMp = mock(MediaPlayer::class.java)
        `when`(mockMp.isPlaying).thenReturn(false)
        viewModel.mp = mockMp

        // メソッド呼び出し
        viewModel.resumeTrack()

        verify(mockMp, times(1)).start()
    }

    @Test
    fun resumeTrack_MediaPlayerが再生状態_Startが呼ばれないこと() {

        // 再生状態のMediaPlayerモックを作成
        val mockMp = mock(MediaPlayer::class.java)
        `when`(mockMp.isPlaying).thenReturn(true)
        viewModel.mp = mockMp

        // メソッド呼び出し
        viewModel.resumeTrack()

        verify(mockMp, times(0)).start()
    }

    @Test
    fun movePlayingTrack_移動後の位置が0未満_最後のTrackList要素が再生曲に設定されること() {

        // 初期再生曲をTrackListの最初の要素に指定
        viewModel.setPlayingTrack(viewModel.tracklist.value!!.get(0))


        // privateメソッドを取得
        val method = viewModel::class.memberFunctions.find { it.name == "movePlayingTrack" }
        method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel, -1)
        }

        assertThat(viewModel.playingTrack.value, IsEqual(viewModel.tracklist.value?.last()))

    }

    @Test
    fun movePlayingTrack_移動後の位置がTrackList要素数を超える_最初のTrackList要素が再生曲に設定されること() {

        // 初期再生曲をTrackListの最初の要素に指定
        viewModel.setPlayingTrack(viewModel.tracklist.value!!.last())


        // privateメソッドを取得
        val method = viewModel::class.memberFunctions.find { it.name == "movePlayingTrack" }
        method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel, 1)
        }

        assertThat(viewModel.playingTrack.value, IsEqual(viewModel.tracklist.value?.get(0)))

    }

    @Test
    fun movePlayingTrack_移動後の位置が0以上TrackList要素数以下_指定分移動した位置のTrackList要素が再生曲に設定されること() {

        // 初期再生曲をTrackListの最初の要素に指定
        viewModel.setPlayingTrack(viewModel.tracklist.value!!.get(0))


        // privateメソッドを取得
        val method = viewModel::class.memberFunctions.find { it.name == "movePlayingTrack" }
        method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel, 1)
        }

        assertThat(viewModel.playingTrack.value, IsEqual(viewModel.tracklist.value?.get(1)))

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