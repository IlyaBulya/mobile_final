package com.example.moviewatchlist.data

import androidx.room.*

@Dao
interface MovieDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movie: FavoriteMovieEntity)

    @Delete
    suspend fun delete(movie: FavoriteMovieEntity)

    @Query("SELECT * FROM favorite_movies")
    suspend fun getAll(): List<FavoriteMovieEntity>

    @Query("DELETE FROM favorite_movies WHERE imdbID = :id")
    suspend fun deleteById(id: String)
}
