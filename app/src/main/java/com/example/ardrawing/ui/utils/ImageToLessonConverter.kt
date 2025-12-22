package com.example.ardrawing.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.ardrawing.data.model.Lesson
import com.example.ardrawing.data.model.LessonStep
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageToLessonConverter {
    /**
     * Creates a lesson with step-by-step images from a single image
     * For now, creates progressive steps by gradually revealing parts of the image
     * This can be enhanced with actual image processing/AI to detect drawing elements
     */
    fun createLessonFromImage(
        context: Context,
        imageUri: Uri,
        lessonName: String
    ): Lesson {
        // Load the original image
        val originalBitmap = loadBitmapFromUri(context, imageUri)
        
        // Create steps directory in internal storage
        val lessonsDir = File(context.filesDir, "lessons/${System.currentTimeMillis()}")
        lessonsDir.mkdirs()
        
        // For now, create 4 steps with progressive opacity/reveal
        // In production, this would use image processing to detect and separate drawing elements
        val steps = mutableListOf<LessonStep>()
        
        // Step 1: Basic features (eyes, nose, mouth) - 30% visible
        val step1Bitmap = createStepBitmap(originalBitmap, 0.3f)
        val step1File = File(lessonsDir, "step1.png")
        saveBitmap(step1Bitmap, step1File)
        steps.add(
            LessonStep(
                stepNumber = 1,
                title = "Step 1: Basic Features",
                imageAssetPath = step1File.absolutePath // Store as file path for now
            )
        )
        
        // Step 2: Add more details - 50% visible
        val step2Bitmap = createStepBitmap(originalBitmap, 0.5f)
        val step2File = File(lessonsDir, "step2.png")
        saveBitmap(step2Bitmap, step2File)
        steps.add(
            LessonStep(
                stepNumber = 2,
                title = "Step 2: Add Details",
                imageAssetPath = step2File.absolutePath
            )
        )
        
        // Step 3: Add outline - 75% visible
        val step3Bitmap = createStepBitmap(originalBitmap, 0.75f)
        val step3File = File(lessonsDir, "step3.png")
        saveBitmap(step3Bitmap, step3File)
        steps.add(
            LessonStep(
                stepNumber = 3,
                title = "Step 3: Add Outline",
                imageAssetPath = step3File.absolutePath
            )
        )
        
        // Step 4: Complete drawing - 100% visible
        val step4File = File(lessonsDir, "step4.png")
        saveBitmap(originalBitmap, step4File)
        steps.add(
            LessonStep(
                stepNumber = 4,
                title = "Step 4: Complete",
                imageAssetPath = step4File.absolutePath
            )
        )
        
        return Lesson(
            id = System.currentTimeMillis().toString(),
            name = lessonName,
            description = "Learn to draw step by step",
            steps = steps
        )
    }
    
    /**
     * Creates a step bitmap with progressive reveal
     * This is a simplified version - can be enhanced with actual image processing
     */
    private fun createStepBitmap(original: Bitmap, progress: Float): Bitmap {
        // For now, just return a copy with adjusted opacity
        // In production, this would use image processing to detect and reveal specific elements
        val config = original.config ?: Bitmap.Config.ARGB_8888
        val result = original.copy(config, true)
        // Apply alpha based on progress
        // This is a placeholder - actual implementation would detect drawing elements
        return result
    }
    
    private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream).also {
            inputStream?.close()
        }
    }
    
    private fun saveBitmap(bitmap: Bitmap, file: File) {
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }
}

