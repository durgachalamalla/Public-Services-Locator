package com.example.publicserviceslocator.presentation.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.publicserviceslocator.presentation.viewmodels.LocationState
import com.example.publicserviceslocator.presentation.viewmodels.LocationViewModel
import com.example.publicserviceslocator.presentation.viewmodels.UserLocation
import com.google.android.gms.location.LocationServices

@Composable
fun LocationPermissionScreen(
    locationViewModel: LocationViewModel,
    content: @Composable (UserLocation) -> Unit
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Collect states from ViewModel
    val hasPermission by locationViewModel.hasPermission.collectAsState()
    val locationState by locationViewModel.locationState.collectAsState()

    // Launcher for requesting permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        locationViewModel.setHasPermission(isGranted)

        if (isGranted) {
            locationViewModel.fetchUserLocation(fusedLocationClient)
        }
    }

    // Initial check for permissions
    LaunchedEffect(Unit) {
        if (checkLocationPermissions(context)) {
            locationViewModel.setHasPermission(true)
            locationViewModel.fetchUserLocation(fusedLocationClient)
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (!hasPermission) {
            // Permission Denied State
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Location access is required to find nearby services.",
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }) {
                    Text("Grant Permission")
                }
            }
        } else {
            // Permission Granted: Handle Location States
            when (val state = locationState) {
                is LocationState.Loading -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Text(
                            text = "Fetching GPS location...",
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }

                is LocationState.Success -> {
                    // Show the main app content
                    content(state.location)
                }

                is LocationState.Error -> {
                    // Timeout or error state: Show Retry button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = state.message,
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            locationViewModel.fetchUserLocation(fusedLocationClient)
                        }) {
                            Text("Retry GPS Connection")
                        }
                    }
                }

                is LocationState.Idle -> {
                    // Do nothing or show a placeholder
                }
            }
        }
    }
}

private fun checkLocationPermissions(context: Context): Boolean {
    val fineLocation = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val coarseLocation = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    return fineLocation || coarseLocation
}