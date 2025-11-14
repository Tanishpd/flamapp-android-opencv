package com.flamapp;

public class NativeProcessor {
    
    /**
     * Process a camera frame using OpenCV
     * @param inputData RGBA frame data
     * @param width Frame width
     * @param height Frame height
     * @param mode Processing mode (0=grayscale, 1=canny)
     * @return Processed RGBA frame data
     */
    public static native byte[] processFrame(byte[] inputData, int width, int height, int mode);
    
    /**
     * Initialize native processing
     */
    public static native void initProcessor();
    
    /**
     * Release native resources
     */
    public static native void releaseProcessor();
}
