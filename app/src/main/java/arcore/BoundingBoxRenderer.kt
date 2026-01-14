package com.example.ardrawing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import com.google.ar.core.AugmentedImage
import com.google.ar.core.Frame
import com.google.ar.core.TrackingState
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
class BoundingBoxRenderer(private val context: Context, private val userBitmap: Bitmap? = null) {

    private var boundingBoxProgram = 0
    private var positionParam = 0
    private var colorParam = 0
    private var mvpMatrixParam = 0

    // Texture rendering for animal image
    private var textureProgram = 0
    private var texturePositionParam = 0
    private var textureCoordParam = 0
    private var textureMvpMatrixParam = 0
    private var textureSamplerParam = 0
    private var animalTextureId = -1

    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var indexBuffer: java.nio.ShortBuffer
    private lateinit var textureQuadBuffer: FloatBuffer
    private lateinit var textureCoordBuffer: FloatBuffer

    // Bounding box vertices (3D wireframe rectangle)
    private val boundingBoxVertices = floatArrayOf(
        // Bottom face (on the ground)
        -0.5f, 0.0f, -0.5f,  // bottom-left-back
        0.5f, 0.0f, -0.5f,   // bottom-right-back
        0.5f, 0.0f, 0.5f,    // bottom-right-front
        -0.5f, 0.0f, 0.5f,   // bottom-left-front

        // Top face (elevated for 3D effect)
        -0.5f, 0.02f, -0.5f, // top-left-back
        0.5f, 0.02f, -0.5f,  // top-right-back
        0.5f, 0.02f, 0.5f,   // top-right-front
        -0.5f, 0.02f, 0.5f   // top-left-front
    )

    // Texture quad vertices (on the image plane, inside edges)
    private val textureQuadVertices = floatArrayOf(
        -0.5f, 0.0f, -0.5f,  // bottom-left
        0.5f, 0.0f, -0.5f,   // bottom-right
        0.5f, 0.0f,  0.5f,   // top-right
        -0.5f, 0.0f,  0.5f   // top-left
    )

    // Texture coordinates (flipped for icons to match OpenGL coordinate system)
    private val textureCoords = floatArrayOf(
        0.0f, 0.0f,  // bottom-left
        1.0f, 0.0f,  // bottom-right
        1.0f, 1.0f,  // top-right
        0.0f, 1.0f   // top-left
    )

    // Indices for drawing lines (wireframe rectangle)
    private val boundingBoxIndices = shortArrayOf(
        // Bottom face edges
        0, 1, 1, 2, 2, 3, 3, 0,
        // Top face edges
        4, 5, 5, 6, 6, 7, 7, 4,
        // Vertical edges
        0, 4, 1, 5, 2, 6, 3, 7
    )

    // Indices for drawing filled surface (bottom face as quad)
    private val surfaceIndices = shortArrayOf(
        0, 1, 2,  // First triangle
        0, 2, 3   // Second triangle
    )
    
    private lateinit var surfaceIndexBuffer: java.nio.ShortBuffer

