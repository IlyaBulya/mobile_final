package com.example.moviewatchlist.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.animation.AnimatedVisibility
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    var title by remember { mutableStateOf("") }
                    var movie by remember { mutableStateOf<MovieResponse?>(null) }
                    var error by remember { mutableStateOf<String?>(null) }

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

                1 -> {
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

                2 -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Dark theme", style = MaterialTheme.typography.titleMedium)
                            Switch(
                                checked = darkTheme,
                                onCheckedChange = onToggleTheme,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        Button(
                            onClick = onSignOut,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Sign Out")
                        }
                    }
                }
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
