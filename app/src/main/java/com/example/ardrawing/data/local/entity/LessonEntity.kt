package com.example.ardrawing.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.ardrawing.data.local.converter.LessonStepConverter

@Entity(tableName = "lessons")
@TypeConverters(LessonStepConverter::class)
data class LessonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val stepsJson: String, // JSON string of steps
    val createdAt: Long = System.currentTimeMillis()
)

