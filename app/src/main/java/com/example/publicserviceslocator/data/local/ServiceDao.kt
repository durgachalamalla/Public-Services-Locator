package com.example.publicserviceslocator.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.publicserviceslocator.data.ServiceEntity

@Dao
interface ServiceDao {

    // Retrieve only services marked as favorite
    @Query("SELECT * FROM services WHERE isFavorite = 1")
    fun getFavoriteServices(): List<ServiceEntity> // Marked as fun (non-suspending) or suspend depending on usage

    // Insert a new service or replace if the primary key conflicts (useful for update/upsert)
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertService(service: ServiceEntity)

    // Delete a service
    @Delete
    suspend fun deleteService(service: ServiceEntity)

    // Retrieve a single service by its external ID (for checking existence)
    @Query("SELECT * FROM services WHERE placeId = :placeId LIMIT 1")
    fun getServiceByPlaceId(placeId: String): ServiceEntity?
}