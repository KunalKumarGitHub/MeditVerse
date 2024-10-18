package com.learning.meditation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learning.meditation.dataclass.MeditationTrack
import com.learning.meditation.dataclass.Playlist
import com.learning.meditation.dataclass.SleepTrack
import com.learning.meditation.repository.PlaylistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlaylistViewModel(private val repository: PlaylistRepository) : ViewModel() {

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> get() = _playlists

    private val _selectedPlaylist = MutableStateFlow<List<MeditationTrack>>(emptyList())
    val selectedPlaylist: StateFlow<List<MeditationTrack>> get() = _selectedPlaylist

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading

    fun fetchPlaylistsIfNeeded(userId: String) {
        if (_playlists.value.isEmpty()) {
            fetchPlaylists(userId)
        }
    }

    fun fetchPlaylistTracksIfNeeded(userId: String,playlistId: String) {
        fetchTrackByPlaylistId(userId,playlistId)
    }

    private fun fetchPlaylists(userId:String) {
        viewModelScope.launch {
            _loading.value = true
            val tracks = repository.fetchPlaylists(userId)
            _playlists.value = tracks
            _loading.value = false
        }
    }

    private fun fetchTrackByPlaylistId(userId: String, playlistId: String) {
        viewModelScope.launch {
            _loading.value = true
            val track = repository.fetchPlaylistTracks(userId,playlistId)
            _selectedPlaylist.value = track
            _loading.value = false
        }
    }
    fun addMeditationTrackToPlaylist(userId: String, playlist: Playlist, track: MeditationTrack, context: Context, onComplete: () -> Unit, onFailure: (Exception)->Unit) {
        repository.addMeditationTrackToPlaylist(userId, playlist, track, context,{
            onComplete()
            fetchPlaylists(userId)
         },
            onFailure)
    }

    fun addSleepTrackToPlaylist(userId: String, playlist: Playlist, track: SleepTrack, context: Context, onComplete: () -> Unit, onFailure: (Exception)->Unit) {
        repository.addSleepTrackToPlaylist(userId, playlist, track, context, onFailure)
    }

    fun addPlaylist(userId: String, playlist: Playlist, onComplete: () -> Unit, onFailure: (Exception) -> Unit) {
        repository.addPlaylistToFirestore(userId, playlist, {
            fetchPlaylists(userId)
            onComplete()
        }, onFailure)
    }

    fun deletePlaylist(userId: String, playlist: Playlist, onComplete: () -> Unit, onFailure: (Exception) -> Unit) {
        repository.deletePlaylistFromFirestore(userId, playlist, {
            fetchPlaylists(userId)
            onComplete()
        }, onFailure)
    }
}
