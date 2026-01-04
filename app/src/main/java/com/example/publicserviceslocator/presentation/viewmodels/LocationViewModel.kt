package com.example.publicserviceslocator.presentation.viewmodels


import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

// Standardized data class for the whole project
data class UserLocation(val latitude: Double = 0.0, val longitude: Double = 0.0)

// Sealed class to handle different UI states
sealed class LocationState {
    object Idle : LocationState()
    object Loading : LocationState()
    data class Success(val location: UserLocation) : LocationState()
    data class Error(val message: String) : LocationState()
}

class LocationViewModel : ViewModel() {

    // Main state for the UI to observe
    private val _locationState = MutableStateFlow<LocationState>(LocationState.Idle)
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    private val _userLocation = MutableStateFlow(UserLocation())
    val userLocation: StateFlow<UserLocation> = _userLocation

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission

    fun setHasPermission(isGranted: Boolean) {
        _hasPermission.value = isGranted
    }

    fun updateManualLocation(newLocation: UserLocation) {
        _userLocation.value = newLocation
        _locationState.value = LocationState.Success(newLocation)
    }

    @SuppressLint("MissingPermission")
    fun fetchUserLocation(fusedLocationClient: FusedLocationProviderClient) {
        viewModelScope.launch {
            _locationState.value = LocationState.Loading

            try {
                // withTimeoutOrNull ensures we don't wait forever (10 seconds)
                val location = withTimeoutOrNull(10000) {
                    // Try to get the last known location first
                    val lastLoc = fusedLocationClient.lastLocation.await()
                    if (lastLoc != null) {
                        lastLoc
                    } else {
                        // If no last location, request a fresh high-accuracy location
                        fusedLocationClient.getCurrentLocation(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            null
                        ).await()
                    }
                }

                if (location != null) {
                    val newLoc = UserLocation(location.latitude, location.longitude)
                    _userLocation.value = newLoc
                    _locationState.value = LocationState.Success(newLoc)
                } else {
                    // This triggers if 10 seconds pass without a signal
                    _locationState.value = LocationState.Error("GPS signal timed out. Please check your settings.")
                }
            } catch (e: SecurityException) {
                _locationState.value = LocationState.Error("Location permission denied.")
            } catch (e: Exception) {
                _locationState.value = LocationState.Error("An error occurred: ${e.localizedMessage}")
            }
        }
    }
}



//https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=40.7128,-74.0060&radius=5000&type=hospital&key=AIzaSyB9j-wGrkgjA7teXJGaoxrs5baqgkVnSZg

//https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=43.5183,-79.8774&radius=5000&type=hospital&key=AIzaSyB9j-wGrkgjA7teXJGaoxrs5baqgkVnSZg   //Milton