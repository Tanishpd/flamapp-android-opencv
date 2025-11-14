cmake_minimum_required(VERSION 3.22.1)
project("flamapp_native")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# OpenCV Configuration
set(OPENCV_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../app/src/main/jniLibs/opencv")

# If using prebuilt OpenCV (download and extract OpenCV Android SDK)
# Adjust path as needed
find_package(OpenCV REQUIRED)

include_directories(
    ${OpenCV_INCLUDE_DIRS}
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${CMAKE_CURRENT_SOURCE_DIR}/../gl
)

# Native Processor Library
add_library(flamapp_native SHARED
    native_processor.cpp
    ../gl/renderer.cpp
)

target_link_libraries(flamapp_native
    ${OpenCV_LIBS}
    android
    log
    GLESv2
    EGL
)

# Alternative: if OpenCV SDK is extracted to app/src/main/jniLibs/opencv
# Uncomment and adjust paths:
# set(OpenCV_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../app/src/main/opencv/sdk/native/jni")
# find_package(OpenCV REQUIRED)
