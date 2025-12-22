package com.example.ardrawing.data.repository

import android.content.Context
import com.example.ardrawing.data.model.Lesson
import com.example.ardrawing.data.model.LessonStep
import com.example.ardrawing.data.utils.AssetUtils

object LessonRepository {
    private const val LESSONS_FOLDER = "lessons"
    private val createdLessons = mutableListOf<Lesson>()
    
    /**
     * Gets all available lessons (from assets and created lessons)
     */
    fun getLessons(context: Context): List<Lesson> {
        val lessons = mutableListOf<Lesson>()
        
        // Add created lessons (from images)
        lessons.addAll(createdLessons)
        
        // Check if lessons folder exists in assets
        val lessonFolders = try {
            context.assets.list(LESSONS_FOLDER)?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        
        // Load lessons from assets
        val assetLessons = lessonFolders.mapIndexed { index, folderName ->
            val stepImages = AssetUtils.listImageFiles(context, "$LESSONS_FOLDER/$folderName")
            Lesson(
                id = "asset_${index + 1}",
                name = folderName.replaceFirstChar { it.uppercaseChar() },
                description = "Learn to draw step by step",
                steps = stepImages.mapIndexed { stepIndex, imageName ->
                    LessonStep(
                        stepNumber = stepIndex + 1,
                        title = "Step ${stepIndex + 1}",
                        imageAssetPath = AssetUtils.getAssetPath("$LESSONS_FOLDER/$folderName", imageName)
                    )
                }
            )
        }
        lessons.addAll(assetLessons)
        
        // If no lessons at all, create a sample lesson
        if (lessons.isEmpty()) {
            return createSampleLesson(context)
        }
        
        return lessons
    }
    
    /**
     * Adds a newly created lesson
     */
    fun addCreatedLesson(lesson: Lesson) {
        createdLessons.add(lesson)
    }
    
    /**
     * Creates a sample lesson using existing templates
     * This is a fallback if no lessons folder exists
     */
    private fun createSampleLesson(context: Context): List<Lesson> {
        val templates = TemplateRepository.getTemplates(context)
        if (templates.isEmpty()) {
            return emptyList()
        }
        
        // Use first template and create 4 steps
        val template = templates.first()
        return listOf(
            Lesson(
                id = "1",
                name = "Basic Drawing",
                description = "Learn to draw step by step",
                steps = (1..4).map { stepNum ->
                    LessonStep(
                        stepNumber = stepNum,
                        title = "Step $stepNum",
                        imageAssetPath = template.imageAssetPath // Using same image for all steps for now
                    )
                }
            )
        )
    }
    
    fun getLessonById(context: Context, id: String): Lesson? {
        // Check created lessons first
        val createdLesson = createdLessons.find { it.id == id }
        if (createdLesson != null) {
            return createdLesson
        }
        
        // Then check asset lessons
        return getLessons(context).find { it.id == id }
    }
}

