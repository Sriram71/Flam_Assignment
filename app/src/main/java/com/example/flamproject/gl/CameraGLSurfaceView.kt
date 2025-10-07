package com.example.flamproject.gl

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.Surface
import androidx.camera.core.ImageProxy
import androidx.camera.core.SurfaceRequest
import java.util.concurrent.Executor

/**
 * Custom GLSurfaceView for rendering camera preview with OpenGL ES
 */
class CameraGLSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    private val renderer: CameraRenderer
    private var processingEnabled = true
    
    // Surface provider for CameraX
    val surfaceProvider = object : androidx.camera.core.Preview.SurfaceProvider {
        override fun onSurfaceRequested(request: SurfaceRequest) {
            renderer.setSurfaceRequest(request)
        }
    }

    init {
        // Configure OpenGL ES
        setEGLContextClientVersion(2)
        
        // Create and set the renderer
        renderer = CameraRenderer(context)
        setRenderer(renderer)
        
        // Render only when there's a new frame
        renderMode = RENDERMODE_WHEN_DIRTY
    }
    
    /**
     * Update texture with new camera frame
     */
    fun updateTexture(imageProxy: ImageProxy) {
        queueEvent {
            renderer.updateTexture(imageProxy, processingEnabled)
            requestRender()
        }
    }
    
    /**
     * Enable or disable image processing
     */
    fun setProcessingEnabled(enabled: Boolean) {
        processingEnabled = enabled
    }
    
    /**
     * Clean up resources when view is destroyed
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        renderer.cleanup()
    }
}