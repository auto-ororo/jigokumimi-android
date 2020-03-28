package com.ororo.auto.jigokumimi.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class FakeMusicDao(
    val tracksAround: MutableList<TrackAround> = mutableListOf(),
    val artistsAround: MutableList<ArtistAround> = mutableListOf()
) : MusicDao {

    override fun getTracks(): LiveData<List<TrackAround>> {
        return MutableLiveData(tracksAround)
    }

    override fun insertTrack(tracks: List<TrackAround>) {
        tracksAround.addAll(tracks)
    }

    override fun getArtists(): LiveData<List<ArtistAround>> {
        return MutableLiveData(artistsAround)
    }

    override fun insertArtist(artists: List<ArtistAround>) {
        artistsAround.addAll(artists)
    }
}