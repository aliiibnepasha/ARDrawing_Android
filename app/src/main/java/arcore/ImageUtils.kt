package com.lowbyte.battery.arexample

import android.graphics.*
import android.media.Image
import com.google.ar.core.ImageFormat

object ImageUtils {

    /**
     * Converts YUV_420_888 image from ARCore camera to RGB Bitmap
     */
    fun yuvToBitmap(image: Image): Bitmap? {
        try {
            if (image.format != ImageFormat.YUV_420_888) {
                android.util.Log.e("ImageUtils", "Unsupported image format: ${image.format}")
                return null
            }

            val width = image.width
            val height = image.height

            // Get YUV planes
            val yPlane = image.planes[0]
            val uPlane = image.planes[1]
            val vPlane = image.planes[2]

            val yBuffer = yPlane.buffer
            val uBuffer = uPlane.buffer
            val vBuffer = vPlane.buffer

            // Create output bitmap
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val pixels = IntArray(width * height)

            // YUV to RGB conversion
            val yRowStride = yPlane.rowStride
            val yPixelStride = yPlane.pixelStride
            val uvRowStride = uPlane.rowStride
            val uvPixelStride = uPlane.pixelStride

            for (y in 0 until height) {
                val yIndex = y * yRowStride
                val uvRowStart = (y / 2) * uvRowStride

                for (x in 0 until width) {
                    val yValue = (yBuffer.get(yIndex + x * yPixelStride).toInt() and 0xFF)
                    val uValue = (uBuffer.get(uvRowStart + (x / 2) * uvPixelStride).toInt() and 0xFF) - 128
                    val vValue = (vBuffer.get(uvRowStart + (x / 2) * uvPixelStride).toInt() and 0xFF) - 128

                    // YUV to RGB conversion formula
                    val r = (yValue + 1.402f * vValue).toInt().coerceIn(0, 255)
                    val g = (yValue - 0.344f * uValue - 0.714f * vValue).toInt().coerceIn(0, 255)
                    val b = (yValue + 1.772f * uValue).toInt().coerceIn(0, 255)

                    pixels[y * width + x] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
                }
            }

            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            return bitmap

        } catch (e: Exception) {
            android.util.Log.e("ImageUtils", "Error converting YUV to Bitmap: ${e.message}")
            return null
        }
    }

    /**
     * Saves bitmap to internal storage and returns file path
     */
    fun saveBitmapToFile(bitmap: Bitmap, context: android.content.Context, filename: String = "captured_${System.currentTimeMillis()}.png"): String? {
        return try {
            val file = java.io.File(context.filesDir, filename)
            java.io.FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            android.util.Log.d("ImageUtils", "Bitmap saved to: ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            android.util.Log.e("ImageUtils", "Error saving bitmap: ${e.message}")
            null
        }
    }

    /**
     * Creates a mutable copy of bitmap for drawing operations
     */
    fun makeMutableBitmap(bitmap: Bitmap): Bitmap {
        return bitmap.copy(Bitmap.Config.ARGB_8888, true)
    }
}
