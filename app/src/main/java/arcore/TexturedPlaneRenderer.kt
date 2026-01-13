package com.example.ardrawing

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import com.google.ar.core.Anchor
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * Renders the selected image as a texture on a plane/quad at the anchor position
 */
class TexturedPlaneRenderer {
    
    private var textureProgram = 0
    private var texturePositionParam = 0
    private var textureCoordParam = 0
    private var textureMvpMatrixParam = 0
    private var textureSamplerParam = 0
    private var textureId = -1
    
    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var texCoordBuffer: FloatBuffer
    
    // Quad vertices (centered at origin, on XY plane)
    private val quadVertices = floatArrayOf(
        -0.5f, -0.5f, 0.0f,  // bottom-left
         0.5f, -0.5f, 0.0f,  // bottom-right
         0.5f,  0.5f, 0.0f,  // top-right
        -0.5f,  0.5f, 0.0f   // top-left
    )
    
    // Texture coordinates
    private val texCoords = floatArrayOf(
        0.0f, 1.0f,  // bottom-left
        1.0f, 1.0f,  // bottom-right
        1.0f, 0.0f,  // top-right
        0.0f, 0.0f   // top-left
    )
    
    private var anchor: Anchor? = null
    private var imageWidth: Float = 1.0f
    private var imageHeight: Float = 1.0f
    private var isImageReady: Boolean = false
    
    fun createOnGlThread() {
        // Create shader program
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)
        
        textureProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(textureProgram, vertexShader)
        GLES20.glAttachShader(textureProgram, fragmentShader)
        GLES20.glLinkProgram(textureProgram)
        
        texturePositionParam = GLES20.glGetAttribLocation(textureProgram, "a_Position")
        textureCoordParam = GLES20.glGetAttribLocation(textureProgram, "a_TexCoord")
        textureMvpMatrixParam = GLES20.glGetUniformLocation(textureProgram, "u_MVPMatrix")
        textureSamplerParam = GLES20.glGetUniformLocation(textureProgram, "u_Texture")
        
        // Create vertex buffers
        vertexBuffer = ByteBuffer.allocateDirect(quadVertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(quadVertices)
                position(0)
            }
        
        texCoordBuffer = ByteBuffer.allocateDirect(texCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(texCoords)
                position(0)
            }
    }
    
    fun setImage(bitmap: Bitmap, anchor: Anchor, widthMeters: Float, heightMeters: Float) {
        Log.d("TexturedPlaneRenderer", "setImage called: bitmap=${bitmap.width}x${bitmap.height}, anchor=${anchor.trackingState}, size=${widthMeters}x${heightMeters}")
        this.anchor = anchor
        this.imageWidth = widthMeters
        this.imageHeight = heightMeters
        isImageReady = false
        
        // Create texture from bitmap
        loadTexture(bitmap)
        
        Log.d("TexturedPlaneRenderer", "Image set successfully, textureId=$textureId, isImageReady=$isImageReady")
    }
    
    private fun loadTexture(bitmap: Bitmap) {
        if (bitmap.isRecycled) {
            Log.e("TexturedPlaneRenderer", "Bitmap is recycled")
            return
        }
        
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]
        
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        
        // Set texture parameters
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        
        // Load bitmap into texture
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        
        if (GLES20.glGetError() != GLES20.GL_NO_ERROR) {
            Log.e("TexturedPlaneRenderer", "Error loading texture")
            isImageReady = false
        } else {
            Log.d("TexturedPlaneRenderer", "Texture loaded successfully: ${bitmap.width}x${bitmap.height}, textureId=$textureId")
            isImageReady = true
        }
    }
    
    fun draw(viewMatrix: FloatArray, projectionMatrix: FloatArray) {
        if (!isImageReady) {
            return
        }
        
        val currentAnchor = anchor ?: return
        if (textureId == -1) {
            Log.w("TexturedPlaneRenderer", "Texture not loaded, textureId = -1")
            return
        }
        
        // Log every 60 frames to confirm it's drawing
        if (System.currentTimeMillis() % 1000 < 20) {
            Log.d("TexturedPlaneRenderer", "Drawing overlay... TextureID: $textureId, Anchor: ${anchor?.trackingState}")
        }
        
        // Check if anchor is still tracking
        if (currentAnchor.trackingState != TrackingState.TRACKING) {
            return
        }
        
        // Get anchor pose
        val anchorPose = currentAnchor.pose
        
        // Create model matrix from anchor pose
        val modelMatrix = FloatArray(16)
        anchorPose.toMatrix(modelMatrix, 0)
        
        // Scale to match image dimensions
        android.opengl.Matrix.scaleM(modelMatrix, 0, imageWidth, imageHeight, 1.0f)
        
        // Calculate MVP matrix
        val mvpMatrix = FloatArray(16)
        val mvMatrix = FloatArray(16)
        android.opengl.Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        android.opengl.Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)
        
        // Use texture program
        GLES20.glUseProgram(textureProgram)
        
        // Enable blending for transparency (but keep image mostly opaque)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        
        // Enable depth test to ensure proper rendering order
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)
        GLES20.glDepthMask(true)
        
        // Bind texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(textureSamplerParam, 0)
        
        // Set MVP matrix
        GLES20.glUniformMatrix4fv(textureMvpMatrixParam, 1, false, mvpMatrix, 0)
        
        // Enable vertex arrays
        GLES20.glEnableVertexAttribArray(texturePositionParam)
        GLES20.glEnableVertexAttribArray(textureCoordParam)
        
        // Set vertex data
        vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(texturePositionParam, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        
        texCoordBuffer.position(0)
        GLES20.glVertexAttribPointer(textureCoordParam, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer)
        
        // Draw quad
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)
        
        // Disable vertex arrays
        GLES20.glDisableVertexAttribArray(texturePositionParam)
        GLES20.glDisableVertexAttribArray(textureCoordParam)
        
        // Disable blending and depth test
        GLES20.glDisable(GLES20.GL_BLEND)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
    }
    
    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        
        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            val error = GLES20.glGetShaderInfoLog(shader)
            Log.e("TexturedPlaneRenderer", "Shader compile error: $error")
            GLES20.glDeleteShader(shader)
            return 0
        }
        return shader
    }
    
    companion object {
        private const val VERTEX_SHADER = """
            uniform mat4 u_MVPMatrix;
            attribute vec4 a_Position;
            attribute vec2 a_TexCoord;
            varying vec2 v_TexCoord;
            
            void main() {
                gl_Position = u_MVPMatrix * a_Position;
                v_TexCoord = a_TexCoord;
            }
        """
        
        private const val FRAGMENT_SHADER = """
            precision mediump float;
            uniform sampler2D u_Texture;
            varying vec2 v_TexCoord;
            
            void main() {
                vec4 texColor = texture2D(u_Texture, v_TexCoord);
                // Apply slight transparency to see real object underneath
                gl_FragColor = vec4(texColor.rgb, texColor.a * 0.8);
            }
        """
    }
}
