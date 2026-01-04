package com.example.publicserviceslocator.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.publicserviceslocator.data.local.AppDatabase
import com.example.publicserviceslocator.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(navController: NavController, database: AppDatabase) {
    // Observe the database directly
    val favorites by database.favoriteDao().getAllFavorites().collectAsState(initial = emptyList())

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Favorites") }) }
    ) { padding ->
        if (favorites.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No favorites saved yet.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(favorites) { favorite ->
                    ListItem(
                        headlineContent = { Text(favorite.name) },
                        supportingContent = { Text(favorite.vicinity) },
                        modifier = Modifier.clickable {
                            navController.navigate(Screen.Details.createRoute(favorite.placeId))
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}