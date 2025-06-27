package com.example.moviewatchlist.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.moviewatchlist.data.AppDatabase
import com.example.moviewatchlist.data.FavoriteMovieEntity
import com.example.moviewatchlist.model.MovieResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalContext

@Composable
fun FavouritesScreen() {
    val context = LocalContext.current
    val dao = AppDatabase.getDatabase(context).movieDao()
    val coroutineScope = rememberCoroutineScope()
    var favorites by remember { mutableStateOf<List<FavoriteMovieEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        favorites = withContext(Dispatchers.IO) { dao.getAll() }
    }

    if (favorites.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("No favorite movies", color = Color.Gray)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(favorites) { fav ->
                MovieCard(
                    movie = MovieResponse(
                        Title = fav.title,
                        Year = fav.year,
                        Poster = fav.poster,
                        Runtime = fav.runtime,
                        Plot = fav.plot,
                        imdbID = fav.imdbID,
                        Response = "True",
                        Error = null
                    ),
                    onRemove = {
                        coroutineScope.launch {
                            withContext(Dispatchers.IO) {
                                dao.deleteById(fav.imdbID)
                            }
                            favorites = withContext(Dispatchers.IO) {
                                dao.getAll()
                            }
                        }
                    }
                )
            }
        }
    }
} 