package com.example.ardrawing

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.camera.view.PreviewView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.ardrawing.LaunchActivity
import com.example.ardrawing.R
import com.example.ardrawing.databinding.ActivityCameraPreviewBinding
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraPreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraPreviewBinding

    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Initialize view binding
        binding = ActivityCameraPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Handle system bars insets
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            val navigationBarHeight = systemBars.bottom
            val density = resources.displayMetrics.density
            
            // Add padding to capture button container
            val captureRing = binding.captureRing
            val layoutParams = captureRing.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            layoutParams.bottomMargin = (32 * density).toInt() + navigationBarHeight
            captureRing.layoutParams = layoutParams
            
            // Add padding to back button for status bar
            val backButton = binding.backButton
            val backButtonParams = backButton.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            backButtonParams.topMargin = (16 * density).toInt() + systemBars.top
            backButton.layoutParams = backButtonParams
            
            insets
        }
        cameraExecutor = Executors.newSingleThreadExecutor()
        // Check camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        // Setup capture button
        binding.captureButton.setOnClickListener {
            takePhoto()
        }

        // Setup back button
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                // Preview
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

                // Image capture
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .setTargetResolution(android.util.Size(3840, 2160)) // 4K resolution for maximum quality
                    .build()

                // Select back camera
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // Bind to lifecycle
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
                Toast.makeText(this, "Failed to start camera", Toast.LENGTH_SHORT).show()
                finish()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "AR_CAPTURE_$timeStamp.jpg"
        val storageDir = File(getExternalFilesDir(null), "AR_Images")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        val outputFile = File(storageDir, imageFileName)

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    Toast.makeText(baseContext, "Photo capture failed", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    lifecycleScope.launch {
                        try {
                            // Load the high-quality bitmap
                            val bitmap = loadHighQualityBitmap(outputFile.absolutePath)
                            if (bitmap != null) {
                                // Set the captured bitmap for the LaunchActivity
                                LaunchActivity.tempCapturedBitmap = bitmap
                                LaunchActivity.currentPhotoPath = outputFile.absolutePath

                                // Return success
                                setResult(RESULT_OK)
                                finish()
                            } else {
                                Toast.makeText(baseContext, "Failed to process image", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing captured image: ${e.message}")
                            Toast.makeText(baseContext, "Error processing image", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        )
    }

    private fun loadHighQualityBitmap(filePath: String): Bitmap? {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                // Load full-size bitmap with optimal settings
                val options = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                    inSampleSize = 1 // Load full resolution
                    inJustDecodeBounds = false
                    // Enable mutable bitmap for processing
                    inMutable = true
                }

                val bitmap = BitmapFactory.decodeFile(filePath, options)
                Log.d(TAG, "Loaded high-quality bitmap: ${bitmap?.width}x${bitmap?.height}")

                bitmap
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading high-quality bitmap: ${e.message}")
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        cameraProvider?.unbindAll()
    }

    companion object {
        private const val TAG = "CameraPreviewActivity"
    }
}