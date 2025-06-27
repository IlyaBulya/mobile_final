package com.example.moviewatchlist.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.moviewatchlist.api.OmdbClient
import com.example.moviewatchlist.data.AppDatabase
import com.example.moviewatchlist.data.FavoriteMovieEntity
import com.example.moviewatchlist.model.MovieResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.platform.LocalContext

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val dao = AppDatabase.getDatabase(context).movieDao()
    val coroutineScope = rememberCoroutineScope()
    val featuredMovieIds = listOf("tt1375666", "tt0816692", "tt0111161", "tt0133093", "tt0114369")
    var featuredMovies by remember { mutableStateOf<List<MovieResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var movie by remember { mutableStateOf<MovieResponse?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        featuredMovies = withContext(Dispatchers.IO) {
            featuredMovieIds.mapNotNull {
                try {
                    val result = OmdbClient.api.getMovieById(it, "95d9495f")
                    if (result.Response == "True") result else null
                } catch (_: Exception) {
                    null
                }
            }
        }
        isLoading = false
    }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Enter movie title") },
            placeholder = { Text("e.g. Inception") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        Button(
            onClick = {
                coroutineScope.launch {
                    try {
                        val result = withContext(Dispatchers.IO) {
                            OmdbClient.api.getMovieByTitle(title.trim(), "95d9495f")
                        }
                        if (result.Response == "False") {
                            error = result.Error ?: "Unknown error"
                            movie = null
                        } else {
                            movie = result
                            error = null
                        }
                    } catch (e: Exception) {
                        error = "Network error: ${e.localizedMessage}"
                        movie = null
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Default.Search, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Search", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        AnimatedVisibility(visible = error != null) {
            error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
        if (title.isBlank()) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text("Featured Movies", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(featuredMovies) { movie ->
                        MovieCard(
                            movie = movie,
                            onAddToFavorites = {
                                coroutineScope.launch {
                                    val fav = FavoriteMovieEntity(
                                        imdbID = movie.imdbID ?: "",
                                        title = movie.Title ?: "",
                                        year = movie.Year ?: "",
                                        poster = movie.Poster ?: "",
                                        runtime = movie.Runtime ?: "",
                                        plot = movie.Plot ?: ""
                                    )
                                    withContext(Dispatchers.IO) {
                                        dao.insert(fav)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        } else {
            AnimatedVisibility(visible = movie != null) {
                movie?.let {
                    MovieCard(movie = it, onAddToFavorites = {
                        coroutineScope.launch {
                            val fav = FavoriteMovieEntity(
                                imdbID = it.imdbID ?: "",
                                title = it.Title ?: "",
                                year = it.Year ?: "",
                                poster = it.Poster ?: "",
                                runtime = it.Runtime ?: "",
                                plot = it.Plot ?: ""
                            )
                            withContext(Dispatchers.IO) {
                                dao.insert(fav)
                            }
                        }
                    })
                }
            }
        }
    }
} 