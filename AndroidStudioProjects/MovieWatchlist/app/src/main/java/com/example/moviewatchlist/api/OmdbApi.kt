package com.example.moviewatchlist.api

import com.example.moviewatchlist.model.MovieResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OmdbApi {

    @GET("/")
    suspend fun getMovieByTitle(
        @Query("t") title: String,
        @Query("apikey") apiKey: String
    ): MovieResponse

    @GET("/")
    suspend fun getMovieById(
        @Query("i") imdbID: String,
        @Query("apikey") apiKey: String
    ): MovieResponse
}
