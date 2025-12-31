package com.example.ardrawing

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.google.ar.core.Session
import com.example.ardrawing.AnchorRenderer
import com.example.ardrawing.BackgroundRenderer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class LabelRenderer(
    private val activity: LabelActivity,
    private val session: Session
) : GLSurfaceView.Renderer {

    private val backgroundRenderer = BackgroundRenderer()
    private lateinit var anchorRenderer: AnchorRenderer
    private lateinit var boundingBoxRenderer: BoundingBoxRenderer

    private var viewportWidth = 0
    private var viewportHeight = 0
    private var viewportChanged = false

    // Camera matrices for anchor rendering
    private val viewMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)

    fun setAnchorRenderer(renderer: AnchorRenderer) {
        anchorRenderer = renderer
    }

    fun setBoundingBoxRenderer(renderer: BoundingBoxRenderer) {
        boundingBoxRenderer = renderer
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        backgroundRenderer.createOnGlThread()
        session.setCameraTextureName(backgroundRenderer.textureId)

        // Initialize renderers
        anchorRenderer.createOnGlThread()
        boundingBoxRenderer.createOnGlThread()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        viewportWidth = width
        viewportHeight = height
        viewportChanged = true
        GLES20.glViewport(0, 0, width, height)

        android.util.Log.d("LABEL_RENDERER", "Surface changed: ${width}x${height}")
    }

    override fun onDrawFrame(gl: GL10?) {
        if (viewportWidth == 0 || viewportHeight == 0) return

        // If size/rotation changed, update ARCore display geometry
        if (viewportChanged) {
            // Get actual device rotation for correct orientation
            val display = activity.windowManager.defaultDisplay
            val rotation = display.rotation

            // For portrait mode, we need to handle dimensions correctly
            // ARCore expects width and height to match the display, not necessarily viewport
            val displayMetrics = android.util.DisplayMetrics()
            display.getRealMetrics(displayMetrics)

            android.util.Log.d("LABEL_RENDERER", "Setting display geometry: rotation=$rotation (${getRotationString(rotation)}), display=${displayMetrics.widthPixels}x${displayMetrics.heightPixels}, viewport=${viewportWidth}x${viewportHeight}")

            // Set display geometry with correct rotation and display dimensions
            session.setDisplayGeometry(rotation, displayMetrics.widthPixels, displayMetrics.heightPixels)
            viewportChanged = false
        }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        try {
            // Update AR session (only called here, once per frame)
            val frame = session.update()

            // Update camera matrices for anchor rendering (if needed)
            frame.camera.getViewMatrix(viewMatrix, 0)
            frame.camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 100.0f)

            // Update texture coordinates for proper camera orientation
            backgroundRenderer.updateTexCoords(frame)

            // Update activity with frame data
            activity.update(frame)

            // Draw camera background
            backgroundRenderer.draw()

            // Draw AR anchors with textures (if any)
            if (::anchorRenderer.isInitialized) {
                anchorRenderer.draw(viewMatrix, projectionMatrix)
            }

            // Draw bounding boxes around detected images (if enabled)
            if (::boundingBoxRenderer.isInitialized && activity.showBoundingBoxes) {
                boundingBoxRenderer.draw(frame, viewMatrix, projectionMatrix)
            }

        } catch (e: Exception) {
            android.util.Log.e("LABEL_RENDERER", "Error in onDrawFrame: ${e.message}")
        }
    }

    private fun getRotationString(rotation: Int): String {
        return when (rotation) {
            android.view.Surface.ROTATION_0 -> "0° (Portrait)"
            android.view.Surface.ROTATION_90 -> "90° (Landscape Left)"
            android.view.Surface.ROTATION_180 -> "180° (Portrait Upside Down)"
            android.view.Surface.ROTATION_270 -> "270° (Landscape Right)"
            else -> "Unknown ($rotation)"
        }
    }
}
