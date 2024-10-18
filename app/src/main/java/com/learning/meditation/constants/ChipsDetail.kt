package com.learning.meditation.constants

import android.content.Context
import com.google.gson.Gson

data class ChipDetail(
    val name: String,
    val intro: String,
    val symptoms: List<String>,
    val diagnosis: String,
    val treatmentIntro:String,
    val treatments: List<Treatment>
)

data class Treatment(
    val name: String,
    val description: String
)

fun getChipsData(context: Context): List<ChipDetail> {
    val jsonString = context.assets.open("chip_data.json").bufferedReader().use { it.readText() }
    val gson = Gson()
    val chipsData = gson.fromJson(jsonString, ChipsData::class.java)
    return chipsData.chips
}

data class ChipsData(
    val chips: List<ChipDetail>
)