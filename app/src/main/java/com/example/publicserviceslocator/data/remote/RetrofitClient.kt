package com.example.publicserviceslocator.data.remote




import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Google Places API Base URL
   // private const val BASE_URL = "https://maps.googleapis.com/"
    private const val BASE_URL = "https://maps.googleapis.com/maps/api/place/"

    // Create a logger for debugging network traffic
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Logs request and response body
    }

    // Create the OkHttpClient, adding the logger
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // Initialize Retrofit
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Expose the PlacesApi service instance
    val placesApi: PlacesApi by lazy {
        retrofit.create(PlacesApi::class.java)
    }
}