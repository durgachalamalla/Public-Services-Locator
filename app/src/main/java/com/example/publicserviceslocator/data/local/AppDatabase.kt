package com.example.publicserviceslocator.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.publicserviceslocator.data.ServiceEntity

@Database(
    entities = [ServiceEntity::class, FavoriteEntity::class], // Both entities go here
    version = 2, // Increment version if you already ran the app with version 1
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serviceDao(): ServiceDao
    abstract fun favoriteDao(): FavoriteDao
}


//https://developers.google.com/maps/documentation/android-sdk/start