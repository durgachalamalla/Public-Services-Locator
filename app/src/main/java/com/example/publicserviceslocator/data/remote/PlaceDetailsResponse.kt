package com.example.publicserviceslocator.data.remote

data class PlaceDetailsResponse(
    val result: ServiceResult, // Reuses your existing ServiceResult
    val status: String
)

// If your existing ServiceResult doesn't have phone/website/rating,
// you should update it or create a specific one for details:
data class DetailedServiceResult(
    val name: String,
    val vicinity: String,
    val formatted_phone_number: String?,
    val rating: Double?,
    val website: String?,
    val opening_hours: OpeningHours?,
    val photos: List<Photo>?
)



data class Photo(
    val photo_reference: String
)