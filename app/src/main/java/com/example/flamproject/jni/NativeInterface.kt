package com.example.flamproject.jni

import android.graphics.Bitmap

/**
 * Interface for native image processing methods
 */
class NativeInterface {
    companion object {
        // Process image using Canny edge detection
        @JvmStatic external fun processImageCanny(inputBitmap: Bitmap, outputBitmap: Bitmap)
        
        // Process image using grayscale conversion
        @JvmStatic external fun processImageGrayscale(inputBitmap: Bitmap, outputBitmap: Bitmap)
        
        // Get OpenCV version
        @JvmStatic external fun getOpenCVVersion(): String
    }
}