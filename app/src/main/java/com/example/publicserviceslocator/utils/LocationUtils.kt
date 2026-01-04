package com.example.publicserviceslocator.utils

import com.example.publicserviceslocator.presentation.viewmodels.UserLocation // Your data class
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// Define the radius of the Earth in kilometers
private const val EARTH_RADIUS_KM = 6371.0

object LocationUtils {

    /**
     * Calculates the distance between two latitude/longitude points using the Haversine formula.
     * @param lat1 Latitude of the starting point (User's location)
     * @param lon1 Longitude of the starting point (User's location)
     * @param lat2 Latitude of the ending point (Service's location)
     * @param lon2 Longitude of the ending point (Service's location)
     * @return Distance in kilometers (Double)
     */
    fun calculateHaversineDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        // Convert degrees to radians
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        // Distance = R * c (where R is the Earth's radius)
        return EARTH_RADIUS_KM * c
    }
}