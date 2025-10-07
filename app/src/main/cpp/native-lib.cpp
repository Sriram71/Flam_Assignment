#include <jni.h>
#include <string>
#include <android/log.h>
#include <android/bitmap.h>

#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>

#include "image_processor.h"

#define TAG "NativeLib"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

extern "C" {

// Convert Android bitmap to OpenCV Mat
void bitmapToMat(JNIEnv *env, jobject bitmap, cv::Mat &mat) {
    AndroidBitmapInfo info;
    void *pixels = 0;
    
    try {
        CV_Assert(AndroidBitmap_getInfo(env, bitmap, &info) >= 0);
        CV_Assert(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888);
        CV_Assert(AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0);
        CV_Assert(pixels);
        
        mat.create(info.height, info.width, CV_8UC4);
        
        if (info.stride == mat.step) {
            memcpy(mat.data, pixels, info.stride * info.height);
        } else {
            // Copy row by row if stride is different
            for (int y = 0; y < info.height; ++y) {
                memcpy(mat.data + y * mat.step, 
                       static_cast<char*>(pixels) + y * info.stride, 
                       mat.step);
            }
        }
        
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch (const cv::Exception &e) {
        AndroidBitmap_unlockPixels(env, bitmap);
        LOGE("OpenCV error: %s", e.what());
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        LOGE("Unknown exception in JNI code");
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code");
        return;
    }
}

// Convert OpenCV Mat to Android bitmap
void matToBitmap(JNIEnv *env, cv::Mat &mat, jobject bitmap) {
    AndroidBitmapInfo info;
    void *pixels = 0;
    
    try {
        CV_Assert(AndroidBitmap_getInfo(env, bitmap, &info) >= 0);
        CV_Assert(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888);
        CV_Assert(mat.type() == CV_8UC4);
        CV_Assert(AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0);
        CV_Assert(pixels);
        
        if (info.stride == mat.step) {
            memcpy(pixels, mat.data, info.stride * info.height);
        } else {
            // Copy row by row if stride is different
            for (int y = 0; y < info.height; ++y) {
                memcpy(static_cast<char*>(pixels) + y * info.stride, 
                       mat.data + y * mat.step, 
                       mat.step);
            }
        }
        
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch (const cv::Exception &e) {
        AndroidBitmap_unlockPixels(env, bitmap);
        LOGE("OpenCV error: %s", e.what());
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        LOGE("Unknown exception in JNI code");
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code");
        return;
    }
}

// Process image using Canny edge detection
JNIEXPORT void JNICALL
Java_com_example_flamproject_jni_NativeInterface_processImageCanny(
        JNIEnv *env, jclass clazz, jobject input_bitmap, jobject output_bitmap) {
    
    cv::Mat inputMat;
    cv::Mat outputMat;
    
    bitmapToMat(env, input_bitmap, inputMat);
    
    // Process the image
    ImageProcessor::processImageCanny(inputMat, outputMat);
    
    matToBitmap(env, outputMat, output_bitmap);
}

// Process image using grayscale conversion
JNIEXPORT void JNICALL
Java_com_example_flamproject_jni_NativeInterface_processImageGrayscale(
        JNIEnv *env, jclass clazz, jobject input_bitmap, jobject output_bitmap) {
    
    cv::Mat inputMat;
    cv::Mat outputMat;
    
    bitmapToMat(env, input_bitmap, inputMat);
    
    // Process the image
    ImageProcessor::processImageGrayscale(inputMat, outputMat);
    
    matToBitmap(env, outputMat, output_bitmap);
}

// Get OpenCV version
JNIEXPORT jstring JNICALL
Java_com_example_flamproject_jni_NativeInterface_getOpenCVVersion(
        JNIEnv *env, jclass clazz) {
    return env->NewStringUTF(CV_VERSION);
}

} // extern "C"