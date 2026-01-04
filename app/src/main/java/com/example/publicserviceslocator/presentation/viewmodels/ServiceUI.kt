package com.example.publicserviceslocator.presentation.viewmodels

import com.example.publicserviceslocator.data.remote.ServiceResult

// This model adds the calculated distance to the API result for UI use
data class ServiceUI(
    val service: ServiceResult,
    val distanceKm: Double // The calculated distance
)
