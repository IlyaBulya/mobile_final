package com.example.moviewatchlist.model

import org.junit.Assert.assertEquals
import org.junit.Test

class MovieResponseTest {

    @Test
    fun createMovieResponse() {
        val movie = MovieResponse(
            Title = "Inception",
            Year = "2010",
            Poster = "url",
            Runtime = "148 min",
            Plot = "A mind-bending thriller",
            imdbID = "tt1375666",
            Response = "True",
            Error = null
        )
        assertEquals("Inception", movie.Title)
        assertEquals("2010", movie.Year)
        assertEquals("url", movie.Poster)
        assertEquals("148 min", movie.Runtime)
        assertEquals("A mind-bending thriller", movie.Plot)
        assertEquals("tt1375666", movie.imdbID)
        assertEquals("True", movie.Response)
        assertEquals(null, movie.Error)
    }
} 