package com.example.publicserviceslocator.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val placeId: String = "", // Default value for Firebase
    val name: String = "",    // Default value for Firebase
    val vicinity: String = "", // Default value for Firebase
    val type: String = "Service"
)