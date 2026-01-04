package com.example.publicserviceslocator.data



import androidx.room.Entity
import androidx.room.PrimaryKey

// Represents a service saved locally in the Room Database
@Entity(tableName = "services")
data class ServiceEntity(
    // Auto-generate ID for Room, used internally
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    // ID from the external API (e.g., Google Place ID)
    val placeId: String,
    val name: String,
    val category: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val isFavorite: Boolean // Boolean to track favorite status
)