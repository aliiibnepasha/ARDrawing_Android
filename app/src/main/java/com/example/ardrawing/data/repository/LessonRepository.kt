package com.example.ardrawing.data.repository

import android.content.Context
import com.example.ardrawing.data.model.Lesson
import com.example.ardrawing.data.model.LessonStep
import com.example.ardrawing.data.utils.AssetUtils

object LessonRepository {
    // Default subfolder name, but we also support top-level folders like "lesson_1"
    private const val LESSONS_FOLDER = "lessons"
    private val createdLessons = mutableListOf<Lesson>()
    
    /**
     * Gets all available lessons (from assets and created lessons)
     */
    fun getLessons(context: Context): List<Lesson> {
        val lessons = mutableListOf<Lesson>()
        
        // Add created lessons (from images)
        lessons.addAll(createdLessons)
        
        // Discover lesson folders in assets.
        // 1) Prefer subfolders under "lessons" if that directory exists.
        // 2) Otherwise, look for top-level folders like "lesson_1", "lesson_face", etc.
        val assetManager = context.assets
        
        val nestedLessonFolders = try {
            assetManager.list(LESSONS_FOLDER)?.toList().orEmpty()
        } catch (e: Exception) {
            emptyList()
        }
        
        val lessonFolderPaths: List<String> = if (nestedLessonFolders.isNotEmpty()) {
            // Use "lessons/<folderName>"
            nestedLessonFolders.map { "$LESSONS_FOLDER/$it" }
        } else {
            // Fallback: look for top-level folders starting with "lesson"
            val rootEntries = try {
                assetManager.list("")?.toList().orEmpty()
            } catch (e: Exception) {
                emptyList()
            }
            
            rootEntries.filter { name ->
                // Treat as a lesson folder if it starts with "lesson" and actually contains files
                name.startsWith("lesson", ignoreCase = true) &&
                    (assetManager.list(name)?.isNotEmpty() == true)
            }
        }
        
        // Load lessons from discovered folders
        val assetLessons = lessonFolderPaths.mapIndexed { index, folderPath ->
            val stepImages = AssetUtils.listImageFiles(context, folderPath)
                .sortedWith(::naturalOrderByNumbers) // ensure 1,2,3... order for numbered files
            
            val folderName = folderPath.substringAfterLast("/")
            
            Lesson(
                id = folderName,
                name = folderName
                    .replace("_", " ")
                    .replaceFirstChar { it.uppercaseChar() },
                description = "Learn to draw step by step",
                steps = stepImages.mapIndexed { stepIndex, imageName ->
                    LessonStep(
                        stepNumber = stepIndex + 1,
                        title = "Step ${stepIndex + 1}",
                        imageAssetPath = AssetUtils.getAssetPath(folderPath, imageName)
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
     * Natural sort that respects numeric order in filenames
     * e.g. step1.png, step2.png, step10.png
     */
    private fun naturalOrderByNumbers(a: String, b: String): Int {
        val regex = "\\d+".toRegex()
        val aMatch = regex.find(a)?.value?.toIntOrNull()
        val bMatch = regex.find(b)?.value?.toIntOrNull()

        return when {
            aMatch != null && bMatch != null && aMatch != bMatch -> aMatch.compareTo(bMatch)
            else -> a.compareTo(b, ignoreCase = true)
        }
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

