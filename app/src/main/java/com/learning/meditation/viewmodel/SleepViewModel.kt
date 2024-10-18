package com.learning.meditation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learning.meditation.dataclass.MeditationTrack
import com.learning.meditation.dataclass.SleepTrack
import com.learning.meditation.repository.SleepRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SleepViewModel(private val repository: SleepRepository) : ViewModel() {

    private val _sleepTracks = MutableStateFlow<List<SleepTrack>>(emptyList())
    val sleepTracks: StateFlow<List<SleepTrack>> get() = _sleepTracks

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading

    fun fetchSleepTracksIfNeeded() {
        if (_sleepTracks.value.isEmpty()) {
            fetchSleepTracks()
        }
    }

    private fun fetchSleepTracks() {
        viewModelScope.launch {
            _loading.value = true
            val tracks = repository.getSleepTracks()
            _sleepTracks.value = tracks
            _loading.value = false
        }
    }

    fun fetchRandomSleepTrack(onTrackFetched: (SleepTrack) -> Unit) {
        viewModelScope.launch {
            val tracks = repository.getSleepTracks()
            if (tracks.isNotEmpty()) {
                val randomTrack = tracks.random()
                onTrackFetched(randomTrack)
            }
        }
    }
}
