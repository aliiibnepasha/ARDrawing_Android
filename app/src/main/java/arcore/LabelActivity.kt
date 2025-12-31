package com.example.ardrawing

import android.graphics.*
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.AugmentedImage
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.TrackingState

class LabelActivity : AppCompatActivity() {

    private lateinit var arSession: Session
    private lateinit var glSurfaceView: GLSurfaceView

    // UI Elements
    private lateinit var statusText: TextView
    private lateinit var capturedImageView: ImageView

    // Tracking state management for better continuous tracking
    private var lastTrackingState = TrackingState.STOPPED
    private var trackingStartTime = 0L
    private var framesSinceLastDetection = 0

    // Stability improvements for AR drawing
    private var lastStablePose: com.google.ar.core.Pose? = null
    private var poseStabilityCounter = 0
    private val STABILITY_THRESHOLD = 5 // frames
    private val POSITION_TOLERANCE = 0.02f // meters
    private val ROTATION_TOLERANCE = 0.05f // radians

    // Bounding box display control
    var showBoundingBoxes = true
        private set

    // Anchor-based tracking (professional approach)
    private var anchorCreated = false
    private var trackingAnchor: com.google.ar.core.Anchor? = null

    // AR Components
    private lateinit var labelRenderer: LabelRenderer
    private lateinit var anchorRenderer: AnchorRenderer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Force portrait orientation for AR
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setContentView(R.layout.activity_label)

        // Initialize UI elements
        statusText = findViewById(R.id.statusText)
        capturedImageView = findViewById(R.id.capturedImageView)

        // Display captured image thumbnail
        LaunchActivity.capturedBitmap?.let {
            capturedImageView.setImageBitmap(it)
            capturedImageView.visibility = View.VISIBLE
        }

        // Setup AR
        glSurfaceView = findViewById(R.id.glSurfaceView)
        glSurfaceView.setEGLContextClientVersion(2)

        setupAR()

        // Create renderers
        labelRenderer = LabelRenderer(this, arSession)
        anchorRenderer = AnchorRenderer(this)
        val boundingBoxRenderer = BoundingBoxRenderer(this)
        labelRenderer.setAnchorRenderer(anchorRenderer)
        labelRenderer.setBoundingBoxRenderer(boundingBoxRenderer)

