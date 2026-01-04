package com.example.publicserviceslocator.repository

import android.util.Log
import com.example.publicserviceslocator.data.local.FavoriteDao
import com.example.publicserviceslocator.data.local.FavoriteEntity
import com.example.publicserviceslocator.data.remote.PlacesApi
import com.example.publicserviceslocator.data.remote.ServiceResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class ServiceRepository(
    private val api: PlacesApi,
    private val dao: FavoriteDao
) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getNearbyServices(
        location: String,
        type: String,
        apiKey: String
    ): List<ServiceResult> {
        return try {
            val response = api.getNearbyServices(
                location = location,
                type = type,
                apiKey = apiKey,
                rankby = "distance",
                radius = null
            )

            if (response.isSuccessful) {
                response.body()?.results ?: emptyList()
            } else {
                Log.e("Repository", "API Error: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("Repository", "Network exception", e)
            emptyList()
        }
    }

    suspend fun getServiceDetails(placeId: String, apiKey: String): ServiceResult? {
        return try {
            val response = api.getPlaceDetails(placeId = placeId, apiKey = apiKey)
            if (response.isSuccessful) {
                response.body()?.result
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // --- Favorites Logic (Room + Firestore Sync) ---

    fun getAllFavorites(): Flow<List<FavoriteEntity>> = dao.getAllFavorites()

    fun isFavorite(id: String): Flow<Boolean> = dao.isFavorite(id)

    suspend fun insertFavorite(service: ServiceResult) {
        val id = service.place_id ?: return
        val userId = auth.currentUser?.uid ?: return

        val entity = FavoriteEntity(
            placeId = id,
            name = service.name ?: "Unknown Service",
            vicinity = service.vicinity ?: "No address available",
            type = service.types?.firstOrNull() ?: "Service"
        )

        try {
            dao.insertFavorite(entity)

            firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .document(id)
                .set(entity)
                .await()

            Log.d("Repository", "Cloud Sync Success: ${entity.name}")
        } catch (e: Exception) {
            Log.e("Repository", "Cloud Sync Failed", e)
        }
    }

    suspend fun removeFavorite(placeId: String) {
        val userId = auth.currentUser?.uid ?: return

        try {
            dao.deleteFavorite(FavoriteEntity(placeId, "", "", ""))

            firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .document(placeId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e("Repository", "Cloud Delete Failed", e)
        }
    }

    /**
     * Downloads all favorites from Firestore into the local Room database.
     * Call this immediately after Login to restore data.
     */
    suspend fun refreshFavoritesFromCloud() {
        val userId = auth.currentUser?.uid ?: return
        Log.d("Repository", "Starting Cloud Refresh for user: $userId")

        try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .get()
                .await()

            val cloudFavorites = snapshot.toObjects(FavoriteEntity::class.java)

            // Repopulate the local Room database
            cloudFavorites.forEach { favorite ->
                dao.insertFavorite(favorite)
            }

            Log.d("Repository", "Successfully pulled ${cloudFavorites.size} items from Cloud")
        } catch (e: Exception) {
            Log.e("Repository", "Cloud Refresh Failed", e)
        }
    }
}