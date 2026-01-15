package com.example.ardrawing.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val prompt: String,
    val imagePath: String? = null, // For future use if we save generated images
    val createdAt: Long = System.currentTimeMillis(),
    val type: String = "text_to_image" // Can be "text_to_image" or other types in future
)
