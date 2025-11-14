# FlamApp - Troubleshooting Guide

Common issues and their solutions for FlamApp development and deployment.

## 🔴 Build Issues

### Issue: "OpenCV not found" during CMake build

**Symptoms:**
```
CMake Error: Could not find OpenCV
```

**Solutions:**

1. **Download OpenCV Android SDK**
   ```bash
   cd ~/Downloads
   wget https://github.com/opencv/opencv/releases/download/4.8.0/opencv-4.8.0-android-sdk.zip
   unzip opencv-4.8.0-android-sdk.zip
   ```

2. **Copy to project**
   ```bash
   cd flamapp
   mkdir -p app/src/main/jniLibs/opencv
   cp -r ~/Downloads/OpenCV-android-sdk/sdk/native/* app/src/main/jniLibs/opencv/
   ```

3. **Update CMakeLists.txt**
   ```cmake
   set(OpenCV_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../app/src/main/jniLibs/opencv/jni")
   ```

### Issue: "NDK not found" error

**Symptoms:**
```
NDK is not installed
```

**Solutions:**

1. **Install via Android Studio**
   - File → Settings → Appearance & Behavior → System Settings → Android SDK
   - SDK Tools tab → Check "NDK (Side by side)"
   - Apply

2. **Set in local.properties**
   ```properties
   ndk.dir=/path/to/android-sdk/ndk/25.1.8937393
   ```

3. **Verify installation**
   ```bash
   ls $ANDROID_HOME/ndk/
   ```

### Issue: "Execution failed for task ':app:externalNativeBuildDebug'"

**Symptoms:**
```
C++ compilation failed
```

**Solutions:**

1. **Check C++ standard**
   ```cmake
   set(CMAKE_CXX_STANDARD 17)
   ```

2. **Clean and rebuild**
   ```bash
   ./gradlew clean
   ./gradlew assembleDebug
   ```

3. **Check CMakeLists.txt paths**
   - Verify all include directories exist
   - Check library paths are correct

### Issue: Gradle sync fails

**Symptoms:**
```
Plugin with id 'com.android.application' not found
```

**Solutions:**

1. **Check build.gradle (root)**
   ```gradle
   buildscript {
       dependencies {
           classpath 'com.android.tools.build:gradle:8.1.0'
       }
   }
   ```

2. **Sync again**
   - File → Sync Project with Gradle Files

3. **Clear Gradle cache**
   ```bash
   rm -rf ~/.gradle/caches/
   ./gradlew clean --refresh-dependencies
   ```

## 📱 Runtime Issues

### Issue: App crashes on launch

**Symptoms:**
```
java.lang.UnsatisfiedLinkError: dlopen failed: library "libflamapp_native.so" not found
```

**Solutions:**

1. **Verify native library built**
   ```bash
   ls app/build/intermediates/cmake/debug/obj/arm64-v8a/
   # Should see: libflamapp_native.so
   ```

2. **Check ABI filters**
   ```gradle
   defaultConfig {
       ndk {
           abiFilters 'arm64-v8a', 'armeabi-v7a'
       }
   }
   ```

3. **Rebuild native code**
   ```bash
   ./gradlew clean
   ./gradlew assembleDebug
   ```

### Issue: Camera permission denied

**Symptoms:**
- App shows permission dialog but camera doesn't start
- Black screen after granting permission

**Solutions:**

1. **Check manifest**
   ```xml
   <uses-permission android:name="android.permission.CAMERA" />
   ```

2. **Request runtime permission**
   - Already implemented in CameraActivity.java

3. **Manual grant (for testing)**
   ```bash
   adb shell pm grant com.flamapp android.permission.CAMERA
   ```

4. **Check device settings**
   - Settings → Apps → FlamApp → Permissions → Camera → Allow

### Issue: Black screen / No camera preview

**Symptoms:**
- Permission granted but nothing displays

**Solutions:**

1. **Check Camera2 API support**
   ```bash
   adb shell getprop ro.hardware.camera
   ```

2. **Try different camera**
   - Edit CameraActivity.java
   - Change `LENS_FACING_BACK` to `LENS_FACING_FRONT`

3. **Check device compatibility**
   - Minimum Android 7.0 (API 24)
   - Camera2 API support required

4. **Verify OpenGL ES support**
   ```bash
   adb shell dumpsys | grep GLES
   # Should show: GLES: 2.0 or higher
   ```

### Issue: Low FPS (< 5 FPS)

**Symptoms:**
- Sluggish camera feed
- FPS counter shows very low values

**Solutions:**

1. **Reduce camera resolution**
   ```java
   private static final int PREVIEW_WIDTH = 320;
   private static final int PREVIEW_HEIGHT = 240;
   ```

2. **Use release build**
   ```bash
   ./gradlew assembleRelease
   ```

3. **Optimize OpenCV processing**
   ```cpp
   // Reduce Gaussian blur kernel
   GaussianBlur(gray, gray, Size(3, 3), 1.0);
   
   // Adjust Canny thresholds
   Canny(gray, edges, 100, 200);  // Higher = less edges
   ```

4. **Enable hardware acceleration**
   - Already set in AndroidManifest.xml
   - Verify: `android:hardwareAccelerated="true"`

### Issue: App freezes or hangs

**Symptoms:**
- UI becomes unresponsive
- ANR (Application Not Responding) dialog

**Solutions:**

1. **Check for main thread blocking**
   - Processing is on background thread (already implemented)

2. **Monitor memory usage**
   ```bash
   adb shell dumpsys meminfo com.flamapp
   ```

3. **Check for memory leaks**
   - Use Android Profiler in Android Studio
   - Look for increasing memory usage

