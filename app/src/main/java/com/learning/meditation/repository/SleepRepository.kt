package com.learning.meditation.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.learning.meditation.dataclass.SleepTrack
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

class SleepRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private var cachedTracks: List<SleepTrack>? = null

    suspend fun getSleepTracks(): List<SleepTrack> = coroutineScope {
        if (cachedTracks != null) {
            return@coroutineScope cachedTracks!!
        }
        try {
            val snapshot = firestore.collection("sleepTracks").get().await()
            val trackList = snapshot.documents.map { document ->
                async {
                    document.toObject(SleepTrack::class.java)
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