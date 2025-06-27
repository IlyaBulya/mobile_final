package com.example.moviewatchlist.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.moviewatchlist.api.OmdbClient
import com.example.moviewatchlist.data.AppDatabase
import com.example.moviewatchlist.data.FavoriteMovieEntity
import com.example.moviewatchlist.model.MovieResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.graphics.Color

@Composable
fun MainScreen(
    onSignOut: () -> Unit,
    darkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val dao = AppDatabase.getDatabase(context).movieDao()
    val coroutineScope = rememberCoroutineScope()
    var favorites by remember { mutableStateOf<List<FavoriteMovieEntity>>(emptyList()) }

    // 🎬 Featured Movies
    val featuredMovieIds = listOf("tt1375666", "tt0816692", "tt0111161", "tt0133093", "tt0114369")
    var featuredMovies by remember { mutableStateOf<List<MovieResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

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

    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) {
            favorites = withContext(Dispatchers.IO) { dao.getAll() }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = { Text("Home") },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = { Text("Favorites") },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    label = { Text("Account") },
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> HomeScreen()
                1 -> FavouritesScreen()
                2 -> ProfileScreen(darkTheme = darkTheme, onToggleTheme = onToggleTheme, onSignOut = onSignOut)
            }
        }
    }
}

@Composable
fun MovieCard(
    movie: MovieResponse,
    onAddToFavorites: (() -> Unit)? = null,
    onRemove: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = movie.Poster,
                    contentDescription = null,
                    modifier = Modifier
                        .size(160.dp)
                        .padding(end = 16.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = movie.Title ?: "No title",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = movie.Year ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = movie.Runtime ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(movie.Plot ?: "", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                onAddToFavorites?.let {
                    Button(onClick = it, shape = MaterialTheme.shapes.medium) {
                        Icon(Icons.Default.Favorite, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Add to Favorites")
                    }
                }
                onRemove?.let {
                    Button(
                        onClick = it,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Remove")
                    }
                }
            }
        }
    }
}