    fun createOnGlThread() {
        // Create wireframe program
        val wireframeVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER)
        val wireframeFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)

        boundingBoxProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(boundingBoxProgram, wireframeVertexShader)
        GLES20.glAttachShader(boundingBoxProgram, wireframeFragmentShader)
        GLES20.glLinkProgram(boundingBoxProgram)

        positionParam = GLES20.glGetAttribLocation(boundingBoxProgram, "a_Position")
        colorParam = GLES20.glGetUniformLocation(boundingBoxProgram, "u_Color")
        mvpMatrixParam = GLES20.glGetUniformLocation(boundingBoxProgram, "u_MVPMatrix")

        // Create texture program
        val textureVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, TEXTURE_VERTEX_SHADER)
        val textureFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, TEXTURE_FRAGMENT_SHADER)

        textureProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(textureProgram, textureVertexShader)
        GLES20.glAttachShader(textureProgram, textureFragmentShader)
        GLES20.glLinkProgram(textureProgram)

        texturePositionParam = GLES20.glGetAttribLocation(textureProgram, "a_Position")
        textureCoordParam = GLES20.glGetAttribLocation(textureProgram, "a_TexCoord")
        textureMvpMatrixParam = GLES20.glGetUniformLocation(textureProgram, "u_MVPMatrix")
        textureSamplerParam = GLES20.glGetUniformLocation(textureProgram, "u_Texture")

        android.util.Log.d("BoundingBoxRenderer", "Shader programs created successfully")

        // Create buffers
        vertexBuffer = ByteBuffer.allocateDirect(boundingBoxVertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(boundingBoxVertices)
                position(0)
            }

        indexBuffer = ByteBuffer.allocateDirect(boundingBoxIndices.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .apply {
                put(boundingBoxIndices)
                position(0)
            }

        surfaceIndexBuffer = ByteBuffer.allocateDirect(surfaceIndices.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .apply {
                put(surfaceIndices)
                position(0)
            }

        textureQuadBuffer = ByteBuffer.allocateDirect(textureQuadVertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(textureQuadVertices)
                position(0)
            }

        textureCoordBuffer = ByteBuffer.allocateDirect(textureCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(textureCoords)
                position(0)
            }

        // Load icon texture
        loadIconTexture()
    }

    private fun loadIconTexture() {
        val loadStartTime = System.currentTimeMillis()
        android.util.Log.d("BoundingBoxRenderer", "Starting texture load...")

        try {
            // Use user-selected bitmap if provided, otherwise load from assets
            val bitmap = if (userBitmap != null && !userBitmap.isRecycled) {
                Log.d("BoundingBoxRenderer", "Using user-selected bitmap: ${userBitmap.width}x${userBitmap.height}")
                userBitmap
            } else {
                // Fallback to static asset image
                val assetManager = context.assets
                val inputStream = assetManager.open("home/Animals/Animal_1.png")
                val assetBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                val decodeTime = System.currentTimeMillis() - loadStartTime
                Log.d("BoundingBoxRenderer", "Bitmap decode time: ${decodeTime}ms")
                
                if (assetBitmap == null) {
                    android.util.Log.e("BoundingBoxRenderer", "Failed to load Animal_1.png from assets")
                    return
                }
                assetBitmap
            }

            if (bitmap == null) {
                Log.e("BoundingBoxRenderer", "No bitmap available")
                return
            }

            // Generate texture
            val textures = IntArray(1)
            GLES20.glGenTextures(1, textures, 0)
            animalTextureId = textures[0]

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, animalTextureId)

            // Set texture parameters
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

            // Load bitmap into texture
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

            // Check for OpenGL errors
            val error = GLES20.glGetError()
            val totalLoadTime = System.currentTimeMillis() - loadStartTime

            if (error != GLES20.GL_NO_ERROR) {
                Log.e("BoundingBoxRenderer", "OpenGL error loading texture: $error (total time: ${totalLoadTime}ms)")
            } else {
                val source = if (userBitmap != null && bitmap == userBitmap) "user-selected" else "asset (Animal_1.png)"
                Log.d("BoundingBoxRenderer", "Successfully loaded $source texture - texId=$animalTextureId size=${bitmap.width}x${bitmap.height} (total time: ${totalLoadTime}ms)")
            }

            // Only recycle if it's not the user's bitmap (we don't own user's bitmap)
            if (userBitmap == null || bitmap != userBitmap) {
                bitmap.recycle()
            }

        } catch (e: Exception) {
            val totalLoadTime = System.currentTimeMillis() - loadStartTime
            Log.e("BoundingBoxRenderer", "Error loading icon texture: ${e.message} (total time: ${totalLoadTime}ms)")
        }
    }

    fun draw(frame: Frame, viewMatrix: FloatArray, projectionMatrix: FloatArray) {
        if (boundingBoxProgram == 0 || textureProgram == 0) return

        val startTime = System.currentTimeMillis()

        // Draw bounding boxes and textures for each tracked image
        for (image in frame.getUpdatedTrackables(AugmentedImage::class.java)) {
            if (image.trackingState == TrackingState.TRACKING) {
                val imageStartTime = System.currentTimeMillis()
                drawBoundingBoxAndTexture(image, viewMatrix, projectionMatrix)
                val imageDrawTime = System.currentTimeMillis() - imageStartTime

                // Log if first draw takes more than 50ms (potential performance issue)
                if (imageDrawTime > 50) {
                    android.util.Log.w("BoundingBoxRenderer", "Slow first draw: ${imageDrawTime}ms for image: ${image.name}")
                }
            }
        }

        val totalDrawTime = System.currentTimeMillis() - startTime
        if (totalDrawTime > 16) { // More than one frame at 60fps
            android.util.Log.w("BoundingBoxRenderer", "Frame draw time: ${totalDrawTime}ms (target: <16ms)")
        }
    }

    private fun drawBoundingBoxAndTexture(image: AugmentedImage, viewMatrix: FloatArray, projectionMatrix: FloatArray) {
        // Calculate the model matrix for this image
        val modelMatrix = FloatArray(16)
        image.centerPose.toMatrix(modelMatrix, 0)

        // Scale the bounding box to match the image size
        val scaleX = image.extentX
        val scaleZ = image.extentZ
        android.opengl.Matrix.scaleM(modelMatrix, 0, scaleX, 1.0f, scaleZ)

        // Calculate MVP matrix
        val mvpMatrix = FloatArray(16)
        val mvMatrix = FloatArray(16)

        android.opengl.Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        android.opengl.Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)

        // Draw the image inside the bounding box
        drawAnimalTextureScaled(image, viewMatrix, projectionMatrix)
        
        // Draw wireframe edges to show the anchor area
        drawWireframeBoundingBox(mvpMatrix, image)
    }

    private fun drawAnimalTexture(mvpMatrix: FloatArray) {
        GLES20.glUseProgram(textureProgram)

        // Enable blending for texture transparency
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        // Disable depth test and cull face to prevent hiding
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glDisable(GLES20.GL_CULL_FACE)

        // Bind texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, animalTextureId)
        GLES20.glUniform1i(textureSamplerParam, 0)

        // Set MVP matrix
        GLES20.glUniformMatrix4fv(textureMvpMatrixParam, 1, false, mvpMatrix, 0)

        // Enable vertex arrays
        GLES20.glEnableVertexAttribArray(texturePositionParam)
        GLES20.glEnableVertexAttribArray(textureCoordParam)

        // Set vertex data
        textureQuadBuffer.position(0)
        GLES20.glVertexAttribPointer(texturePositionParam, 3, GLES20.GL_FLOAT, false, 0, textureQuadBuffer)

        textureCoordBuffer.position(0)
        GLES20.glVertexAttribPointer(textureCoordParam, 2, GLES20.GL_FLOAT, false, 0, textureCoordBuffer)

        // Draw the texture quad
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)

        // Disable vertex arrays
        GLES20.glDisableVertexAttribArray(texturePositionParam)
        GLES20.glDisableVertexAttribArray(textureCoordParam)

        // Restore depth test and disable blending
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDisable(GLES20.GL_BLEND)
    }

    private fun drawAnimalTextureScaled(image: AugmentedImage, viewMatrix: FloatArray, projectionMatrix: FloatArray) {
        val modelMatrix = FloatArray(16)
        image.centerPose.toMatrix(modelMatrix, 0)

        // ✅ Perfect size = real image size (no over-scaling)
        android.opengl.Matrix.scaleM(
            modelMatrix, 0,
            image.extentX, 1.0f, image.extentZ
        )

        val mvMatrix = FloatArray(16)
        val mvpMatrix = FloatArray(16)

        android.opengl.Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        android.opengl.Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)

        drawAnimalTexture(mvpMatrix)
    }

    private fun drawFilledSurface(mvpMatrix: FloatArray, image: AugmentedImage) {
        GLES20.glUseProgram(boundingBoxProgram)

        // Enable blending for semi-transparent surface
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        // Enable depth test for proper rendering
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)

        // Set MVP matrix
        GLES20.glUniformMatrix4fv(mvpMatrixParam, 1, false, mvpMatrix, 0)

        // Set color for surface (semi-transparent white/light gray)
        val surfaceColor = floatArrayOf(0.9f, 0.9f, 0.9f, 0.3f) // Light gray with transparency
        GLES20.glUniform4fv(colorParam, 1, surfaceColor, 0)

        // Enable vertex array
        GLES20.glEnableVertexAttribArray(positionParam)

        // Set vertex data (bottom face vertices: 0, 1, 2, 3)
        vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(positionParam, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        // Draw filled surface as triangles
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, surfaceIndices.size, GLES20.GL_UNSIGNED_SHORT, surfaceIndexBuffer)

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionParam)

        // Disable depth test for wireframe overlay
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
    }

    private fun drawWireframeBoundingBox(mvpMatrix: FloatArray, image: AugmentedImage) {
        GLES20.glUseProgram(boundingBoxProgram)

        // Enable blending for semi-transparent lines
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        // Disable depth test for overlay effect
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)

        // Set MVP matrix
        GLES20.glUniformMatrix4fv(mvpMatrixParam, 1, false, mvpMatrix, 0)

        // Set color based on tracking quality
        val color = when (image.trackingMethod) {
            AugmentedImage.TrackingMethod.FULL_TRACKING -> floatArrayOf(0.2f, 1.0f, 0.2f, 1.0f) // Bright Green
            AugmentedImage.TrackingMethod.LAST_KNOWN_POSE -> floatArrayOf(1.0f, 1.0f, 0.2f, 1.0f) // Bright Yellow
            else -> floatArrayOf(1.0f, 0.3f, 0.3f, 1.0f) // Bright Red
        }
        GLES20.glUniform4fv(colorParam, 1, color, 0)

        // Enable vertex array
        GLES20.glEnableVertexAttribArray(positionParam)

        // Set vertex data
        vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(positionParam, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        // Draw the bounding box lines (thicker for better visibility)
        GLES20.glLineWidth(5.0f)
        GLES20.glDrawElements(GLES20.GL_LINES, boundingBoxIndices.size, GLES20.GL_UNSIGNED_SHORT, indexBuffer)

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionParam)

        // Re-enable depth test
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        // Disable blending
        GLES20.glDisable(GLES20.GL_BLEND)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            val error = GLES20.glGetShaderInfoLog(shader)
            android.util.Log.e("BoundingBoxRenderer", "Shader compile error: $error")
            GLES20.glDeleteShader(shader)
            return 0
        }

        return shader
    }

    companion object {
        private const val VERTEX_SHADER = """
            attribute vec4 a_Position;
            uniform mat4 u_MVPMatrix;
            void main() {
                gl_Position = u_MVPMatrix * a_Position;
            }
        """

        private const val FRAGMENT_SHADER = """
            precision mediump float;
            uniform vec4 u_Color;
            void main() {
                gl_FragColor = u_Color;
            }
        """

        private const val TEXTURE_VERTEX_SHADER = """
            attribute vec4 a_Position;
            attribute vec2 a_TexCoord;
            uniform mat4 u_MVPMatrix;
            varying vec2 v_TexCoord;
            void main() {
                gl_Position = u_MVPMatrix * a_Position;
                v_TexCoord = a_TexCoord;
            }
        """

        private const val TEXTURE_FRAGMENT_SHADER = """
            precision mediump float;
            uniform sampler2D u_Texture;
            varying vec2 v_TexCoord;
            void main() {
                gl_FragColor = texture2D(u_Texture, v_TexCoord);
            }
        """
    }
}
