package com.learning.meditation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learning.meditation.dataclass.MeditationTrack
import com.learning.meditation.repository.MeditationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MeditationViewModel(private val repository: MeditationRepository) : ViewModel() {

    private val _meditationTracks = MutableStateFlow<List<MeditationTrack>>(emptyList())
    val meditationTracks: StateFlow<List<MeditationTrack>> get() = _meditationTracks

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading

    fun fetchMeditationTracksIfNeeded() {
        if (_meditationTracks.value.isEmpty()) {
            fetchMeditationTracks()
        }
    }

    private fun fetchMeditationTracks() {
        viewModelScope.launch {
            _loading.value = true
            val tracks = repository.getMeditationTracks()
            _meditationTracks.value = tracks
            _loading.value = false
        }
    }

    fun fetchRandomMeditationTrack(onTrackFetched: (MeditationTrack) -> Unit) {
        viewModelScope.launch {
            val tracks = repository.getMeditationTracks()
            if (tracks.isNotEmpty()) {
                val randomTrack = tracks.random()
                onTrackFetched(randomTrack)
            }
        }
    }
}
