package com.learning.meditation.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learning.meditation.dataclass.Routine
import com.learning.meditation.dataclass.RoutineStep
import com.learning.meditation.repository.RoutineRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RoutineViewModel(private val repository: RoutineRepository) : ViewModel() {

    private val _routines = MutableStateFlow<List<Routine>>(emptyList())
    val routines: StateFlow<List<Routine>> = _routines

    private val _routineSteps = MutableStateFlow<List<RoutineStep>>(emptyList())
    val routineSteps: StateFlow<List<RoutineStep>> = _routineSteps

    private val _selectedRoutine = MutableStateFlow<Routine?>(null)
    val selectedRoutine: StateFlow<Routine?> = _selectedRoutine

    private val _currentStepIndex = MutableStateFlow(0)
    val currentStepIndex: StateFlow<Int> = _currentStepIndex

    private val _remainingTime = MutableStateFlow(0)
    val remainingTime: StateFlow<Int> = _remainingTime

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning

    private var timerJob: Job? = null

    fun fetchRoutinesIfNeeded(userId: String) {
        if (_routines.value.isEmpty()) {
            loadRoutines(userId)
        }
    }

    private fun loadRoutines(userId: String) {
        viewModelScope.launch {
            _isLoading.value=true
            _routines.value = repository.fetchRoutines(userId)
            _isLoading.value=false
        }
    }

    fun fetchRoutineStepsIfNeeded(userId: String, routineId: String) {
        fetchStepsByRoutineId(userId, routineId)
    }

    private fun fetchStepsByRoutineId(userId: String, routineId: String) {
        viewModelScope.launch {
            _routineSteps.value = repository.fetchRoutineSteps(userId, routineId)
        }
    }

    fun selectRoutine(routine: Routine, userId: String) {
        resetTimer()
        _selectedRoutine.value = routine
        _currentStepIndex.value = 0

        viewModelScope.launch {
            fetchRoutineStepsIfNeeded(userId, routine.id)
            _routineSteps.collect { steps ->
                if (steps.isNotEmpty()) {
                    setRemainingTime(steps[0].durationMinutes * 60)
                }
            }
        }
    }

    fun editStep(userId: String, title: String, desc: String, duration: Int, routineId: String, stepId: String) {
        viewModelScope.launch {
            repository.editStepOfRoutine(userId,routineId, stepId, title, desc, duration)
        }
        fetchRoutineStepsIfNeeded(userId, routineId)
        setRemainingTime(duration*60)
        stopTimer()

    }

    fun notSelectRoutine() {
        resetTimer()
        _currentStepIndex.value=0
        _selectedRoutine.value = null
    }

    fun startTimer(duration: Int) {
        if (!_isTimerRunning.value) {
            _isTimerRunning.value = true
            if (_remainingTime.value == 0) {
                _remainingTime.value = duration
            }
            timerJob = viewModelScope.launch {
                while (_remainingTime.value > 0) {
                    delay(1000)
                    _remainingTime.value -= 1
                }
                resetTimer()
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _isTimerRunning.value = false
    }

    fun resetTimer() {
        stopTimer()
        _remainingTime.value = 0
    }

    fun markStepCompleted(stepIndex: Int) {
        stopTimer()
        val updatedSteps = _routineSteps.value.mapIndexed { index, step ->
            if (index == stepIndex) step.copy(isCompleted = true) else step
        }
        _routineSteps.value = updatedSteps
    }

    fun goToNextStep() {
        if (_currentStepIndex.value < _routineSteps.value.size -1) {
            _currentStepIndex.value += 1
            val nextStepDuration = _routineSteps.value[_currentStepIndex.value].durationMinutes
            setRemainingTime(nextStepDuration * 60)
        }
        else {
            _currentStepIndex.value += 1
        }
    }

    fun setRemainingTime(duration: Int) {
        _remainingTime.value = duration
    }

    fun addRoutine(userId: String, newRoutine: Routine,steps:List<RoutineStep>) {
        viewModelScope.launch {
            repository.addRoutine(userId, newRoutine,steps)
            loadRoutines(userId)
        }
    }

    fun deleteRoutine(userId: String, routine: Routine) {
        viewModelScope.launch {
            repository.deleteRoutine(userId, routine)
            _routines.value = _routines.value.filter { it.id != routine.id }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}


