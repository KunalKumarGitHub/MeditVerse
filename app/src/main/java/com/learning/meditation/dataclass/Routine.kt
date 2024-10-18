package com.learning.meditation.dataclass

data class RoutineStep(
    var id: String="",
    val index:Int=0,
    var title: String="",
    var description: String="",
    var durationMinutes: Int=0,
    val isCompleted: Boolean = false
)

data class Routine(
    val id: String="",
    val name: String="",
    val stepCount: Int = 0
)
