#include "native_processor.h"
#include <android/log.h>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>

#define LOG_TAG "FlamApp_Native"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

using namespace cv;

// Convert RGBA to Mat
Mat bytesToMat(jbyte* data, int width, int height) {
    Mat mat(height, width, CV_8UC4, data);
    return mat;
}

// Convert Mat to RGBA bytes
void matToBytes(const Mat& mat, jbyte* output) {
    if (mat.isContinuous()) {
        memcpy(output, mat.data, mat.total() * mat.elemSize());
    } else {
        LOGE("Mat is not continuous");
    }
}

Mat NativeProcessor::processGrayscale(const Mat& input) {
    Mat gray, output;
    
    // Convert RGBA to grayscale
    cvtColor(input, gray, COLOR_RGBA2GRAY);
    
    // Convert back to RGBA for display
    cvtColor(gray, output, COLOR_GRAY2RGBA);
    
    return output;
}

Mat NativeProcessor::processCanny(const Mat& input) {
    Mat gray, edges, output;
    
    // Convert to grayscale
    cvtColor(input, gray, COLOR_RGBA2GRAY);
    
    // Apply Gaussian blur to reduce noise
    GaussianBlur(gray, gray, Size(5, 5), 1.5);
    
    // Apply Canny edge detection
    Canny(gray, edges, 50, 150);
    
    // Convert edges to RGBA
    cvtColor(edges, output, COLOR_GRAY2RGBA);
    
    return output;
}

Mat NativeProcessor::convertYUVtoRGB(const Mat& yuv) {
    Mat rgb;
    cvtColor(yuv, rgb, COLOR_YUV2RGBA_NV21);
    return rgb;
}

extern "C" {

JNIEXPORT jbyteArray JNICALL
Java_com_flamapp_NativeProcessor_processFrame(
        JNIEnv* env,
        jobject /* this */,
        jbyteArray inputData,
        jint width,
        jint height,
        jint mode) {
    
    if (inputData == nullptr) {
        LOGE("Input data is null");
        return nullptr;
    }
    
    try {
        // Get input byte array
        jbyte* input = env->GetByteArrayElements(inputData, nullptr);
        if (input == nullptr) {
            LOGE("Failed to get input array elements");
            return nullptr;
        }
        
        jsize inputLength = env->GetArrayLength(inputData);
        LOGD("Processing frame: %dx%d, mode=%d, length=%d", width, height, mode, inputLength);
        
        // Convert to OpenCV Mat
        Mat inputMat = bytesToMat(input, width, height);
        
        // Process based on mode
        Mat outputMat;
        switch (mode) {
            case 0:
                outputMat = NativeProcessor::processGrayscale(inputMat);
                break;
            case 1:
                outputMat = NativeProcessor::processCanny(inputMat);
                break;
            default:
                outputMat = inputMat.clone();
                break;
        }
        
        // Create output byte array
        jbyteArray outputData = env->NewByteArray(width * height * 4);
        if (outputData == nullptr) {
            LOGE("Failed to create output array");
            env->ReleaseByteArrayElements(inputData, input, JNI_ABORT);
            return nullptr;
        }
        
        // Copy processed data to output
        jbyte* output = env->GetByteArrayElements(outputData, nullptr);
        if (output != nullptr) {
            matToBytes(outputMat, output);
            env->ReleaseByteArrayElements(outputData, output, 0);
        }
        
        // Release input array
        env->ReleaseByteArrayElements(inputData, input, JNI_ABORT);
        
        return outputData;
        
    } catch (const cv::Exception& e) {
        LOGE("OpenCV exception: %s", e.what());
        return nullptr;
    } catch (const std::exception& e) {
        LOGE("Standard exception: %s", e.what());
        return nullptr;
    } catch (...) {
        LOGE("Unknown exception occurred");
        return nullptr;
    }
}

JNIEXPORT void JNICALL
Java_com_flamapp_NativeProcessor_initProcessor(JNIEnv* env, jobject /* this */) {
    LOGD("Native processor initialized");
}

JNIEXPORT void JNICALL
Java_com_flamapp_NativeProcessor_releaseProcessor(JNIEnv* env, jobject /* this */) {
    LOGD("Native processor released");
}

} // extern "C"
