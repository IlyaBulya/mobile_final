package com.example.moviewatchlist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.moviewatchlist.ui.theme.MovieWatchlistTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.layout.fillMaxSize
import com.example.moviewatchlist.ui.theme.MainScreen
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.view.WindowCompat
import androidx.compose.runtime.SideEffect
import android.app.Activity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var darkTheme by rememberSaveable { mutableStateOf(false) }

            // Меняем цвет иконок status bar и navigation bar в зависимости от темы
            SideEffect {
                val window = (this@MainActivity as Activity).window
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }

            MovieWatchlistTheme(darkTheme = darkTheme) {
                var user by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }

                Surface(modifier = Modifier.fillMaxSize()) {
                    if (user != null) {
                        MainScreen(
                            onSignOut = {
                                FirebaseAuth.getInstance().signOut()
                                user = null
                            },
                            darkTheme = darkTheme,
                            onToggleTheme = { darkTheme = it }
                        )
                    } else {
                        AuthScreen(
                            onAuthSuccess = {
                                user = FirebaseAuth.getInstance().currentUser
                            }
                        )
                    }
                }
            }
        }
    }
}
