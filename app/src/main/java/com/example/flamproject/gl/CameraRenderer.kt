package com.example.flamproject.gl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.util.Log
import android.view.Surface
import androidx.camera.core.ImageProxy
import androidx.camera.core.SurfaceRequest
import com.example.flamproject.jni.NativeInterface
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * OpenGL ES renderer for camera preview and processing
 */
class CameraRenderer(private val context: Context) : GLSurfaceView.Renderer {
    
    private val TAG = "CameraRenderer"
    
    // OpenGL program handles
    private var program = 0
    private var textureHandle = 0
    private var positionHandle = 0
    private var texCoordHandle = 0
    
    // Texture IDs
    private var cameraTextureId = 0
    private var processedTextureId = 0
    
    // Vertex and texture coordinate data
    private val vertexData = floatArrayOf(
        -1.0f, -1.0f, 0.0f,  // Bottom left
         1.0f, -1.0f, 0.0f,  // Bottom right
        -1.0f,  1.0f, 0.0f,  // Top left
         1.0f,  1.0f, 0.0f   // Top right
    )
    
    private val texCoordData = floatArrayOf(
        0.0f, 1.0f,  // Bottom left
        1.0f, 1.0f,  // Bottom right
        0.0f, 0.0f,  // Top left
        1.0f, 0.0f   // Top right
    )
    
    // Buffers
    private val vertexBuffer: FloatBuffer
    private val texCoordBuffer: FloatBuffer
    
    // Surface for camera preview
    private var surfaceTexture: SurfaceTexture? = null
    private var surfaceRequest: SurfaceRequest? = null
    
    // Bitmaps for processing
    private var inputBitmap: Bitmap? = null
    private var outputBitmap: Bitmap? = null
    private var imageWidth = 0
    private var imageHeight = 0
    
    init {
        // Initialize vertex buffer
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(vertexData)
                position(0)
            }
        
        // Initialize texture coordinate buffer
        texCoordBuffer = ByteBuffer.allocateDirect(texCoordData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(texCoordData)
                position(0)
            }
    }
    
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Set clear color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        
        // Create shader program
        program = createProgram()
        
        // Get handle to vertex shader's position attribute
        positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        
        // Get handle to texture coordinates attribute
        texCoordHandle = GLES20.glGetAttribLocation(program, "vTexCoord")
        
        // Get handle to texture uniform
        textureHandle = GLES20.glGetUniformLocation(program, "sTexture")
        
        // Generate texture for camera preview
        val textures = IntArray(2)
        GLES20.glGenTextures(2, textures, 0)
        cameraTextureId = textures[0]
        processedTextureId = textures[1]
        
        // Set up camera texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, cameraTextureId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        
        // Set up processed texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, processedTextureId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
    }
    
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        // Set viewport
        GLES20.glViewport(0, 0, width, height)
        
        // Create SurfaceTexture for camera preview
        surfaceTexture = SurfaceTexture(cameraTextureId).apply {
            setOnFrameAvailableListener {
                // Request render when new frame is available
                // This is handled by CameraGLSurfaceView.updateTexture
            }
        }
        
        // Provide surface to camera
        surfaceRequest?.let { request ->
            val surface = Surface(surfaceTexture)
            request.provideSurface(surface, { Log.d(TAG, "Surface provided") }) { error ->
                Log.e(TAG, "Failed to provide surface: $error")
            }
        }
    }
    
    override fun onDrawFrame(gl: GL10?) {
        // Clear the screen
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        
        // Use the shader program
        GLES20.glUseProgram(program)
        
        // Update texture if SurfaceTexture is available
        surfaceTexture?.updateTexImage()
        
        // Bind the texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, processedTextureId)
        GLES20.glUniform1i(textureHandle, 0)
        
        // Enable vertex attributes
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        
        // Set vertex attributes
        GLES20.glVertexAttribPointer(
            positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer
        )
        GLES20.glVertexAttribPointer(
            texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer
        )
        
        // Draw the quad
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        
        // Disable vertex attributes
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }
    
    /**
     * Update texture with new camera frame and process it
     */
    fun updateTexture(imageProxy: ImageProxy, processFrame: Boolean) {
        // Initialize bitmaps if needed or if size changed
        if (inputBitmap == null || imageWidth != imageProxy.width || imageHeight != imageProxy.height) {
            imageWidth = imageProxy.width
            imageHeight = imageProxy.height
            
            // Recycle old bitmaps
            inputBitmap?.recycle()
            outputBitmap?.recycle()
            
            // Create new bitmaps
            inputBitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)
            outputBitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)
        }
        
        // Convert YUV to RGB and copy to input bitmap
        val planes = imageProxy.planes
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer
        
        // TODO: Implement YUV to RGB conversion
        // For now, we'll use a placeholder conversion
        // In a real implementation, use renderscript or native code for this conversion
        
        // Process the image if enabled
        if (processFrame && inputBitmap != null && outputBitmap != null) {
            // Apply Canny edge detection using native code
            NativeInterface.processImageCanny(inputBitmap!!, outputBitmap!!)
            
            // Upload processed bitmap to texture
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, processedTextureId)
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, outputBitmap, 0)
        } else if (inputBitmap != null) {
            // Upload original bitmap to texture
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, processedTextureId)
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, inputBitmap, 0)
        }
    }
    
    /**
     * Set the surface request for camera preview
     */
    fun setSurfaceRequest(request: SurfaceRequest) {
        surfaceRequest = request
        
        // If surface texture is already created, provide it to the camera
        surfaceTexture?.let { texture ->
            val surface = Surface(texture)
            request.provideSurface(surface, { Log.d(TAG, "Surface provided") }) { error ->
                Log.e(TAG, "Failed to provide surface: $error")
            }
        }
    }
    
    /**
     * Create OpenGL shader program
     */
    private fun createProgram(): Int {
        // Vertex shader source
        val vertexShaderCode = """
            attribute vec4 vPosition;
            attribute vec2 vTexCoord;
            varying vec2 texCoord;
            void main() {
                gl_Position = vPosition;
                texCoord = vTexCoord;
            }
        """.trimIndent()
        
        // Fragment shader source
        val fragmentShaderCode = """
            precision mediump float;
            varying vec2 texCoord;
            uniform sampler2D sTexture;
            void main() {
                gl_FragColor = texture2D(sTexture, texCoord);
            }
        """.trimIndent()
        
        // Compile shaders
        val vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        
        // Create and link program
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        
        // Check link status
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES20.GL_TRUE) {
            val error = GLES20.glGetProgramInfoLog(program)
            Log.e(TAG, "Error linking program: $error")
            GLES20.glDeleteProgram(program)
            return 0
        }
        
        return program
    }
    
    /**
     * Compile shader from source code
     */
    private fun compileShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        
        // Check compile status
        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] != GLES20.GL_TRUE) {
            val error = GLES20.glGetShaderInfoLog(shader)
            Log.e(TAG, "Error compiling shader: $error")
            GLES20.glDeleteShader(shader)
            return 0
        }
        
        return shader
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        surfaceTexture?.release()
        inputBitmap?.recycle()
        outputBitmap?.recycle()
    }
}