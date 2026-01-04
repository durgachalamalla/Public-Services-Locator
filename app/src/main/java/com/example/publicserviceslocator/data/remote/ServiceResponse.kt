package com.example.publicserviceslocator.data.remote

import com.google.gson.annotations.SerializedName

// Top-level JSON response for Nearby Search
data class ServiceResponse(
    @SerializedName("results")
    val results: List<ServiceResult>,
    @SerializedName("status")
    val status: String
)

// Represents a single service with BOTH Nearby and Details fields
data class ServiceResult(
    // CHANGED: Made place_id nullable with a default value to prevent NullPointerExceptions
    @SerializedName("place_id")
    val place_id: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("vicinity")
    val vicinity: String? = null,

    @SerializedName("types")
    val types: List<String>? = null,

    @SerializedName("geometry")
    val geometry: Geometry? = null,

    // --- FIELDS FOR DETAILS SCREEN ---
    @SerializedName("rating")
    val rating: Double? = null,

    @SerializedName("formatted_phone_number")
    val formatted_phone_number: String? = null,

    @SerializedName("website")
    val website: String? = null,

    @SerializedName("opening_hours")
    val opening_hours: OpeningHours? = null
)

// --- SUB-CLASSES FOR NESTED JSON ---

data class Geometry(
    @SerializedName("location")
    val location: Location
)

data class Location(
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lng")
    val lng: Double
)

data class OpeningHours(
    @SerializedName("open_now")
    val open_now: Boolean? = null
)

// Top-level JSON response for Place Details API
data class ServiceDetailResponse(
    @SerializedName("result")
    val result: ServiceResult?,
    @SerializedName("status")
    val status: String
)