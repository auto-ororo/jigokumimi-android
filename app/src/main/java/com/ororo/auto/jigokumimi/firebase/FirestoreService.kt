package com.ororo.auto.jigokumimi.firebase

import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.ororo.auto.jigokumimi.util.Constants.Companion.MUSIC_LIST_SIZE
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.extension.getAtLocation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirestoreService(private val firestore: FirebaseFirestore) {

    companion object {
        private const val USER = "user"
        private const val HISTORY = "history"
    }

    suspend fun postMusicAround(request: PostMusicAroundRequest) {
        val ref = firestore.collection(request.type.pathName).document(request.userId)

        val musicAround = request.convertToMusicAround()

        ref.set(musicAround).await()
    }

    suspend fun getMusicAroundItems(
        itemsRequest: GetMusicAroundItemsRequest
    ): List<MusicAroundItem> =
        suspendCancellableCoroutine { continuation ->

            GeoFirestore(firestore.collection(itemsRequest.type.pathName)).getAtLocation(
                GeoPoint(itemsRequest.location.latitude, itemsRequest.location.longitude),
                itemsRequest.distance.toDouble()
            ) { docs, ex ->
                ex?.let { continuation.resumeWithException(ex) }

                docs?.mapNotNull { it.toObject<MusicAround>() }
                    ?.filter { musicAround -> musicAround.userId != itemsRequest.userId }
                    ?.map { it.musicAroundItems }?.flatten()
                    ?.groupingBy { it.musicItemId }
                    ?.reduce { _, acc, ele ->
                        MusicAroundItem(
                            ele.musicItemId,
                            ele.popularity + acc.popularity
                        )
                    }?.values?.toList()?.sortedByDescending { it.popularity }?.take(MUSIC_LIST_SIZE)
                    .let { continuation.resume(it ?: emptyList()) }
            }
        }

    suspend fun postSearchHistory(
        request: PostSearchHistoryRequest
    ) {
        val ref = firestore.collection("$HISTORY/${request.userId}/${request.type.pathName}")

        val doc = ref.document()
        val searchHistory = request.convertToSearchHistory(doc.id)

        firestore.runTransaction { transaction ->
            transaction.set(doc, searchHistory)
        }.await()
    }

    suspend fun getSearchHistory(request: GetSearchHistoryRequest) =
        firestore.collection("$HISTORY/${request.userId}/${request.type.pathName}")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(MUSIC_LIST_SIZE.toLong())
            .get().await().toObjects<SearchHistory>()


    suspend fun deleteSearchHistory(request: DeleteSearchHistoryRequest): Void =
        firestore.collection("$HISTORY/${request.userId}/${request.type.pathName}")
            .document(request.searchHistoryId).delete().await()

    suspend fun createUser(request: UserRequest): String {
        val ref = firestore.collection(USER)

        val doc = ref.document()

        val user = User(doc.id, spotify = User.Spotify(request.spotifyUserId))

        firestore.runTransaction { transaction ->
            transaction.set(doc, user)
        }.await()

        return doc.id
    }

    suspend fun existsUser(request: UserRequest): Boolean =
        firestore.collection(USER)
            .whereEqualTo(FieldPath.of("spotify", "id"), request.spotifyUserId)
            .get().await().isEmpty.not()

}

