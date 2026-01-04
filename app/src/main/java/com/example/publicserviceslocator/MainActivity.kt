package com.example.publicserviceslocator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.publicserviceslocator.data.local.AppDatabase
import com.example.publicserviceslocator.data.remote.RetrofitClient
import com.example.publicserviceslocator.navigation.MainScreen
import com.example.publicserviceslocator.presentation.screens.AuthScreen
import com.example.publicserviceslocator.presentation.screens.LocationPermissionScreen
import com.example.publicserviceslocator.presentation.screens.SignUpScreen
import com.example.publicserviceslocator.presentation.viewmodels.AuthViewModel
import com.example.publicserviceslocator.presentation.viewmodels.LocationViewModel
import com.example.publicserviceslocator.repository.ServiceRepository
import com.example.publicserviceslocator.ui.theme.PublicServicesLocatorTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "public_services_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PublicServicesLocatorTheme {
                val authViewModel: AuthViewModel = viewModel()
                val locationViewModel: LocationViewModel = viewModel()

                val repository = remember {
                    ServiceRepository(
                        api = RetrofitClient.placesApi,
                        dao = database.favoriteDao()
                    )
                }

                AppNavigation(
                    authViewModel = authViewModel,
                    locationViewModel = locationViewModel,
                    repository = repository
                )
            }
        }
    }

    @Composable
    fun AppNavigation(
        authViewModel: AuthViewModel,
        locationViewModel: LocationViewModel,
        repository: ServiceRepository
    ) {
        val navController = rememberNavController()
        val authState by authViewModel.authState.collectAsState()
        val scope = rememberCoroutineScope()

        // AUTO-SYNC & NAVIGATION LOGIC
        LaunchedEffect(authState.user) {
            val user = authState.user
            if (user == null) {
                // If user is logged out, clear backstack and go to Auth
                navController.navigate("auth_screen") {
                    popUpTo(0)
                }
            } else {
                // USER LOGGED IN: Restore favorites from Firestore to Room
                scope.launch {
                    repository.refreshFavoritesFromCloud()
                }
            }
        }

        NavHost(
            navController = navController,
            startDestination = if (authState.user != null) "main_app_route" else "auth_screen"
        ) {
            composable("auth_screen") {
                AuthScreen(
                    viewModel = authViewModel,
                    onAuthSuccess = {
                        navController.navigate("main_app_route") {
                            popUpTo("auth_screen") { inclusive = true }
                        }
                    },
                    onNavigateToSignUp = {
                        navController.navigate("sign_up_screen")
                    }
                )
            }

            composable("sign_up_screen") {
                SignUpScreen(
                    authViewModel = authViewModel,
                    onNavigateToLogin = { navController.popBackStack() },
                    onSignUpSuccess = {
                        navController.navigate("main_app_route") {
                            popUpTo("auth_screen") { inclusive = true }
                        }
                    }
                )
            }

            composable("main_app_route") {
                LocationPermissionScreen(locationViewModel = locationViewModel) { userLocation ->
                    MainScreen(
                        userLocation = userLocation,
                        locationViewModel = locationViewModel,
                        database = database,
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}