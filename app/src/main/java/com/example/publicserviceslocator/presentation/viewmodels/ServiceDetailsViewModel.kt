package com.example.publicserviceslocator.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.publicserviceslocator.BuildConfig
import com.example.publicserviceslocator.data.remote.ServiceResult
import com.example.publicserviceslocator.repository.ServiceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ServiceDetailsViewModel(private val repository: ServiceRepository) : ViewModel() {

    private val _details = MutableStateFlow<ServiceResult?>(null)
    val details = _details.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite = _isFavorite.asStateFlow()

    fun fetchDetails(placeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetch details from repository
                val result = repository.getServiceDetails(
                    placeId = placeId,
                    apiKey = BuildConfig.PUBLIC_SERVICES_API_KEY
                )
                _details.value = result

                // Check if this specific result is already a favorite
                result?.place_id?.let { checkIfFavorite(it) }
            } catch (e: Exception) {
                Log.e("DetailsVM", "Fetch error: ${e.message}", e)
                _details.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkIfFavorite(id: String) {
        if (id.isEmpty()) return
        viewModelScope.launch {
            repository.isFavorite(id).collectLatest { status ->
                _isFavorite.value = status
            }
        }
    }

    fun toggleFavorite(service: ServiceResult) {
        val safeId = service.place_id ?: ""
        if (safeId.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (_isFavorite.value) {
                    repository.removeFavorite(safeId)
                } else {
                    repository.insertFavorite(service)
                }
            } catch (e: Exception) {
                Log.e("DetailsVM", "Database error: ${e.message}")
            }
        }
    }

    companion object {
        fun provideFactory(repository: ServiceRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ServiceDetailsViewModel(repository) as T
                }
            }
    }
}