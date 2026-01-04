package com.example.publicserviceslocator.features.citysearch

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.publicserviceslocator.presentation.viewmodels.UserLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class CitySearchViewModel : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    fun onQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    /**
     * UPDATED: Now uses Coroutines for non-blocking network calls.
     * This prevents UI freezes and ensures the search actually executes.
     */
    fun performCitySearch(context: Context, onLocationFound: (UserLocation) -> Unit) {
        val query = _searchQuery.value
        if (query.isBlank()) return

        viewModelScope.launch {
            _isSearching.value = true
            try {
                // Shift to IO thread because Geocoding requires a network request
                val location = withContext(Dispatchers.IO) {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocationName(query, 1)

                    if (!addresses.isNullOrEmpty()) {
                        val addr = addresses[0]
                        Log.d("CitySearch", "Found: ${addr.latitude}, ${addr.longitude}")
                        UserLocation(addr.latitude, addr.longitude)
                    } else {
                        null
                    }
                }

                // Return to Main thread to update UI/Callback
                location?.let {
                    onLocationFound(it)
                } ?: Log.e("CitySearch", "No coordinates found for query: $query")

            } catch (e: Exception) {
                Log.e("CitySearch", "Geocoding error: ${e.message}")
                e.printStackTrace()
            } finally {
                _isSearching.value = false
            }
        }
    }
}