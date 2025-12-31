package com.lowbyte.battery.arexample

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import com.google.ar.core.Anchor
import com.google.ar.core.Pose
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class AnchorRenderer(private val context: Context) {

    private var program = 0
    private var positionHandle = 0
    private var textureHandle = 0
    private var mvpMatrixHandle = 0

    // Quad vertices (two triangles forming a rectangle)
    private val quadCoords = floatArrayOf(
        // Triangle 1
        -0.5f,  0.5f, 0f,   // top left
         0.5f,  0.5f, 0f,   // top right
        -0.5f, -0.5f, 0f,   // bottom left
        // Triangle 2
         0.5f,  0.5f, 0f,   // top right
         0.5f, -0.5f, 0f,   // bottom right
        -0.5f, -0.5f, 0f    // bottom left
    )

    // Texture coordinates
    private val texCoords = floatArrayOf(
        0f, 0f,  // top left
        1f, 0f,  // top right
        0f, 1f,  // bottom left
        1f, 0f,  // top right
        1f, 1f,  // bottom right
        0f, 1f   // bottom left
    )

    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var texCoordBuffer: FloatBuffer

    // Store anchors and their textures
    private val anchors = mutableListOf<Anchor>()
    private val textures = mutableListOf<Int>()

    init {
        initializeBuffers()
    }

    private fun initializeBuffers() {
        // Vertex buffer
        vertexBuffer = ByteBuffer.allocateDirect(quadCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(quadCoords)
                position(0)
            }

        // Texture coordinate buffer
        texCoordBuffer = ByteBuffer.allocateDirect(texCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(texCoords)
                position(0)
            }
    }

    fun createOnGlThread() {
        program = createProgram()

        positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        textureHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
    }

    private fun createProgram(): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)

        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val error = GLES20.glGetProgramInfoLog(program)
            android.util.Log.e("AnchorRenderer", "Program link error: $error")
            GLES20.glDeleteProgram(program)
            return 0
        }

        return program
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            val error = GLES20.glGetShaderInfoLog(shader)
            android.util.Log.e("AnchorRenderer", "Shader compile error: $error")
            GLES20.glDeleteShader(shader)
            return 0
        }

        return shader
    }

    fun addAnchorWithTexture(anchor: Anchor, bitmap: Bitmap) {
        val textureId = createTextureFromBitmap(bitmap)
        if (textureId != 0) {
            anchors.add(anchor)
            textures.add(textureId)
            android.util.Log.d("AnchorRenderer", "Added anchor with texture. Total anchors: ${anchors.size}")
        }
    }

    private fun createTextureFromBitmap(bitmap: Bitmap): Int {
        val textureHandle = IntArray(1)
        GLES20.glGenTextures(1, textureHandle, 0)

        if (textureHandle[0] != 0) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

            if (GLES20.glGetError() != GLES20.GL_NO_ERROR) {
                android.util.Log.e("AnchorRenderer", "Error loading texture")
                return 0
            }
        } else {
            android.util.Log.e("AnchorRenderer", "Error generating texture handle")
            return 0
        }

        return textureHandle[0]
    }

    fun draw(viewMatrix: FloatArray, projectionMatrix: FloatArray) {
        if (anchors.isEmpty()) return

        GLES20.glUseProgram(program)

        // Enable blending for transparency
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        // Enable vertex arrays
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(textureHandle)

        // Set vertex data
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glVertexAttribPointer(textureHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer)

        for (i in anchors.indices) {
            val anchor = anchors[i]
            val textureId = textures[i]

            if (anchor.trackingState != com.google.ar.core.TrackingState.TRACKING) {
                continue
            }

            // Calculate MVP matrix
            val mvpMatrix = FloatArray(16)
            val modelMatrix = FloatArray(16)

            // Get anchor pose and convert to matrix
            val pose = anchor.pose
            pose.toMatrix(modelMatrix, 0)

            // Scale the quad (adjust size as needed)
            Matrix.scaleM(modelMatrix, 0, 0.3f, 0.3f, 0.3f)

            // MVP = Projection * View * Model
            val vpMatrix = FloatArray(16)
            Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)

            // Bind texture
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "uTexture"), 0)

            // Set MVP matrix
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

            // Draw the quad
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6)
        }

        // Disable vertex arrays
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(textureHandle)

        // Disable blending
        GLES20.glDisable(GLES20.GL_BLEND)
    }

    fun clearAnchors() {
        // Clean up textures
        GLES20.glDeleteTextures(textures.size, textures.toIntArray(), 0)
        anchors.clear()
        textures.clear()
    }

    companion object {
        private const val vertexShaderCode = """
            uniform mat4 uMVPMatrix;
            attribute vec4 aPosition;
            attribute vec2 aTexCoord;
            varying vec2 vTexCoord;

            void main() {
                gl_Position = uMVPMatrix * aPosition;
                vTexCoord = aTexCoord;
            }
        """

        private const val fragmentShaderCode = """
            precision mediump float;
            uniform sampler2D uTexture;
            varying vec2 vTexCoord;

            void main() {
                gl_FragColor = texture2D(uTexture, vTexCoord);
            }
        """
    }
}
