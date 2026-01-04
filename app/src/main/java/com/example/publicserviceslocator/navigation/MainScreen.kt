package com.example.publicserviceslocator.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.publicserviceslocator.data.local.AppDatabase
import com.example.publicserviceslocator.data.remote.RetrofitClient
import com.example.publicserviceslocator.presentation.screens.*
import com.example.publicserviceslocator.presentation.viewmodels.*
import com.example.publicserviceslocator.repository.ServiceRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    userLocation: UserLocation,
    locationViewModel: LocationViewModel,
    database: AppDatabase,
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController()

    val repository = remember {
        ServiceRepository(
            api = RetrofitClient.placesApi,
            dao = database.favoriteDao()
        )
    }

    val sharedServiceViewModel: ServiceListViewModel = viewModel(
        factory = ServiceListViewModel.provideFactory(repository)
    )

    val bottomNavItems = listOf(Screen.List, Screen.Map, Screen.Favorites)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Service Locator") },
                actions = {
                    IconButton(onClick = {
                        //  Corrected 'databas' to 'database'
                        authViewModel.logout(database)
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.List.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.List.route) {
                ServiceListScreen(navController, userLocation, sharedServiceViewModel)
            }
            composable(Screen.Map.route) {
                MapScreen(navController, locationViewModel, sharedServiceViewModel)
            }
            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    navController = navController,
                    database = database
                )
            }
            composable(
                route = Screen.Details.route,
                arguments = listOf(navArgument("serviceId") { type = NavType.StringType })
            ) { backStackEntry ->
                val serviceId = backStackEntry.arguments?.getString("serviceId") ?: ""
                val detailsViewModel: ServiceDetailsViewModel = viewModel(
                    factory = ServiceDetailsViewModel.provideFactory(repository)
                )
                ServiceDetailsScreen(navController, serviceId, detailsViewModel)
            }
        }
    }
}