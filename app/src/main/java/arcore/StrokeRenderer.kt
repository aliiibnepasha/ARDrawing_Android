package com.example.ardrawing

import android.opengl.GLES20
import android.util.Log
import com.google.ar.core.Anchor
import com.google.ar.core.Pose
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * Data class to store a single stroke point in anchor-local coordinates
 */
data class StrokePoint(
    val x: Float,  // Local X coordinate (-0.5 to 0.5)
    val y: Float   // Local Y coordinate (-0.5 to 0.5)
)

/**
 * Data class to store a complete stroke (series of points)
 */
data class Stroke(
    val points: MutableList<StrokePoint>,
    val color: FloatArray = floatArrayOf(1.0f, 0.0f, 0.0f, 1.0f) // Red by default
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }
    
    override fun hashCode(): Int {
        return points.hashCode()
    }
}

/**
 * Renders drawing strokes on top of the image plane
 */
class StrokeRenderer {
    
    private var strokeProgram = 0
    private var strokePositionParam = 0
    private var strokeColorParam = 0
    private var strokeMvpMatrixParam = 0
    
    // Store all strokes
    private val strokes = mutableListOf<Stroke>()
    
    private var anchor: Anchor? = null
    private var imageWidth: Float = 1.0f
    private var imageHeight: Float = 1.0f
    
    fun createOnGlThread() {
        // Create shader program
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)
        
        strokeProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(strokeProgram, vertexShader)
        GLES20.glAttachShader(strokeProgram, fragmentShader)
        GLES20.glLinkProgram(strokeProgram)
        
        strokePositionParam = GLES20.glGetAttribLocation(strokeProgram, "a_Position")
        strokeColorParam = GLES20.glGetUniformLocation(strokeProgram, "u_Color")
        strokeMvpMatrixParam = GLES20.glGetUniformLocation(strokeProgram, "u_MVPMatrix")
        
        Log.d("StrokeRenderer", "Stroke renderer initialized")
    }
    
    fun setAnchor(anchor: Anchor, widthMeters: Float, heightMeters: Float) {
        this.anchor = anchor
        this.imageWidth = widthMeters
        this.imageHeight = heightMeters
    }
    
    fun addStrokePoint(point: StrokePoint, color: FloatArray = floatArrayOf(1.0f, 0.0f, 0.0f, 1.0f)) {
        // Add to last stroke or create new one
        if (strokes.isEmpty() || strokes.last().points.isEmpty()) {
            strokes.add(Stroke(mutableListOf(), color))
        }
        strokes.last().points.add(point)
    }
    
    fun startNewStroke(color: FloatArray = floatArrayOf(1.0f, 0.0f, 0.0f, 1.0f)) {
        strokes.add(Stroke(mutableListOf(), color))
    }
    
    fun clearStrokes() {
        strokes.clear()
    }
    
    fun draw(viewMatrix: FloatArray, projectionMatrix: FloatArray) {
        val currentAnchor = anchor ?: return
        if (strokes.isEmpty()) return
        
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
        
        // Use stroke program
        GLES20.glUseProgram(strokeProgram)
        
        // Enable blending
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        
        // Disable depth test for overlay effect
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        
        // Set MVP matrix
        GLES20.glUniformMatrix4fv(strokeMvpMatrixParam, 1, false, mvpMatrix, 0)
        
        // Draw each stroke
        for (stroke in strokes) {
            if (stroke.points.size < 2) continue
            
            // Set color
            GLES20.glUniform4fv(strokeColorParam, 1, stroke.color, 0)
            
            // Create vertex buffer for this stroke
            val vertices = FloatArray(stroke.points.size * 3)
            for (i in stroke.points.indices) {
                val point = stroke.points[i]
                vertices[i * 3] = point.x
                vertices[i * 3 + 1] = point.y
                vertices[i * 3 + 2] = 0.01f // Slightly above the plane
            }
            
            val vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .apply {
                    put(vertices)
                    position(0)
                }
            
            // Enable vertex array
            GLES20.glEnableVertexAttribArray(strokePositionParam)
            
            // Set vertex data
            GLES20.glVertexAttribPointer(strokePositionParam, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
            
            // Draw as line strip
            GLES20.glLineWidth(5.0f)
            GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, stroke.points.size)
            
            // Disable vertex array
            GLES20.glDisableVertexAttribArray(strokePositionParam)
        }
        
        // Re-enable depth test
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
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
            Log.e("StrokeRenderer", "Shader compile error: $error")
            GLES20.glDeleteShader(shader)
            return 0
        }
        return shader
    }
    
    companion object {
        private const val VERTEX_SHADER = """
            uniform mat4 u_MVPMatrix;
            attribute vec4 a_Position;
            
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
    }
}