4. **Optimize frame processing**
   - Skip frames if processing takes too long
   - Reduce buffer allocations

## 🌐 Web Viewer Issues

### Issue: TypeScript compilation fails

**Symptoms:**
```
error TS2304: Cannot find name 'document'
```

**Solutions:**

1. **Check tsconfig.json**
   ```json
   {
     "compilerOptions": {
       "lib": ["ES2020", "DOM"]
     }
   }
   ```

2. **Reinstall dependencies**
   ```bash
   rm -rf node_modules package-lock.json
   npm install
   ```

3. **Update TypeScript**
   ```bash
   npm install -D typescript@latest
   ```

### Issue: Canvas not displaying

**Symptoms:**
- Blank canvas
- No error in console

**Solutions:**

1. **Check canvas initialization**
   - Open browser console (F12)
   - Look for JavaScript errors

2. **Verify script loading**
   ```html
   <script type="module" src="dist/main.js"></script>
   ```

3. **Build TypeScript**
   ```bash
   cd web
   npm run build
   ```

4. **Check file paths**
   - Ensure dist/main.js exists
   - Verify relative paths in HTML

### Issue: "Module not found" error

**Symptoms:**
```
Failed to load module script
```

**Solutions:**

1. **Use proper server**
   ```bash
   # Don't just open HTML file directly
   # Use a server:
   npx http-server . -p 8000
   ```

2. **Check CORS**
   - Some servers block module loading
   - Use http-server or Python's http.server

3. **Verify TypeScript output**
   ```bash
   ls web/dist/
   # Should see: main.js
   ```

## 🔧 Development Environment Issues

### Issue: Android Studio doesn't recognize Java files

**Symptoms:**
- Red underlines in Java code
- "Cannot resolve symbol"

**Solutions:**

1. **Invalidate caches**
   - File → Invalidate Caches / Restart

2. **Reimport project**
   - File → Close Project
   - Reopen project

3. **Check Gradle sync**
   - File → Sync Project with Gradle Files

### Issue: Native code not visible in Android Studio

**Symptoms:**
- Can't navigate to C++ files
- No syntax highlighting

**Solutions:**

1. **Install C/C++ plugin**
   - File → Settings → Plugins
   - Search: "C/C++"
   - Install

2. **Refresh CMake**
   - Build → Refresh Linked C++ Projects

3. **Check CMakeLists.txt**
   - Verify path in app/build.gradle

## 🐛 Debugging Tips

### Enable verbose logging

**Android native code:**
```cpp
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "FlamApp", __VA_ARGS__)
```

**View logs:**
```bash
adb logcat | grep FlamApp
```

### Monitor performance

**FPS counter:**
- Already implemented in UI
- Check top-left corner of app

**Memory:**
```bash
adb shell dumpsys meminfo com.flamapp | grep TOTAL
```

**CPU:**
```bash
adb shell top | grep flamapp
```

### Debug native code

1. **Enable native debugging**
   - Run → Edit Configurations
   - Debugger → Debug Type → Dual (Java + Native)

2. **Set breakpoints in C++**
   - Click left margin in C++ file
   - Run in debug mode

3. **Use Android Studio profiler**
   - View → Tool Windows → Profiler

## 📊 Performance Optimization

### Reduce memory usage

1. **Reuse buffers**
   ```cpp
   // Don't allocate new Mat every frame
   static Mat outputMat;
   ```

2. **Use smaller data types**
   ```cpp
   Mat gray(height, width, CV_8UC1);  // Not CV_32F
   ```

3. **Release temporary objects**
   ```cpp
   edges.release();
   ```

### Improve FPS

1. **Profile code**
   ```cpp
   auto start = std::chrono::high_resolution_clock::now();
   // ... processing ...
   auto end = std::chrono::high_resolution_clock::now();
   auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(end - start);
   LOGD("Processing took %lld ms", duration.count());
   ```

2. **Optimize algorithms**
   - Reduce blur kernel size
   - Adjust Canny thresholds
   - Skip frames if needed

3. **Use NEON optimizations**
   ```cmake
   set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -mfpu=neon")
   ```

## 🔐 Security Issues

### Issue: Cleartext traffic not permitted

**Symptoms:**
```
java.io.IOException: Cleartext HTTP traffic not permitted
```

**Solutions:**

1. **Add network security config**
   ```xml
   <!-- AndroidManifest.xml -->
   <application
       android:networkSecurityConfig="@xml/network_security_config">
   ```

2. **Create config file**
   ```xml
   <!-- res/xml/network_security_config.xml -->
   <?xml version="1.0" encoding="utf-8"?>
   <network-security-config>
       <base-config cleartextTrafficPermitted="true"/>
   </network-security-config>
   ```

## 🆘 Getting Help

If issues persist:

1. **Check logs**
   ```bash
   adb logcat > logcat.txt
   ```

2. **Create GitHub issue**
   - Include error message
   - Attach logcat.txt
   - Describe steps to reproduce

3. **Community resources**
   - Stack Overflow: Tag with [android-opencv]
   - OpenCV Forums: https://forum.opencv.org/
   - Android Developers: https://developer.android.com/

## 📝 Useful Commands Reference

```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Uninstall
adb uninstall com.flamapp

# View logs
adb logcat -c  # Clear logs
adb logcat | grep -E "FlamApp|OpenCV"

# Check device info
adb shell getprop ro.build.version.sdk  # API level
adb shell dumpsys | grep OpenGL

# Screen recording (for demos)
adb shell screenrecord /sdcard/demo.mp4
```

---

**Still stuck? Open an issue on GitHub with detailed error logs!**
