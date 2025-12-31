package com.example.ardrawing

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import arcore.PermissionUtils
import com.example.ardrawing.R


class LaunchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show minimal loading screen while checking permissions
        setContentView(R.layout.activity_loading)

        // Immediately start camera capture flow
        if (PermissionUtils.hasCameraPermission(this)) {
            captureImage()
        } else {
            PermissionUtils.requestCameraPermission(this, REQUEST_CAMERA)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CAMERA) {
            if (PermissionUtils.hasCameraPermission(this)) {
                captureImage()
            } else {
                Toast.makeText(this, "Camera permission required for AR image capture", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    fun captureImage() {
        // Show pre-capture tips
        android.app.AlertDialog.Builder(this)
            .setTitle("📸 Camera Tips")
            .setMessage("• Keep 6-12 inches from object\n" +
                       "• Center the object in frame\n" +
                       "• Ensure good lighting\n" +
                       "• Hold steady and focus\n\n" +
                       "After taking photo, you'll be able to crop the object!")
            .setPositiveButton("📸 Take Photo") { _, _ ->
                captureHighQualityImage()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun captureHighQualityImage() {
        try {
            // Create file to store the high-quality image
            val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
            val imageFileName = "AR_CAPTURE_$timeStamp.jpg"
            val storageDir = java.io.File(getExternalFilesDir(null), "AR_Images")
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }
            val imageFile = java.io.File(storageDir, imageFileName)

            // Save file path for later use
            currentPhotoPath = imageFile.absolutePath

            val photoURI = androidx.core.content.FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                imageFile
            )

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

            // Grant permissions for camera app to write to our file
            val resInfoList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivityForResult(intent, REQUEST_IMAGE)

        } catch (e: Exception) {
            android.util.Log.e("CAMERA_DEBUG", "Error setting up camera: ${e.message}")
            // Fallback to basic camera intent
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, REQUEST_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK) {
            try {
                val rawBitmap = loadHighQualityBitmap()
                if (rawBitmap != null) {
                    android.util.Log.d("CAMERA_DEBUG", "Loaded high-quality bitmap: ${rawBitmap.width}x${rawBitmap.height}")
                    // Store raw bitmap temporarily for cropping
                    tempCapturedBitmap = rawBitmap
                    // Launch cropping activity
                    startCroppingActivity()
                } else {
                    // Fallback to thumbnail from intent data
                    val thumbnailBitmap = data?.getExtras()?.get("data") as Bitmap?
                    if (thumbnailBitmap != null) {
                        android.util.Log.d("CAMERA_DEBUG", "Using thumbnail fallback: ${thumbnailBitmap.width}x${thumbnailBitmap.height}")
                        tempCapturedBitmap = thumbnailBitmap
                        startCroppingActivity()
                        Toast.makeText(this, "Using standard quality (upgrade camera for better quality)", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("CAMERA_DEBUG", "Error processing captured image: ${e.message}")
                Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == REQUEST_CROP && resultCode == RESULT_OK) {
            // Handle cropped image result
            if (croppedBitmap != null) {
                capturedBitmap = enhanceImageForAR(croppedBitmap!!)

                // Analyze image quality and give feedback (dialog will proceed to AR)
                val qualityScore = analyzeImageQuality(capturedBitmap!!)
                showImageQualityFeedback(qualityScore)

                Toast.makeText(this, "Image cropped and enhanced!", Toast.LENGTH_SHORT).show()

                // Clear temp data
                tempCapturedBitmap = null
                croppedBitmap = null
                currentPhotoPath = null
            }
        }
    }

    private fun proceedToARLabels() {
        try {
            android.util.Log.d("LaunchActivity", "proceedToARLabels called, capturedBitmap is null: ${capturedBitmap == null}")
            // Launch the existing LabelActivity for AR tracking
            startActivity(Intent(this, LabelActivity::class.java))
            finish() // Close LaunchActivity so user can't go back to empty screen
        } catch (e: Exception) {
            android.util.Log.e("LaunchActivity", "Error starting AR labels: ${e.message}", e)
            Toast.makeText(this, "Error starting AR labels: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadHighQualityBitmap(): Bitmap? {
        return try {
            currentPhotoPath?.let { path ->
                val file = java.io.File(path)
                if (file.exists()) {
                    // Load full-size bitmap with optimal settings
                    val options = BitmapFactory.Options().apply {
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                        inSampleSize = 1 // Load full resolution
                        inJustDecodeBounds = false
                    }

                    val bitmap = BitmapFactory.decodeFile(path, options)
                    android.util.Log.d("CAMERA_DEBUG", "Loaded bitmap: ${bitmap?.width}x${bitmap?.height}")

                    // Clean up the file after loading
                    try {
                        file.delete()
                    } catch (e: Exception) {
                        android.util.Log.w("CAMERA_DEBUG", "Could not delete temp file: ${e.message}")
                    }

                    bitmap
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("CAMERA_DEBUG", "Error loading high-quality bitmap: ${e.message}")
            null
        }
    }

    private fun startCroppingActivity() {
        val intent = Intent(this, CropActivity::class.java)
        startActivityForResult(intent, REQUEST_CROP)
    }

    private fun analyzeImageQuality(bitmap: Bitmap): ImageQualityScore {
        val width = bitmap.width
        val height = bitmap.height

        // Get pixel data for analysis
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        var totalBrightness = 0
        var contrastScore = 0
        var edgeCount = 0

        // Analyze brightness and contrast
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            val brightness = (r + g + b) / 3
            totalBrightness += brightness
        }

        val avgBrightness = totalBrightness / pixels.size

        // Simple edge detection (contrast between adjacent pixels)
        for (y in 0 until height - 1) {
            for (x in 0 until width - 1) {
                val currentPixel = pixels[y * width + x]
                val rightPixel = pixels[y * width + x + 1]
                val downPixel = pixels[(y + 1) * width + x]

                val currentBrightness = getBrightness(currentPixel)
                val rightBrightness = getBrightness(rightPixel)
                val downBrightness = getBrightness(downPixel)

                contrastScore += Math.abs(currentBrightness - rightBrightness)
                contrastScore += Math.abs(currentBrightness - downBrightness)

                // Count significant edges
                if (Math.abs(currentBrightness - rightBrightness) > 30) edgeCount++
                if (Math.abs(currentBrightness - downBrightness) > 30) edgeCount++
            }
        }

        val avgContrast = contrastScore / (width * height * 2)
        val edgeDensity = edgeCount.toFloat() / (width * height)

        return ImageQualityScore(avgBrightness, avgContrast, edgeDensity, width, height)
    }

    private fun getBrightness(pixel: Int): Int {
        val r = (pixel shr 16) and 0xFF
        val g = (pixel shr 8) and 0xFF
        val b = pixel and 0xFF
        return (r + g + b) / 3
    }

    private fun showImageQualityFeedback(score: ImageQualityScore) {
        val qualityRating = when {
            score.edgeDensity > 0.15 && score.avgContrast > 20 -> "EXCELLENT"
            score.edgeDensity > 0.1 && score.avgContrast > 15 -> "GOOD"
            score.edgeDensity > 0.05 && score.avgContrast > 10 -> "FAIR"
            else -> "POOR"
        }

        val message = when (qualityRating) {
            "EXCELLENT" -> "🎉 Great photo! This should track very well in AR."
            "GOOD" -> "👍 Good photo! Should work well for AR tracking."
            "FAIR" -> "⚠️ Fair photo. May work, but try getting closer or improving lighting."
            "POOR" -> "❌ Poor photo quality. Try taking a clearer photo with better lighting."
            else -> "Unknown quality"
        }

        android.app.AlertDialog.Builder(this)
            .setTitle("📊 Photo Quality: $qualityRating")
            .setMessage("$message\n\n" +
                       "Details:\n" +
                       "• Brightness: ${score.avgBrightness}/255\n" +
                       "• Contrast: ${score.avgContrast}\n" +
                       "• Details: ${(score.edgeDensity * 100).toInt()}%\n" +
                       "• Size: ${score.width}x${score.height}")
            .setPositiveButton("Continue") { _, _ ->
                // Automatically proceed to AR Labels after quality check
                proceedToARLabels()
            }
            .setNeutralButton("Take Again") { _, _ ->
                captureImage()
            }
            .show()
    }

    data class ImageQualityScore(
        val avgBrightness: Int,
        val avgContrast: Int,
        val edgeDensity: Float,
        val width: Int,
        val height: Int
    )

    private fun enhanceImageForAR(originalBitmap: Bitmap): Bitmap {
        try {
            // For AR tracking, we want to preserve the original quality as much as possible
            // The cropping already handles sizing, so we just do minimal enhancement
            val enhancedBitmap = Bitmap.createBitmap(originalBitmap.width, originalBitmap.height, Bitmap.Config.ARGB_8888)

            val canvas = Canvas(enhancedBitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

            // Apply very subtle contrast enhancement to help AR tracking
            // Use conservative values to avoid degrading image quality
            val colorMatrix = ColorMatrix(floatArrayOf(
                1.1f, 0f, 0f, 0f, 2f,   // Red channel - slight contrast
                0f, 1.1f, 0f, 0f, 2f,   // Green channel
                0f, 0f, 1.1f, 0f, 2f,   // Blue channel
                0f, 0f, 0f, 1f, 0f      // Alpha channel
            ))

            paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
            canvas.drawBitmap(originalBitmap, 0f, 0f, paint)

            android.util.Log.d("AR_DEBUG", "Enhanced image: ${enhancedBitmap.width}x${enhancedBitmap.height} (quality preserved)")
            return enhancedBitmap

        } catch (e: Exception) {
            android.util.Log.e("AR_DEBUG", "Error enhancing image: ${e.message}")
            // Return original bitmap if enhancement fails
            return originalBitmap
        }
    }

    private fun applySharpening(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val sharpenedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val sharpenedPixels = IntArray(width * height)

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val idx = y * width + x

                // Simple sharpening kernel
                val center = pixels[idx]
                val top = pixels[(y - 1) * width + x]
                val bottom = pixels[(y + 1) * width + x]
                val left = pixels[y * width + (x - 1)]
                val right = pixels[y * width + (x + 1)]

                // Sharpening formula: center * (1 + 4*amount) - neighbors * amount
                val amount = 0.3f
                val sharpened = center * (1 + 4 * amount) - top * amount - bottom * amount - left * amount - right * amount

                sharpenedPixels[idx] = clampColor(sharpened.toInt())
            }
        }

        // Copy border pixels unchanged
        for (x in 0 until width) {
            sharpenedPixels[x] = pixels[x] // top row
            sharpenedPixels[(height - 1) * width + x] = pixels[(height - 1) * width + x] // bottom row
        }
        for (y in 1 until height - 1) {
            sharpenedPixels[y * width] = pixels[y * width] // left column
            sharpenedPixels[y * width + (width - 1)] = pixels[y * width + (width - 1)] // right column
        }

        sharpenedBitmap.setPixels(sharpenedPixels, 0, width, 0, 0, width, height)
        return sharpenedBitmap
    }

    private fun clampColor(color: Int): Int {
        return color.coerceIn(0, 255)
    }


    companion object {
        const val REQUEST_CAMERA: Int = 100
        const val REQUEST_IMAGE: Int = 101
        const val REQUEST_CROP: Int = 102
        var capturedBitmap: Bitmap? = null
        var tempCapturedBitmap: Bitmap? = null
        var croppedBitmap: Bitmap? = null
        var currentPhotoPath: String? = null
    }
}