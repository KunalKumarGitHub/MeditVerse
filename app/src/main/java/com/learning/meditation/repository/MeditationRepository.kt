package com.learning.meditation.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.learning.meditation.dataclass.MeditationTrack
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class MeditationRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private var cachedTracks: List<MeditationTrack>? = null

    suspend fun getMeditationTracks(): List<MeditationTrack> = coroutineScope {
        if (cachedTracks != null) {
            return@coroutineScope cachedTracks!!
        }

        try {
            val snapshot = firestore.collection("meditationTracks").get().await()
            val trackList = snapshot.documents.map { document ->
                async {
                    document.toObject(MeditationTrack::class.java)
                }
            }.awaitAll().filterNotNull()

            cachedTracks = trackList.shuffled()

            return@coroutineScope cachedTracks!!
        } catch (e: Exception) {
            Log.e("FirestoreError", "Error fetching tracks", e)
            return@coroutineScope emptyList()
        }
    }

}
