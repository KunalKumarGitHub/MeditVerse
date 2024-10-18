package com.learning.meditation.constants

import com.learning.meditation.dataclass.Quote
import retrofit2.Call
import retrofit2.http.GET

interface ZenQuotesApi {
    @GET("random")
    fun getRandomQuote(): Call<List<Quote>>
}