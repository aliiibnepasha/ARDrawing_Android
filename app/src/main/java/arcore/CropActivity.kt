package com.example.ardrawing

import android.graphics.*
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.ardrawing.R

class CropActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var cropOverlayView: CropOverlayView
    private lateinit var cropButton: Button
    private lateinit var cancelButton: Button

    private var bitmap: Bitmap? = null

    // Crop rectangle
    var cropRect = RectF()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop)

        imageView = findViewById(R.id.imageView)
        cropButton = findViewById(R.id.cropButton)
        cancelButton = findViewById(R.id.cancelButton)

        // Load the captured image
        bitmap = LaunchActivity.tempCapturedBitmap
        if (bitmap == null) {
            finish()
            return
        }

        setupImageView()
        setupButtons()

        // Initialize crop rectangle to center square
        initializeCropRect()
    }

    private fun setupImageView() {
        imageView.setImageBitmap(bitmap)
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER

        cropOverlayView = findViewById(R.id.cropOverlay)
        cropOverlayView.updateCropRectangle(cropRect)
    }

    private fun setupButtons() {
        cropButton.setOnClickListener {
            performCrop()
        }

        cancelButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun initializeCropRect() {
        val viewWidth = cropOverlayView.width.toFloat()
        val viewHeight = cropOverlayView.height.toFloat()

        if (viewWidth > 0 && viewHeight > 0) {
            // Get image display rect to determine proper crop size
            val imageRect = getImageDisplayRect(imageView)

            // Make crop rectangle much bigger - 85% of visible image size
            val size = minOf(imageRect.width(), imageRect.height()) * 0.85f

            // Center the crop rectangle on the actual image position
            val left = imageRect.centerX() - size / 2
            val top = imageRect.centerY() - size / 2
            val right = left + size
            val bottom = top + size

            cropRect.set(left, top, right, bottom)
            cropOverlayView.invalidate()
        }
    }

    /**
     * Force crop rectangle to remain square and within image bounds
     */
    fun constrainCropRect() {
        val imageRect = getImageDisplayRect(imageView)

        // Force square crop
        val currentSize = cropRect.width()
        val centerX = cropRect.centerX()
        val centerY = cropRect.centerY()

        cropRect.left = centerX - currentSize / 2
        cropRect.right = centerX + currentSize / 2
        cropRect.top = centerY - currentSize / 2
        cropRect.bottom = centerY + currentSize / 2

        // Constrain to image area
        cropRect.left = cropRect.left.coerceIn(imageRect.left, imageRect.right - cropRect.width())
        cropRect.top = cropRect.top.coerceIn(imageRect.top, imageRect.bottom - cropRect.height())
        cropRect.right = cropRect.right.coerceAtMost(imageRect.right)
        cropRect.bottom = cropRect.bottom.coerceAtMost(imageRect.bottom)

        cropOverlayView.invalidate()
    }

    /**
     * Get the actual display rectangle of the image within the ImageView (accounts for FIT_CENTER scaling)
     */
    private fun getImageDisplayRect(imageView: ImageView): RectF {
        val drawable = imageView.drawable ?: return RectF()
        val matrix = imageView.imageMatrix
        val rect = RectF(
            0f,
            0f,
            drawable.intrinsicWidth.toFloat(),
            drawable.intrinsicHeight.toFloat()
        )
        matrix.mapRect(rect)
        return rect
    }


    private fun performCrop() {
        val bitmap = bitmap ?: return

        val imageView = findViewById<ImageView>(R.id.imageView)
        val imageRect = getImageDisplayRect(imageView)

        // 🔒 Clamp crop rect inside image area to prevent errors
        val safeCrop = RectF(
            maxOf(cropRect.left, imageRect.left),
            maxOf(cropRect.top, imageRect.top),
            minOf(cropRect.right, imageRect.right),
            minOf(cropRect.bottom, imageRect.bottom)
        )

        if (safeCrop.width() <= 0 || safeCrop.height() <= 0) {
            android.util.Log.e("CROP_DEBUG", "Crop rectangle is outside image area")
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        try {
            // Calculate scale between bitmap & displayed image
            val scaleX = bitmap.width / imageRect.width()
            val scaleY = bitmap.height / imageRect.height()

            // Convert crop rect to bitmap coordinates
            val bitmapX = ((safeCrop.left - imageRect.left) * scaleX).toInt()
                .coerceIn(0, bitmap.width - 1)
            val bitmapY = ((safeCrop.top - imageRect.top) * scaleY).toInt()
                .coerceIn(0, bitmap.height - 1)
            val bitmapW = (safeCrop.width() * scaleX).toInt()
                .coerceAtMost(bitmap.width - bitmapX)
            val bitmapH = (safeCrop.height() * scaleY).toInt()
                .coerceAtMost(bitmap.height - bitmapY)

            android.util.Log.d("CROP_DEBUG", "Image rect: $imageRect")
            android.util.Log.d("CROP_DEBUG", "Safe crop: $safeCrop")
            android.util.Log.d("CROP_DEBUG", "Bitmap crop: $bitmapX, $bitmapY, $bitmapW, $bitmapH")

            // Ensure valid dimensions
            if (bitmapW <= 0 || bitmapH <= 0) {
                android.util.Log.e("CROP_DEBUG", "Invalid crop dimensions")
                setResult(RESULT_CANCELED)
                finish()
                return
            }

            // Crop the bitmap
            val croppedBitmap = Bitmap.createBitmap(bitmap, bitmapX, bitmapY, bitmapW, bitmapH)

            // Create square bitmap for AR (ARCore prefers square images)
            val squareSize = maxOf(croppedBitmap.width, croppedBitmap.height)
            val squareBitmap = Bitmap.createBitmap(squareSize, squareSize, Bitmap.Config.ARGB_8888)

            val canvas = Canvas(squareBitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

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

            android.util.Log.d("CROP_DEBUG", "Final bitmap size: ${finalBitmap.width}x${finalBitmap.height}")

            // Show preview of cropped image
            showCroppedPreview(finalBitmap)

        } catch (e: Exception) {
            android.util.Log.e("CROP_DEBUG", "Error during cropping: ${e.message}", e)
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun showCroppedPreview(croppedBitmap: Bitmap) {
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Cropped Image Preview")
            .setMessage("Does this look correct? This will be used for AR tracking.")
            .create()

        val imageView = android.widget.ImageView(this)
        imageView.setImageBitmap(croppedBitmap)
        imageView.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
        imageView.layoutParams = android.widget.LinearLayout.LayoutParams(
            300, 300
        )

        val layout = android.widget.LinearLayout(this)
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setPadding(32, 16, 32, 16)
        layout.addView(imageView)

        dialog.setView(layout)
        dialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, "Use This Image") { _, _ ->
            setResult(RESULT_OK)
            finish()
        }
        dialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, "Crop Again") { _, _ ->
            // Don't finish, let user try cropping again
        }
        dialog.setCancelable(false)
        dialog.show()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && cropRect.isEmpty) {
            // Initialize crop rect when view is ready
            initializeCropRect()
        }
    }
}
