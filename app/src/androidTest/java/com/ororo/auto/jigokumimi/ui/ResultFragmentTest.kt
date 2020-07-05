package com.ororo.auto.jigokumimi.ui

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.github.javafaker.Faker
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.domain.Artist
import com.ororo.auto.jigokumimi.domain.Track
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.repository.IMusicRepository
import com.ororo.auto.jigokumimi.util.Constants
import com.ororo.auto.jigokumimi.util.CreateAndroidTestDataUtil
import com.ororo.auto.jigokumimi.viewmodels.ResultViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.android.synthetic.main.result_artist_item.view.*
import kotlinx.android.synthetic.main.result_track_item.view.*
import kotlinx.android.synthetic.main.result_track_item.view.name
import kotlinx.android.synthetic.main.result_track_item.view.rank
import kotlinx.coroutines.runBlocking
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidApplication
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.Serializable
import java.util.*


@MediumTest
@RunWith(AndroidJUnit4::class)
class ResultFragmentTest {

    private val authRepository: IAuthRepository = mockk(relaxed = true)
    private val musicRepository: IMusicRepository = mockk(relaxed = true)

    val faker = Faker(Locale("jp_JP"))

    private val testDataUtil = CreateAndroidTestDataUtil()

    private val modules: List<Module> = listOf(
        module {
            factory { musicRepository }
        },
        module {
            factory { authRepository }
        },
        module {
            factory { ResultViewModel(androidApplication(), get(), get()) }
        }
    )

    @Before
    fun init() {
        loadKoinModules(modules)

        // Tracksデータを作成・設定
        val dummyTracks = mutableListOf<Track>()
        for (i in 1..10) {
            // 再生可能な音源を設定
            dummyTracks.add(
                testDataUtil.createDummyTrack(
                    previewUrl = "http://www.ne.jp/asahi/music/myuu/wave/menuettm.mp3",
                    isSaved = false
                )
            )
        }
        every { runBlocking { musicRepository.tracks } } returns MutableLiveData(dummyTracks)

        // Artistsダミーデータを作成・設定
        val dummyArtists = mutableListOf<Artist>()
        for (i in 1..10) {
            dummyArtists.add(testDataUtil.createDummyArtist())
        }
        every { runBlocking { musicRepository.artists } } returns MutableLiveData(dummyArtists)
    }

    @After
    fun cleanUp() {
        unloadKoinModules(modules)
    }

    @Test
    fun Track検索後_RecyclerView上にTrackが表示されること() {

        // TrackをBundleに設定して結果画面を起動し、NavControllerを設定
        val bundle = Bundle()
        bundle.putSerializable(
            "searchType",
            Serializable::class.java.cast(Constants.SearchType.TRACK)
        )
        bundle.putInt(
            "distance",
            500
        )
        bundle.putString(
            "searchDateTime",
            "9999-99-99 99:99:99"
        )
        launchFragmentInContainer<ResultFragment>(bundle, R.style.AppTheme)

        onView(withId(R.id.recyclerView))
            .check(matches(hasTrack(0, musicRepository.tracks.value!!.get(0))))
    }

    @Test
    fun Artist検索後_RecyclerView上にArtistが表示されること() {

        // ArtistをBundleに設定して結果画面を起動し、NavControllerを設定
        val bundle = Bundle()
        bundle.putSerializable(
            "searchType",
            Serializable::class.java.cast(Constants.SearchType.ARTIST)
        )
        bundle.putInt(
            "distance",
            500
        )
        bundle.putString(
            "searchDateTime",
            "9999-99-99 99:99:99"
        )
        launchFragmentInContainer<ResultFragment>(bundle, R.style.AppTheme)

        onView(withId(R.id.recyclerView))
            .check(matches(hasArtist(0, musicRepository.artists.value!!.get(0))))
    }

