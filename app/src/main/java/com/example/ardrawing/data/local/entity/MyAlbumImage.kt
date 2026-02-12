package com.example.ardrawing.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "my_album_images")
data class MyAlbumImage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uri: String,
    val timestamp: Long = System.currentTimeMillis()
)
