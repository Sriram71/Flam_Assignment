#ifndef IMAGE_PROCESSOR_H
#define IMAGE_PROCESSOR_H

#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>

class ImageProcessor {
public:
    // Process image using Canny edge detection
    static void processImageCanny(const cv::Mat& input, cv::Mat& output, 
                                 double threshold1 = 50, double threshold2 = 150);
    
    // Process image using grayscale conversion
    static void processImageGrayscale(const cv::Mat& input, cv::Mat& output);
};

#endif // IMAGE_PROCESSOR_H