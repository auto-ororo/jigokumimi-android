package com.ororo.auto.jigokumimi.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class FakeMusicDao(
    val tracksDisplayed: MutableList<DisplayedTrack> = mutableListOf(),
    val artistsDisplayed: MutableList<DisplayedArtist> = mutableListOf()
) : MusicDao {

    override fun getTracks(): LiveData<List<DisplayedTrack>> {
        return MutableLiveData(tracksDisplayed)
    }

    override fun insertTrack(displayedTrackLogs: List<DisplayedTrack>) {
        tracksDisplayed.addAll(displayedTrackLogs)
    }

    override fun getArtists(): LiveData<List<DisplayedArtist>> {
        return MutableLiveData(artistsDisplayed)
    }

    override fun insertArtist(displayedArtist: List<DisplayedArtist>) {
        artistsDisplayed.addAll(displayedArtist)
    }
}