package com.learning.meditation.dataclass


import androidx.annotation.DrawableRes
import javax.annotation.concurrent.Immutable

@Immutable
data class BottomMenuContent(
    val title: String,
    val route:String,
    @DrawableRes val iconId: Int
)