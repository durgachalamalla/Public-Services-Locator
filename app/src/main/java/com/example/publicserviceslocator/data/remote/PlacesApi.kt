package com.example.publicserviceslocator.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApi {
    @GET("nearbysearch/json")
    suspend fun getNearbyServices(
        @Query("location") location: String,
        @Query("type") type: String,
        @Query("key") apiKey: String,
        @Query("rankby") rankby: String?, // FIXED: Changed 'rankBy' to 'rankby' (lowercase)
        @Query("radius") radius: Int? = null
    ): Response<ServiceResponse>

    @GET("details/json")
    suspend fun getPlaceDetails(
        @Query("place_id") placeId: String,
        @Query("fields") fields: String = "name,rating,formatted_phone_number,vicinity,place_id,website,opening_hours,geometry",
        @Query("key") apiKey: String
    ): Response<ServiceDetailResponse>
}