package com.example.publicserviceslocator.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.publicserviceslocator.BuildConfig
import com.example.publicserviceslocator.data.remote.ServiceResult
import com.example.publicserviceslocator.repository.ServiceRepository
import com.example.publicserviceslocator.utils.LocationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ServiceListViewModel(
    private val repository: ServiceRepository
) : ViewModel() {

    private val _masterServices = MutableStateFlow<List<ServiceUI>>(emptyList())
    private val _filteredServices = MutableStateFlow<List<ServiceUI>>(emptyList())
    val services: StateFlow<List<ServiceUI>> = _filteredServices

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _maxDistance = MutableStateFlow(10.0)
    val maxDistance: StateFlow<Double> = _maxDistance.asStateFlow()

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    init {
        // Reactive filtering logic
        viewModelScope.launch {
            combine(_masterServices, _searchQuery, _maxDistance) { masterList, query, maxDist ->
                masterList.filter { uiItem ->
                    val isWithinDistance = uiItem.distanceKm <= maxDist
                    val matchesSearch = if (query.isBlank()) true else {
                        uiItem.service.name?.contains(query, ignoreCase = true) == true ||
                                uiItem.service.vicinity?.contains(query, ignoreCase = true) == true
                    }
                    isWithinDistance && matchesSearch
                }
            }.collect { _filteredServices.value = it }
        }

        viewModelScope.launch {
            repository.getAllFavorites().collect { favs ->
                _favoriteIds.value = favs.map { it.placeId }.toSet()
            }
        }
    }

    fun onDistanceChanged(newDistance: Double) { _maxDistance.value = newDistance }
    fun onSearchQueryChanged(newQuery: String) { _searchQuery.value = newQuery }

    fun toggleFavorite(service: ServiceResult) {
        val id = service.place_id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            if (_favoriteIds.value.contains(id)) repository.removeFavorite(id)
            else repository.insertFavorite(service)
        }
    }

    fun loadServices(currentLocation: UserLocation) {
        if (currentLocation.latitude == 0.0 && currentLocation.longitude == 0.0) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val typesToFetch = listOf("hospital", "school", "pharmacy", "police", "park")
                val deferredResults = typesToFetch.map { type ->
                    async {
                        repository.getNearbyServices(
                            location = "${currentLocation.latitude},${currentLocation.longitude}",
                            type = type,
                            apiKey = BuildConfig.PUBLIC_SERVICES_API_KEY
                        )
                    }
                }
                val allApiResults = deferredResults.awaitAll().flatten()

                val uiServices = allApiResults
                    .filter { it.place_id != null }
                    .distinctBy { it.place_id }
                    .map { service ->
                        val distance = LocationUtils.calculateHaversineDistance(
                            currentLocation.latitude, currentLocation.longitude,
                            service.geometry?.location?.lat ?: 0.0,
                            service.geometry?.location?.lng ?: 0.0
                        )
                        ServiceUI(service, distance)
                    }
                _masterServices.value = uiServices.sortedBy { it.distanceKm }
            } catch (e: Exception) {
                Log.e("ViewModel", "Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    companion object {
        fun provideFactory(repository: ServiceRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T = ServiceListViewModel(repository) as T
            }
    }
}