#include "image_processor.h"

void ImageProcessor::processImageCanny(const cv::Mat& input, cv::Mat& output, 
                                     double threshold1, double threshold2) {
    // Convert to grayscale if input is color
    cv::Mat grayImage;
    if (input.channels() > 1) {
        cv::cvtColor(input, grayImage, cv::COLOR_RGBA2GRAY);
    } else {
        grayImage = input.clone();
    }
    
    // Apply Gaussian blur to reduce noise
    cv::Mat blurredImage;
    cv::GaussianBlur(grayImage, blurredImage, cv::Size(5, 5), 1.5);
    
    // Apply Canny edge detection
    cv::Canny(blurredImage, output, threshold1, threshold2);
    
    // Convert back to RGBA for display
    cv::cvtColor(output, output, cv::COLOR_GRAY2RGBA);
}

void ImageProcessor::processImageGrayscale(const cv::Mat& input, cv::Mat& output) {
    // Convert to grayscale
    cv::Mat grayImage;
    if (input.channels() > 1) {
        cv::cvtColor(input, grayImage, cv::COLOR_RGBA2GRAY);
    } else {
        grayImage = input.clone();
    }
    
    // Convert back to RGBA for display
    cv::cvtColor(grayImage, output, cv::COLOR_GRAY2RGBA);
}