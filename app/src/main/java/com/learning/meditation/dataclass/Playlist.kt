package com.learning.meditation.dataclass

import javax.annotation.concurrent.Immutable

data class Playlist(
    val playlistId:String="",
    val playlistName:String="",
    var trackCount:Int=0
)
