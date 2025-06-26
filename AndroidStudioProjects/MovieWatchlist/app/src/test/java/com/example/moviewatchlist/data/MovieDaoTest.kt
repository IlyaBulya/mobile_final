package com.example.moviewatchlist.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MovieDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: MovieDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = db.movieDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetAll() = runBlocking {
        val movie = FavoriteMovieEntity(
            imdbID = "tt1234567",
            title = "Test Movie",
            year = "2024",
            poster = "url",
            runtime = "120 min",
            plot = "Test plot"
        )
        dao.insert(movie)
        val all = dao.getAll()
        assertEquals(1, all.size)
        assertEquals("Test Movie", all[0].title)
    }

    @Test
    fun deleteById() = runBlocking {
        val movie = FavoriteMovieEntity(
            imdbID = "tt1234567",
            title = "Test Movie",
            year = "2024",
            poster = "url",
            runtime = "120 min",
            plot = "Test plot"
        )
        dao.insert(movie)
        dao.deleteById("tt1234567")
        val all = dao.getAll()
        assertTrue(all.isEmpty())
    }

    @Test
    fun deleteMovie() = runBlocking {
        val movie = FavoriteMovieEntity(
            imdbID = "tt1234567",
            title = "Test Movie",
            year = "2024",
            poster = "url",
            runtime = "120 min",
            plot = "Test plot"
        )
        dao.insert(movie)
        dao.delete(movie)
        val all = dao.getAll()
        assertTrue(all.isEmpty())
    }
} 