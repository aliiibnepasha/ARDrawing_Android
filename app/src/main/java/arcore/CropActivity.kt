package com.example.ardrawing

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.yalantis.ucrop.UCrop
import java.io.File

class CropActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CropActivity"
        private const val REQUEST_CROP = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load the captured image
        val bitmap = LaunchActivity.tempCapturedBitmap
        if (bitmap == null) {
            Log.e(TAG, "No bitmap found in LaunchActivity.tempCapturedBitmap")
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        if (bitmap.isRecycled) {
            Log.e(TAG, "Bitmap has been recycled")
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        Log.d(TAG, "Starting UCrop with bitmap: ${bitmap.width}x${bitmap.height}")

        // Save bitmap to temporary file for UCrop
        val sourceFile = saveBitmapToTempFile(bitmap)
        if (sourceFile == null) {
            Log.e(TAG, "Failed to save bitmap to file")
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        // Start UCrop
        startUCrop(sourceFile)
    }

    private fun saveBitmapToTempFile(bitmap: Bitmap): File? {
        return try {
            val timeStamp = System.currentTimeMillis()
            val imageFileName = "AR_CROP_SOURCE_$timeStamp.jpg"
            val storageDir = File(getExternalFilesDir(null), "AR_Crop")
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }
            val imageFile = File(storageDir, imageFileName)

            // Save bitmap to file
            val outputStream = imageFile.outputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
            outputStream.flush()
            outputStream.close()

            Log.d(TAG, "Bitmap saved to: ${imageFile.absolutePath}")
            imageFile
        } catch (e: Exception) {
            Log.e(TAG, "Error saving bitmap to file: ${e.message}", e)
            null
        }
    }

    private fun startUCrop(sourceFile: File) {
        try {
            // Create destination file
            val timeStamp = System.currentTimeMillis()
            val destFileName = "AR_CROP_RESULT_$timeStamp.jpg"
            val storageDir = File(getExternalFilesDir(null), "AR_Crop")
            val destFile = File(storageDir, destFileName)

            // Get URIs using FileProvider
            val sourceUri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                sourceFile
            )
            val destinationUri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                destFile
            )

            // Configure UCrop
            val uCrop = UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1f, 1f) // Square crop for AR
                .withMaxResultSize(1024, 1024) // Max size for AR
                .withOptions(getUCropOptions())

            // Start UCrop activity (uses startActivityForResult internally)
            uCrop.start(this, REQUEST_CROP)

        } catch (e: Exception) {
            Log.e(TAG, "Error starting UCrop: ${e.message}", e)
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun getUCropOptions(): com.yalantis.ucrop.UCrop.Options {
        val options = com.yalantis.ucrop.UCrop.Options()

        // UI Customization
        options.setHideBottomControls(false)
        options.setFreeStyleCropEnabled(true) // Allow free-form cropping
        options.setShowCropFrame(true)
        options.setShowCropGrid(true)
        options.setCropFrameColor(android.graphics.Color.parseColor("#4285F4"))
        options.setCropGridColor(android.graphics.Color.parseColor("#4285F4"))
        options.setCropGridStrokeWidth(2)
        options.setCropFrameStrokeWidth(3)

        // Toolbar
        options.setToolbarTitle("Crop Your AR Object")
        options.setToolbarColor(android.graphics.Color.parseColor("#1C1C1C"))
        options.setStatusBarColor(android.graphics.Color.parseColor("#000000"))
        options.setToolbarWidgetColor(android.graphics.Color.parseColor("#FFFFFF"))
        
        // Compression
        options.setCompressionQuality(95)
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG)
        
        // Max size
        options.setMaxBitmapSize(2048)
        
        return options
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CROP) {
            if (resultCode == RESULT_OK && data != null) {
                val resultUri = UCrop.getOutput(data)
                if (resultUri != null) {
                    processCroppedImage(resultUri)
                } else {
                    Log.e(TAG, "UCrop result URI is null")
                    setResult(RESULT_CANCELED)
                    finish()
                }
            } else if (resultCode == UCrop.RESULT_ERROR && data != null) {
                val cropError = UCrop.getError(data)
                Log.e(TAG, "UCrop error: ${cropError?.message ?: "Unknown error"}")
                setResult(RESULT_CANCELED)
                finish()
            } else {
                Log.d(TAG, "User cancelled cropping")
            setResult(RESULT_CANCELED)
            finish()
            }
        }
    }

    private fun processCroppedImage(resultUri: Uri) {
        try {
            // Load the cropped image
            val inputStream = contentResolver.openInputStream(resultUri)
            val croppedBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (croppedBitmap == null) {
                Log.e(TAG, "Failed to decode cropped image")
                setResult(RESULT_CANCELED)
                finish()
                return
            }

            Log.d(TAG, "Cropped image loaded: ${croppedBitmap.width}x${croppedBitmap.height}")

            // Create square bitmap for AR (ARCore prefers square images)
            val squareSize = maxOf(croppedBitmap.width, croppedBitmap.height)
            val squareBitmap = Bitmap.createBitmap(squareSize, squareSize, Bitmap.Config.ARGB_8888)

            val canvas = android.graphics.Canvas(squareBitmap)
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

            // Center the cropped image in the square
            val left = (squareSize - croppedBitmap.width) / 2f
            val top = (squareSize - croppedBitmap.height) / 2f
            canvas.drawBitmap(croppedBitmap, left, top, paint)

            // Resize to 512x512 for optimal AR processing
            val finalBitmap = Bitmap.createScaledBitmap(squareBitmap, 512, 512, true)

            // Clean up intermediate bitmaps
            croppedBitmap.recycle()
            squareBitmap.recycle()

            // Store the result
            LaunchActivity.croppedBitmap = finalBitmap

            Log.d(TAG, "Final bitmap size: ${finalBitmap.width}x${finalBitmap.height}")

            // Go directly to AR - no preview dialog
            setResult(RESULT_OK)
            finish()

        } catch (e: Exception) {
            Log.e(TAG, "Error processing cropped image: ${e.message}", e)
            setResult(RESULT_CANCELED)
            finish()
        }
    }
}