package com.example.publicserviceslocator.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object List : Screen("list_route", "Nearby", Icons.Filled.List)
    data object Map : Screen("map_route", "Map", Icons.Filled.LocationOn)
    data object Favorites : Screen("favorites_route", "Favorites", Icons.Filled.Favorite)

    // The route string MUST match exactly what is in the NavHost
    data object Details : Screen("details_route/{serviceId}", "Details", Icons.Filled.Info) {
        fun createRoute(serviceId: String) = "details_route/$serviceId"
    }
}