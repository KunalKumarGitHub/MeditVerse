package com.learning.meditation.dataclass

import javax.annotation.concurrent.Immutable


@Immutable
data class Quote(
    val q:String,
    val a:String
)
