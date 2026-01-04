package com.example.publicserviceslocator.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.example.publicserviceslocator.navigation.Screen
import com.example.publicserviceslocator.presentation.viewmodels.LocationViewModel
import com.example.publicserviceslocator.presentation.viewmodels.ServiceListViewModel
import com.google.android.gms.maps.CameraUpdateFactory

@Composable
fun MapScreen(
    navController: NavController,
    locationViewModel: LocationViewModel,
    listViewModel: ServiceListViewModel
) {
    val userLocation by locationViewModel.userLocation.collectAsState()
    val services by listViewModel.services.collectAsState()
    val isLoading by listViewModel.isLoading.collectAsState()

    val currentLatLng = LatLng(userLocation.latitude, userLocation.longitude)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLatLng, 14f)
    }

    LaunchedEffect(userLocation) {
        if (userLocation.latitude != 0.0 || userLocation.longitude != 0.0) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(currentLatLng, 14f),
                durationMs = 1000
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (userLocation.latitude == 0.0 && userLocation.longitude == 0.0) {
            Text(
                text = "Waiting for valid location data...",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = true)
            ) {
                // User's Location Marker
                Marker(
                    state = MarkerState(position = currentLatLng),
                    title = "Your Location"
                )

                // Service Markers
                services.forEach { uiItem ->
                    val service = uiItem.service
                    // FIX: Safe access to coordinates
                    val lat = service.geometry?.location?.lat
                    val lng = service.geometry?.location?.lng
                    val placeId = service.place_id

                    if (lat != null && lng != null && placeId != null) {
                        val serviceLatLng = LatLng(lat, lng)

                        Marker(
                            state = MarkerState(position = serviceLatLng),
                            title = service.name ?: "Unknown Service",
                            snippet = service.vicinity ?: "No address available",
                            onClick = {
                                navController.navigate(Screen.Details.createRoute(placeId))
                                true
                            }
                        )
                    }
                }
            }
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}