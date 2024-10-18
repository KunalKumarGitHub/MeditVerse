package com.learning.meditation.repository

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.learning.meditation.dataclass.MeditationTrack
import com.learning.meditation.dataclass.Playlist
import com.learning.meditation.dataclass.SleepTrack
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

class PlaylistRepository{
    private val firestore=FirebaseFirestore.getInstance()
    private var cachedPlaylists: List<Playlist>? = null

    suspend fun fetchPlaylists(userId: String): List<Playlist> = coroutineScope {
        if (cachedPlaylists != null) {
            return@coroutineScope cachedPlaylists!!
        }

        try {
            val result = firestore.collection("users")
                .document(userId)
                .collection("myPlaylists")
                .get()
                .await()

            cachedPlaylists= result.documents.mapNotNull { document ->
                document.toObject(Playlist::class.java)
            }
            return@coroutineScope cachedPlaylists!!.sortedBy { playlist ->playlist.playlistName  }
        } catch (e: Exception) {
            Log.e("FirestoreError", "Error fetching playlists", e)
            return@coroutineScope emptyList()
        }
    }

    suspend fun fetchPlaylistTracks(userId: String,playlistId: String): List<MeditationTrack> {
        val firestore = FirebaseFirestore.getInstance()

        return try {
            val result = firestore.collection("users")
                .document(userId)
                .collection("myPlaylists")
                .document(playlistId)
                .collection("tracks")
                .get()
                .await()

            result.documents.mapNotNull { document ->
                document.toObject(MeditationTrack::class.java)
            }.sortedBy { tracks-> tracks.title }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addMeditationTrackToPlaylist(userId: String, playlist: Playlist, track: MeditationTrack, context: Context, onComplete: () -> Unit, onFailure: (Exception)->Unit) {
        val firestore = FirebaseFirestore.getInstance()

        val trackRef = firestore.collection("users").document(userId)
            .collection("myPlaylists").document(playlist.playlistId)
            .collection("tracks")

        trackRef.whereEqualTo("trackId", track.trackId).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    trackRef.add(track)
                        .addOnSuccessListener {
                            incrementTrackCount(userId, playlist, context)
                            cachedPlaylists=null
                            onComplete()
                        }
                        .addOnFailureListener { exception ->
                            onFailure(exception)
                        }
                } else {
                    Toast.makeText(context, "Track already exists in the playlist", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Failed to check track existence: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun addSleepTrackToPlaylist(userId: String, playlist: Playlist, track: SleepTrack, context: Context, onComplete: () -> Unit, onFailure: (Exception)->Unit) {
        val firestore = FirebaseFirestore.getInstance()

        val trackRef = firestore.collection("users").document(userId)
            .collection("myPlaylists").document(playlist.playlistId)
            .collection("tracks")

        trackRef.whereEqualTo("trackId", track.trackId).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    trackRef.add(track)
                        .addOnSuccessListener {
                            incrementTrackCount(userId, playlist, context)
                            cachedPlaylists=null
                            Toast.makeText(context, "Track added successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            onFailure(exception)
                        }
                } else {
                    Toast.makeText(context, "Track already exists in the playlist", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Failed to check track existence: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun incrementTrackCount(userId: String, playlist: Playlist, context: Context) {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("users").document(userId)
            .collection("myPlaylists").document(playlist.playlistId)
            .update("trackCount", playlist.trackCount+1)
            .addOnSuccessListener {
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Failed to update track count: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }


    fun addPlaylistToFirestore(userId: String, playlist: Playlist, onComplete: () -> Unit, onFailure: (Exception)->Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users")
            .document(userId)
            .collection("myPlaylists")
            .document(playlist.playlistId)
            .set(playlist)
            .addOnSuccessListener {
                cachedPlaylists=null
                onComplete()
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    fun deletePlaylistFromFirestore(
        userId: String,
        playlist: Playlist,
        onComplete: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val firestore = FirebaseFirestore.getInstance()
        val playlistDocRef = firestore.collection("users")
            .document(userId)
            .collection("myPlaylists")
            .document(playlist.playlistId)

        playlistDocRef.collection("tracks").get()
            .addOnSuccessListener { querySnapshot ->
                val batch = firestore.batch()

                for (document in querySnapshot.documents) {
                    batch.delete(document.reference)
                }

                batch.commit()
                    .addOnSuccessListener {
                        playlistDocRef.delete()
                            .addOnSuccessListener {
                                cachedPlaylists = null
                                onComplete()
                            }
                            .addOnFailureListener { exception ->
                                onFailure(exception)
                            }
                    }
                    .addOnFailureListener { exception ->
                        onFailure(exception)
                    }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}
