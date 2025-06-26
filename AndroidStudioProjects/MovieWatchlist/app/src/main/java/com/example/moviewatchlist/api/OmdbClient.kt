package com.example.moviewatchlist.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OmdbClient {
    private const val BASE_URL = "https://www.omdbapi.com"

    val api: OmdbApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OmdbApi::class.java)
    }
}
