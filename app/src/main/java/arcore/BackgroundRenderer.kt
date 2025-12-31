package com.example.ardrawing

import android.opengl.GLES11Ext
import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class BackgroundRenderer {

    var textureId = -1
        private set

    private var quadCoords: FloatBuffer? = null
    private var quadTexCoords: FloatBuffer? = null
    private var transformedQuadTexCoords: FloatBuffer? = null
    private var quadProgram = 0
    private var quadPositionParam = 0
    private var quadTexCoordParam = 0

    fun createOnGlThread() {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )

        // Create shader program
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)

        quadProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(quadProgram, vertexShader)
        GLES20.glAttachShader(quadProgram, fragmentShader)
        GLES20.glLinkProgram(quadProgram)
        GLES20.glUseProgram(quadProgram)

        quadPositionParam = GLES20.glGetAttribLocation(quadProgram, "a_Position")
        quadTexCoordParam = GLES20.glGetAttribLocation(quadProgram, "a_TexCoord")

        // Create vertex buffers
        val coords = floatArrayOf(
            -1.0f, -1.0f, 0.0f,
            -1.0f, 1.0f, 0.0f,
            1.0f, -1.0f, 0.0f,
            1.0f, 1.0f, 0.0f
        )

        // ARCore camera texture coordinates (needs transformation)
        val texCoords = floatArrayOf(
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
        )

        quadCoords = ByteBuffer.allocateDirect(coords.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        quadCoords?.put(coords)
        quadCoords?.position(0)

        quadTexCoords = ByteBuffer.allocateDirect(texCoords.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        quadTexCoords?.put(texCoords)
        quadTexCoords?.position(0)

        // Buffer for transformed texture coordinates (updated by ARCore)
        transformedQuadTexCoords = ByteBuffer.allocateDirect(texCoords.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
    }

    fun draw() {
        if (quadProgram == 0) return

        GLES20.glUseProgram(quadProgram)

        // Set the active texture unit to texture unit 0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)

        // Enable vertex arrays
        GLES20.glEnableVertexAttribArray(quadPositionParam)
        GLES20.glEnableVertexAttribArray(quadTexCoordParam)

        // Set vertex positions
        quadCoords?.let {
            GLES20.glVertexAttribPointer(quadPositionParam, 3, GLES20.GL_FLOAT, false, 0, it)
        }

        // Set transformed texture coordinates (updated by updateTexCoords)
        transformedQuadTexCoords?.let {
            GLES20.glVertexAttribPointer(quadTexCoordParam, 2, GLES20.GL_FLOAT, false, 0, it)
        }

        // Draw the quad
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        // Disable vertex arrays
        GLES20.glDisableVertexAttribArray(quadPositionParam)
        GLES20.glDisableVertexAttribArray(quadTexCoordParam)
    }

    /**
     * Update texture coordinates using ARCore's display UV transform
     */
    fun updateTexCoords(frame: com.google.ar.core.Frame) {
        if (quadTexCoords == null || transformedQuadTexCoords == null) return

        try {
            // ARCore's transformDisplayUvCoords expects source and destination FloatBuffers
            // Copy original coords to transformed buffer first
            quadTexCoords?.let { src ->
                transformedQuadTexCoords?.let { dst ->
                    src.position(0)
                    dst.position(0)
                    for (i in 0 until 8) {
                        dst.put(src.get(i))
                    }
                    dst.position(0)
                }
            }

            // Apply ARCore's UV coordinate transformation in place
            frame.transformDisplayUvCoords(transformedQuadTexCoords, transformedQuadTexCoords)

        } catch (e: Exception) {
            android.util.Log.w("BackgroundRenderer", "Failed to update texture coordinates: ${e.message}")
            // Fallback: use original coordinates
            quadTexCoords?.let { src ->
                transformedQuadTexCoords?.let { dst ->
                    src.position(0)
                    dst.position(0)
                    for (i in 0 until 8) {
                        dst.put(src.get(i))
                    }
                    dst.position(0)
                }
            }
        }
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }

    companion object {
        private const val VERTEX_SHADER = """
            attribute vec4 a_Position;
            attribute vec2 a_TexCoord;
            varying vec2 v_TexCoord;
            void main() {
                gl_Position = a_Position;
                v_TexCoord = a_TexCoord;
            }
        """

        private const val FRAGMENT_SHADER = """
            #extension GL_OES_EGL_image_external : require
            precision mediump float;
            varying vec2 v_TexCoord;
            uniform samplerExternalOES sTexture;
            void main() {
                gl_FragColor = texture2D(sTexture, v_TexCoord);
            }
        """
    }
}