package com.example.moviewatchlist.model

import com.google.gson.annotations.SerializedName

data class MovieResponse(
    @SerializedName("Title") val Title: String?,
    @SerializedName("Year") val Year: String?,
    @SerializedName("Poster") val Poster: String?,
    @SerializedName("Runtime") val Runtime: String?,
    @SerializedName("Plot") val Plot: String?,
    @SerializedName("imdbID") val imdbID: String?,
    @SerializedName("Response") val Response: String?,
    @SerializedName("Error") val Error: String?
)