    @Test
    fun 再生プレーヤー動作検証() {

        // TrackをBundleに設定して結果画面を起動し、NavControllerを設定
        val bundle = Bundle()
        bundle.putSerializable(
            "searchType",
            Serializable::class.java.cast(Constants.SearchType.TRACK)
        )
        bundle.putInt(
            "distance",
            500
        )
        bundle.putString(
            "searchDateTime",
            "9999-99-99 99:99:99"
        )
        launchFragmentInContainer<ResultFragment>(bundle, R.style.AppTheme)

        // キューボタンをタップ
        onView(withId(R.id.recyclerView)).perform(
            RecyclerViewActions.actionOnItemAtPosition<HistoryListViewHolder>(
                0,
                ChildViewAction.clickChildViewWithId(R.id.queueButton)
            )
        )

        Thread.sleep(1000)

        // 再生プレーヤーが表示されることを確認
        onView(withId(R.id.rankTrackNameText)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.artistAlbumText)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.playStopButton)).check(matches(isCompletelyDisplayed()))


        // 選択したTrack情報が表示されることを確認
        var targetTrack = musicRepository.tracks.value!!.get(0)
        onView(withId(R.id.rankTrackNameText)).check(matches(withText("${targetTrack.rank} . ${targetTrack.name}")))
        onView(withId(R.id.artistAlbumText)).check(matches(withText("${targetTrack.artists} - ${targetTrack.album}")))

        // キューボタンに停止アイコンが表示されることを確認
        onView(withId(R.id.playStopButton)).check(matches(withDrawable(R.drawable.ic_pause)))

        // キューボタンをタップ
        onView(withId(R.id.playStopButton)).perform(click())

        // キューボタンに再生アイコンが表示されることを確認
        onView(withId(R.id.playStopButton)).check(matches(withDrawable(R.drawable.ic_play_arrow)))

        // お気に入り登録ボタンをタップ
        onView(withId(R.id.miniPlayerSaveTrackButton)).perform(click())

        // お気に入り登録ボタンが登録状態になることを確認
        onView(withId(R.id.miniPlayerSaveTrackButton)).check(matches(withDrawable(R.drawable.ic_favorite)))

        // Snackbarが表示されることを確認
        val expectedSaveMessage =
            InstrumentationRegistry.getInstrumentation().targetContext.resources.getString(
                R.string.save_track_message, targetTrack.name
            )
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(expectedSaveMessage)))

        // お気に入り登録ボタンをタップ
        onView(withId(R.id.miniPlayerSaveTrackButton)).perform(click())

        // お気に入り登録ボタンが登録解除状態になることを確認
        onView(withId(R.id.miniPlayerSaveTrackButton)).check(matches(withDrawable(R.drawable.ic_favorite_border)))

        // Snackbarが表示されることを確認
        val expectedRemoveMessage =
            InstrumentationRegistry.getInstrumentation().targetContext.resources.getString(
                R.string.remove_track_message, targetTrack.name
            )
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(expectedRemoveMessage)))
    }


    /**
     * Track情報がRecyclerViewに表示されているかどうかを評価するCustomMatcher
     */
    private fun hasTrack(position: Int, track: Track) = object : TypeSafeMatcher<View>() {
        var innerName: String? = null
        var innerRank: String? = null
        var innerArtists: String? = null
        var innerAlbum: String? = null

        override fun describeTo(description: Description) {
            description.appendText(String.format(track.toString() + "is not equal to %s.", track))
        }

        override fun matchesSafely(view: View): Boolean {
            if (view !is RecyclerView) return false

            val holder =
                view.findViewHolderForAdapterPosition(position) as ResultTrackListViewHolder
            innerName = holder.itemView.name.text.toString()
            innerRank = holder.itemView.rank.text.toString()
            innerArtists = holder.itemView.artists.text.toString()
            innerAlbum = holder.itemView.album.text.toString()
            return innerName == track.name && innerRank == track.rank.toString() && innerArtists == track.artists && innerAlbum == track.album
        }
    }

    /**
     * Artist情報がRecyclerViewに表示されているかどうかを評価するCustomMatcher
     */
    private fun hasArtist(position: Int, artist: Artist) = object : TypeSafeMatcher<View>() {
        var innerName: String? = null
        var innerRank: String? = null
        var innerGenres: String? = null

        override fun describeTo(description: Description) {
            description.appendText(String.format(artist.toString() + "is not equal to %s.", artist))
        }

        override fun matchesSafely(view: View): Boolean {
            if (view !is RecyclerView) return false

            val holder =
                view.findViewHolderForAdapterPosition(position) as ResultArtistListViewHolder
            innerName = holder.itemView.name.text.toString()
            innerRank = holder.itemView.rank.text.toString()
            innerGenres = holder.itemView.genres.text.toString()
            return innerName == artist.name && innerRank == artist.rank.toString() && innerGenres == artist.genres
        }
    }

    fun withDrawable(resourceId: Int): Matcher<View?>? {
        return DrawableMatcher(resourceId)
    }

    fun noDrawable(): Matcher<View?>? {
        return DrawableMatcher(-1)
    }
}


/**
 * RecyclerViewの子Viewを操作するActionクラス
 */
object ChildViewAction {
    fun clickChildViewWithId(id: Int): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View>? {
                return null
            }

            override fun getDescription(): String {
                return "Click on a child view with specified id."
            }

            override fun perform(uiController: UiController?, view: View) {
                val v = view.findViewById<View>(id)
                v?.performClick()
            }
        }
    }
}

/**
 *  DrawableのリソースIDを評価するCustomMatcher
 */
class DrawableMatcher internal constructor(private val expectedId: Int) :
    TypeSafeMatcher<View>(View::class.java) {
    private var resourceName: String? = null
    override fun matchesSafely(target: View): Boolean {
        if (target !is ImageView) {
            return false
        }
        val imageView: ImageView = target as ImageView
        if (expectedId == EMPTY) {
            return imageView.getDrawable() == null
        }
        if (expectedId == ANY) {
            return imageView.getDrawable() != null
        }
        val resources: Resources = target.context.resources
        val expectedDrawable: Drawable =
            resources.getDrawable(expectedId)
        resourceName = resources.getResourceEntryName(expectedId)
        if (expectedDrawable == null) {
            return false
        }
        val bitmap = getBitmap(imageView.getDrawable())
        val otherBitmap = getBitmap(expectedDrawable)
        return bitmap.sameAs(otherBitmap)
    }

    private fun getBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
        drawable.draw(canvas)
        return bitmap
    }

    override fun describeTo(description: Description) {
        description.appendText("with drawable from resource id: ")
        description.appendValue(expectedId)
        if (resourceName != null) {
            description.appendText("[")
            description.appendText(resourceName)
            description.appendText("]")
        }
    }

    companion object {
        const val EMPTY = -1
        const val ANY = -2
    }

}
