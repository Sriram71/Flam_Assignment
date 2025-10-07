cmake_minimum_required(VERSION 3.10.2)
project(FlamProject)

# Set OpenCV path - using the OpenCV-android-sdk in the project root
set(OpenCV_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../OpenCV-android-sdk/sdk/native/jni)
find_package(OpenCV REQUIRED QUIET)

# Include OpenCV headers
include_directories(${OpenCV_INCLUDE_DIRS})

# Define the native-lib library
add_library(native-lib SHARED
            native-lib.cpp
            image_processor.cpp)

# Find required libraries
find_library(log-lib log)
find_library(android-lib android)
find_library(jnigraphics-lib jnigraphics)
find_library(GLESv2-lib GLESv2)
find_library(EGL-lib EGL)

# Link libraries
target_link_libraries(native-lib
                      ${OpenCV_LIBS}
                      ${log-lib}
                      ${android-lib}
                      ${jnigraphics-lib}
                      ${GLESv2-lib}
                      ${EGL-lib})