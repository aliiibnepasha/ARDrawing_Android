package com.example.ardrawing.data.model

data class Lesson(
    val id: String,
    val name: String,
    val description: String,
    val steps: List<LessonStep>
)

data class LessonStep(
    val stepNumber: Int,
    val title: String,
    val imageAssetPath: String
)

