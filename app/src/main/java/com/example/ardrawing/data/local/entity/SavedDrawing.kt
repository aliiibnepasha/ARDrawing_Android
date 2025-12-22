package com.example.ardrawing.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "saved_drawings")
data class SavedDrawing(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val templateId: String,
    val templateName: String,
    val imageAssetPath: String,
    val sourceType: String, // "Camera" or "Paper Trace"
    val createdAt: Long = System.currentTimeMillis(),
    val isSaved: Boolean = true
)