        glSurfaceView.setRenderer(labelRenderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        // Set up double-tap to toggle bounding boxes
        var lastTapTime = 0L
        glSurfaceView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastTapTime < 300) { // Double tap detected
                    showBoundingBoxes = !showBoundingBoxes
                    runOnUiThread {
                        val status = if (showBoundingBoxes) "Bounding boxes: ON" else "Bounding boxes: OFF"
                        Toast.makeText(this@LabelActivity, status, Toast.LENGTH_SHORT).show()
                    }
                }
                lastTapTime = currentTime
            }
            true
        }

        updateStatus("📸 Point camera at captured image")

        // Add professional guidance for optimal AR tracking
        runOnUiThread {
            statusText.text = "📸 Point camera at captured image\n📏 Keep image 30-50cm away & flat\n💡 Ensure good lighting\n👆 Double-tap to toggle bounding boxes"
        }
    }

    private fun setupAR() {
        try {
            android.util.Log.d("AR_DEBUG", "Initializing AR session...")
            arSession = Session(this)

            // Check if we have a captured bitmap for image tracking
            if (LaunchActivity.capturedBitmap == null) {
                android.util.Log.w("AR_DEBUG", "No captured bitmap found for image tracking")
                runOnUiThread {
                    android.app.AlertDialog.Builder(this)
                        .setTitle("No Image Found")
                        .setMessage("No captured image found. Please take a photo first.")
                        .setPositiveButton("Take Photo") { _, _ ->
                            startActivity(android.content.Intent(this, LaunchActivity::class.java))
                            finish()
                        }
                        .setCancelable(false)
                        .show()
                }
                return
            }

            // Create Augmented Image Database for image detection
            val db = AugmentedImageDatabase(arSession)

            val bitmap = LaunchActivity.capturedBitmap
            if (bitmap == null || bitmap.isRecycled) {
                android.util.Log.e("AR_DEBUG", "Bitmap is null or recycled")
                handleGenericARError("Image data is not available")
                return
            }

            // Validate bitmap properties
            if (bitmap.width < 32 || bitmap.height < 32) {
                android.util.Log.e("AR_DEBUG", "Bitmap too small: ${bitmap.width}x${bitmap.height}")
                handleGenericARError("Image is too small for AR tracking")
                return
            }

            // Simple preparation for ARCore (no heavy processing)
            val preparedBitmap = prepareImageForAR(bitmap)

            android.util.Log.d("AR_DEBUG", "Adding captured image to database: ${preparedBitmap.width}x${preparedBitmap.height}")

            // Add the image with PHYSICAL SIZE (crucial for tracking speed!)
            val physicalWidthMeters = 0.20f // Approximate real-world width in meters
            db.addImage("CapturedImage", preparedBitmap, physicalWidthMeters)

            // Configure for stable AR drawing experience
            val config = Config(arSession)
            config.augmentedImageDatabase = db
            config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE

            // Critical settings for stable AR drawing
            config.focusMode = Config.FocusMode.AUTO  // Maintain focus for consistent tracking
            config.planeFindingMode = Config.PlaneFindingMode.DISABLED  // Planes not needed for image tracking

            // Enable depth for better pose estimation (if available)
            config.depthMode = Config.DepthMode.AUTOMATIC

            // Lighting estimation for better tracking stability
            config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR

            arSession.configure(config)

            android.util.Log.d("AR_CONFIG", "AR session configured for stable image tracking")

            android.util.Log.d("AR_DEBUG", "AR setup completed successfully with augmented image database")

        } catch (e: com.google.ar.core.exceptions.ImageInsufficientQualityException) {
            android.util.Log.e("AR_DEBUG", "Image quality exception: ${e.message}")
            handleImageQualityFallback()

        } catch (e: Exception) {
            android.util.Log.e("AR_DEBUG", "General AR exception: ${e.message}")
            handleGenericARError(e.message ?: "Unknown AR error")
        }
    }

    private fun handleImageQualityFallback() {
        try {
            android.util.Log.d("AR_DEBUG", "Trying enhanced image processing...")
            val enhancedBitmap = enhanceImageFurther(LaunchActivity.capturedBitmap!!)
            val db2 = AugmentedImageDatabase(arSession)
            db2.addImage("CapturedImage", enhancedBitmap)

            val config2 = Config(arSession)
            config2.augmentedImageDatabase = db2
            arSession.configure(config2)

            android.util.Log.d("AR_DEBUG", "AR setup completed with enhanced image")
            updateStatus("AR Camera Ready - Using enhanced image")

        } catch (e2: Exception) {
            android.util.Log.e("AR_DEBUG", "Enhanced image also failed: ${e2.message}")
            useDemoImageFallback()
        }
    }

    private fun useDemoImageFallback() {
        try {
            android.util.Log.d("AR_DEBUG", "Using demo image as fallback...")
            val sampleBitmap = createSampleImage()
            val db3 = AugmentedImageDatabase(arSession)
            db3.addImage("SampleImage", sampleBitmap)

            val config3 = Config(arSession)
            config3.augmentedImageDatabase = db3
            arSession.configure(config3)

            android.util.Log.d("AR_DEBUG", "AR setup completed with demo image")
            updateStatus("AR Camera Ready - Demo Mode")

            runOnUiThread {
                android.app.AlertDialog.Builder(this)
                    .setTitle("Using Demo Image")
                    .setMessage("Your captured image couldn't be processed for AR tracking.\n\n" +
                               "We're using a demo image instead so you can see how AR works!\n\n" +
                               "💡 Tip: Try photographing:\n" +
                               "• Books or posters with clear text\n" +
                               "• Objects with distinct patterns\n" +
                               "• Well-lit scenes with good contrast")
                    .setPositiveButton("Got it!") { _, _ ->
                        // Update UI to show we're using demo image
                        updateStatus("AR Camera Ready - Demo Mode")
                    }
                    .setCancelable(false)
                    .show()
            }

        } catch (e3: Exception) {
            android.util.Log.e("AR_DEBUG", "Demo image also failed: ${e3.message}")
            showImageQualityError()
        }
    }

    private fun handleFatalARError() {
        runOnUiThread {
            android.app.AlertDialog.Builder(this)
                .setTitle("AR Not Available")
                .setMessage("AR functionality is not available on this device or there's a system issue.\n\n" +
                           "Please ensure:\n" +
                           "• Your device supports AR\n" +
                           "• Camera permission is granted\n" +
                           "• Google Play Services for AR is installed")
                .setPositiveButton("OK") { _, _ -> finish() }
                .setCancelable(false)
                .show()
        }
    }

    private fun handleGenericARError(message: String) {
        runOnUiThread {
            android.app.AlertDialog.Builder(this)
                .setTitle("AR Setup Error")
                .setMessage("Failed to setup AR tracking: $message\n\nWould you like to try again?")
                .setPositiveButton("Retry") { _, _ -> setupAR() }
                .setNegativeButton("Cancel") { _, _ -> finish() }
                .setCancelable(false)
                .show()
        }
    }

    private fun enhanceImageFurther(originalBitmap: Bitmap): Bitmap {
        try {
            // Create bitmap with maximum size for best quality
            val targetSize = 1024
            val enhancedBitmap = Bitmap.createScaledBitmap(originalBitmap, targetSize, targetSize, true)

            val canvas = Canvas(enhancedBitmap)
            val paint = Paint()

            // Apply very aggressive contrast and brightness enhancement
            val colorMatrix = ColorMatrix(floatArrayOf(
                2.0f, 0f, 0f, 0f, 20f,   // Red channel - high contrast with brightness boost
                0f, 2.0f, 0f, 0f, 20f,   // Green channel
                0f, 0f, 2.0f, 0f, 20f,   // Blue channel
                0f, 0f, 0f, 1f, 0f       // Alpha channel
            ))

            paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
            canvas.drawBitmap(enhancedBitmap, 0f, 0f, paint)

            // Apply stronger sharpening
            return applyStrongSharpening(enhancedBitmap)

        } catch (e: Exception) {
            android.util.Log.e("AR_DEBUG", "Error in further enhancement: ${e.message}")
            return originalBitmap
        }
    }

    private fun applyStrongSharpening(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val sharpenedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val sharpenedPixels = IntArray(width * height)

        // Stronger sharpening kernel
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val idx = y * width + x

                val center = pixels[idx]
                val top = pixels[(y - 1) * width + x]
                val bottom = pixels[(y + 1) * width + x]
                val left = pixels[y * width + (x - 1)]
                val right = pixels[y * width + (x + 1)]
                val topLeft = pixels[(y - 1) * width + (x - 1)]
                val topRight = pixels[(y - 1) * width + (x + 1)]
                val bottomLeft = pixels[(y + 1) * width + (x - 1)]
                val bottomRight = pixels[(y + 1) * width + (x + 1)]

                // Stronger sharpening formula
                val sharpened = center * 5 - top - bottom - left - right - topLeft - topRight - bottomLeft - bottomRight
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

    private fun createSampleImage(): Bitmap {
        // Create a 512x512 bitmap with strong features for AR tracking
        val size = 512
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        // Fill with white background
        canvas.drawColor(android.graphics.Color.WHITE)

        // Draw a black border
        paint.color = android.graphics.Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 8f
        canvas.drawRect(10f, 10f, size - 10f, size - 10f, paint)

        // Draw some geometric shapes for feature detection
        paint.style = Paint.Style.FILL
        canvas.drawCircle(size / 2f, size / 2f, 100f, paint)

        // Draw some lines and text
        paint.strokeWidth = 4f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(50f, 50f, size - 50f, size - 50f, paint)
        canvas.drawLine(size - 50f, 50f, 50f, size - 50f, paint)

        // Draw text
        paint.style = Paint.Style.FILL
        paint.textSize = 48f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("AR TEST", size / 2f, size / 2f - 120f, paint)
        canvas.drawText("SAMPLE", size / 2f, size / 2f + 120f, paint)

        // Draw some smaller shapes
        canvas.drawRect(100f, 100f, 150f, 150f, paint)
        canvas.drawRect(size - 150f, size - 150f, size - 100f, size - 100f, paint)

        return bitmap
    }

    private fun showImageQualityError() {
        runOnUiThread {
            android.app.AlertDialog.Builder(this)
                .setTitle("Image Quality Issue")
                .setMessage("The captured image doesn't have enough detail for AR tracking. For best results:\n\n" +
                           "• Use well-lit environments\n" +
                           "• Take photos of objects with clear textures and patterns\n" +
                           "• Avoid blurry or low-contrast images\n" +
                           "• Try taking a photo of a book, poster, or textured object\n\n" +
                           "Would you like to take a new photo?")
                .setPositiveButton("Take New Photo") { _, _ ->
                    startActivity(android.content.Intent(this, LaunchActivity::class.java))
                    finish()
                }
                .setNegativeButton("Cancel") { _, _ ->
                    finish()
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun showGenericError(message: String) {
        runOnUiThread {
            android.app.AlertDialog.Builder(this)
                .setTitle("AR Setup Error")
                .setMessage("Failed to setup AR tracking: $message")
                .setPositiveButton("OK") { _, _ ->
                    finish()
                }
                .setCancelable(false)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        arSession.resume()
        glSurfaceView.onResume()
        glSurfaceView.requestLayout()
    }

    override fun onPause() {
        super.onPause()
        try {
            glSurfaceView.onPause()
            arSession.pause()
        } catch (e: Exception) {
            android.util.Log.e("AR_DEBUG", "Error pausing AR session: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            arSession.close()
        } catch (e: Exception) {
            android.util.Log.e("AR_DEBUG", "Error closing AR session: ${e.message}")
        }
    }

    fun updateARInfo(augmentedImage: AugmentedImage) {
        when (augmentedImage.trackingState) {
            TrackingState.TRACKING -> {
                // Professional approach: Create anchor on first stable detection
                if (!anchorCreated && isTrackingStable(augmentedImage)) {
                    createTrackingAnchor(augmentedImage)
                }

                // Log tracking information
                val imageName = augmentedImage.name
                val imageSize = "${"%.2f".format(augmentedImage.extentX)} x ${"%.2f".format(augmentedImage.extentZ)} meters"

                if (anchorCreated) {
                    // Using stable anchor tracking
                    trackingAnchor?.let { anchor ->
                        val position = "(${"%.2f".format(anchor.pose.tx())}, ${"%.2f".format(anchor.pose.ty())}, ${"%.2f".format(anchor.pose.tz())})"
                        android.util.Log.d("AR_TRACKING", "ANCHOR TRACK ✅ | Name: $imageName | Size: $imageSize | Position: $position")
                    }
                } else {
                    // Still using image tracking - show stabilization progress
                    val progress = (poseStabilityCounter.toFloat() / STABILITY_THRESHOLD * 100).toInt()
                    val position = "(${"%.2f".format(augmentedImage.centerPose.tx())}, ${"%.2f".format(augmentedImage.centerPose.ty())}, ${"%.2f".format(augmentedImage.centerPose.tz())})"
                    android.util.Log.d("AR_TRACKING", "STABILIZING 🔄 ${progress}% | Name: $imageName | Size: $imageSize | Position: $position")
                }

                // Add tracking quality indicator
                val trackingQuality = when (augmentedImage.trackingMethod) {
                    AugmentedImage.TrackingMethod.FULL_TRACKING -> "High"
                    AugmentedImage.TrackingMethod.LAST_KNOWN_POSE -> "Medium"
                    else -> "Low"
                }
                android.util.Log.d("AR_TRACKING", "Tracking Quality: $trackingQuality")

            }
            TrackingState.PAUSED -> {
                android.util.Log.d("AR_TRACKING", "Status: PAUSED ⏸️")
            }
            TrackingState.STOPPED -> {
                android.util.Log.d("AR_TRACKING", "Status: LOST ❌")

                // Reset stability tracking when completely lost
                lastStablePose = null
                poseStabilityCounter = 0
                // Note: Keep anchorCreated = true to maintain anchor-based tracking
            }
        }
    }

    /**
     * Check if the current pose is stable enough for AR drawing
     */
    private fun isPoseStable(currentPose: com.google.ar.core.Pose): Boolean {
        if (lastStablePose == null) {
            // First pose - establish baseline
            lastStablePose = currentPose
            poseStabilityCounter = 1
            return false // Not stable yet
        }

        // Calculate pose difference
        val positionDiff = calculatePositionDifference(lastStablePose!!, currentPose)
        val rotationDiff = calculateRotationDifference(lastStablePose!!, currentPose)

        val isPositionStable = positionDiff < POSITION_TOLERANCE
        val isRotationStable = rotationDiff < ROTATION_TOLERANCE

        if (isPositionStable && isRotationStable) {
            // Pose is stable - update counter
            poseStabilityCounter++
            if (poseStabilityCounter >= STABILITY_THRESHOLD) {
                // Fully stable - update baseline
                lastStablePose = currentPose
                return true
            }
        } else {
            // Pose changed - reset counter
            poseStabilityCounter = 0
        }

        return false
    }

    private fun calculatePositionDifference(pose1: com.google.ar.core.Pose, pose2: com.google.ar.core.Pose): Float {
        val dx = pose1.tx() - pose2.tx()
        val dy = pose1.ty() - pose2.ty()
        val dz = pose1.tz() - pose2.tz()
        return Math.sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()
    }

    private fun calculateRotationDifference(pose1: com.google.ar.core.Pose, pose2: com.google.ar.core.Pose): Float {
        // Simplified rotation difference using quaternion dot product
        val q1 = pose1.rotationQuaternion
        val q2 = pose2.rotationQuaternion

        val dotProduct = q1[0] * q2[0] + q1[1] * q2[1] + q1[2] * q2[2] + q1[3] * q2[3]

        // Ensure dot product is within valid range for acos and work with consistent Float types
        val clampedDotProduct = dotProduct.coerceIn(-1.0f, 1.0f)

        // Convert to Double for Math.acos, then back to Float
        return Math.acos(clampedDotProduct.toDouble()).toFloat()
    }

    /**
     * Prepare image for ARCore with minimal processing (no heavy enhancement)
     */
    private fun prepareImageForAR(bitmap: Bitmap): Bitmap {
        // ARCore works best with natural images - avoid over-processing
        val targetSize = 640 // Optimal size for ARCore
        val resized = Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, true)

        // Ensure correct format
        return if (resized.config != Bitmap.Config.ARGB_8888) {
            resized.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            resized
        }
    }

    /**
     * Check if tracking is stable enough to update UI (prevents blinking)
     */
    private fun isTrackingStable(image: AugmentedImage): Boolean {
        // Only show updates for high-confidence tracking
        return image.trackingMethod == AugmentedImage.TrackingMethod.FULL_TRACKING &&
               poseStabilityCounter >= STABILITY_THRESHOLD
    }

    /**
     * Create a stable anchor for tracking (professional approach)
     */
    private fun createTrackingAnchor(image: AugmentedImage) {
        try {
            trackingAnchor = image.createAnchor(image.centerPose)
            anchorCreated = true

            android.util.Log.d("AR_TRACKING", "Created stable tracking anchor - switching to anchor-based tracking")

            // Visual feedback for anchor creation
            runOnUiThread {
                Toast.makeText(this@LabelActivity, "🎯 Anchor locked - stable tracking!", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            android.util.Log.e("AR_TRACKING", "Failed to create tracking anchor: ${e.message}")
        }
    }

    private fun updateStatus(message: String) {
        statusText.text = message
    }


    fun update(frame: Frame) {
        try {
            var hasTrackingImage = false
            var hasPausedImage = false
            var hasStoppedImage = false
            var currentTrackingState = TrackingState.STOPPED

            // Check for augmented images with stability filtering
            for (image in frame.getUpdatedTrackables(AugmentedImage::class.java)) {
                when (image.trackingState) {
                    TrackingState.TRACKING -> {
                        hasTrackingImage = true
                        currentTrackingState = TrackingState.TRACKING

                        // Only update UI when tracking is stable to prevent blinking
                        if (isTrackingStable(image)) {
                            updateARInfo(image)
                        }
                    }
                    TrackingState.PAUSED -> {
                        hasPausedImage = true
                        if (currentTrackingState != TrackingState.TRACKING) {
                            currentTrackingState = TrackingState.PAUSED
                        }
                        // Show paused state immediately for responsiveness
                        updateARInfo(image)
                    }
                    TrackingState.STOPPED -> {
                        hasStoppedImage = true
                        currentTrackingState = TrackingState.STOPPED
                        // Show lost state immediately
                        updateARInfo(image)
                    }
                }
            }

            // Track frames since last detection for better feedback
            if (hasTrackingImage || hasPausedImage) {
                framesSinceLastDetection = 0
                if (lastTrackingState != TrackingState.TRACKING && currentTrackingState == TrackingState.TRACKING) {
                    trackingStartTime = System.currentTimeMillis()
                    // Reset stability tracking for fresh session (but keep anchor if it exists)
                    if (!anchorCreated) {
                        poseStabilityCounter = 0
                        lastStablePose = null
                    }
                }
            } else {
                framesSinceLastDetection++
                // Reset stability when tracking is completely lost for extended period
                if (framesSinceLastDetection > 90) { // ~3 seconds at 30fps
                    poseStabilityCounter = 0
                    lastStablePose = null
                    // Don't reset anchorCreated - anchor should persist for recovery
                }
            }

            lastTrackingState = currentTrackingState

            // Provide continuous feedback based on overall tracking state
            runOnUiThread {
                when {
                    hasTrackingImage -> {
                        // Check if anchor has been created for stable tracking
                        if (anchorCreated) {
                            val trackingDuration = (System.currentTimeMillis() - trackingStartTime) / 1000
                            statusText.text = "🎯 Anchor locked - stable tracking! (${trackingDuration}s)"
                            statusText.setTextColor(resources.getColor(android.R.color.holo_green_dark))
                        } else if (poseStabilityCounter >= STABILITY_THRESHOLD) {
                            statusText.text = "🔄 Creating anchor..."
                            statusText.setTextColor(resources.getColor(android.R.color.holo_blue_light))
                        } else {
                            val progress = (poseStabilityCounter.toFloat() / STABILITY_THRESHOLD * 100).toInt()
                            statusText.text = "🔄 Stabilizing... ${progress}%"
                            statusText.setTextColor(resources.getColor(android.R.color.holo_blue_light))
                        }
                    }
                    hasPausedImage -> {
                        // Images detected but tracking paused - guide user
                        statusText.text = "⏸️ Hold camera steady"
                        statusText.setTextColor(resources.getColor(android.R.color.holo_orange_light))
                    }

                    hasStoppedImage -> {
                        // Tracking lost - help user regain it
                        if (framesSinceLastDetection < 30) {
                            statusText.text = "📱 Reposition camera slowly"
                        } else {
                            statusText.text = "🔍 Locate your image again"
                        }
                        statusText.setTextColor(resources.getColor(android.R.color.holo_red_light))
                    }
                    else -> {
                        // No images detected at all
                        statusText.text = "📸 Point camera at captured image"
                        statusText.setTextColor(resources.getColor(android.R.color.holo_blue_light))
                    }
                }
            }

        } catch (e: Exception) {
            android.util.Log.e("AR_DEBUG", "Error in update: ${e.message}")
            runOnUiThread {
                statusText.text = "⚠️ Tracking error - try restarting"
                statusText.setTextColor(resources.getColor(android.R.color.holo_red_light))
            }
        }
    }

}
