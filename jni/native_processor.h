#ifndef NATIVE_PROCESSOR_H
#define NATIVE_PROCESSOR_H

#include <jni.h>
#include <opencv2/opencv.hpp>

class NativeProcessor {
public:
    static cv::Mat processGrayscale(const cv::Mat& input);
    static cv::Mat processCanny(const cv::Mat& input);
    static cv::Mat convertYUVtoRGB(const cv::Mat& yuv);
};

#endif // NATIVE_PROCESSOR_H
