package com.learning.meditation.dataclass

import kotlinx.serialization.Serializable
import javax.annotation.concurrent.Immutable

@Immutable
@Serializable
data class SleepTrack(
    val trackId:String="",
    val title: String = "",
    val duration: String = "",
    val description: String = "",
    val audioUrl: String = "",
    val imageUrl: String = ""
)