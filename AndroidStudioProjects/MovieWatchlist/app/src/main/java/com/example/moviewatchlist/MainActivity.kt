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



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MovieWatchlistTheme {
                var user by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }

                Surface(modifier = Modifier.fillMaxSize()) {
                    if (user != null) {
                        MainScreen(
                            onSignOut = {
                                FirebaseAuth.getInstance().signOut()
                                user = null
                            }
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
