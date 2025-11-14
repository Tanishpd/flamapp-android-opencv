# FlamApp Setup Guide

Complete step-by-step instructions for building and running FlamApp.

## Prerequisites

### Required Software

1. **Android Studio** (Arctic Fox or later)
   - Download: https://developer.android.com/studio
   - Install Android SDK, NDK, and CMake

2. **Android NDK** (r21 or later)
   - Install via Android Studio SDK Manager
   - Or download: https://developer.android.com/ndk/downloads

3. **OpenCV Android SDK** (4.5.0 or later)
   - Download: https://opencv.org/releases/
   - Extract the archive

4. **Node.js** (14+ for web viewer)
   - Download: https://nodejs.org/

## Part 1: Android Setup

### Step 1: Download OpenCV

```bash
cd ~/Downloads
wget https://github.com/opencv/opencv/releases/download/4.8.0/opencv-4.8.0-android-sdk.zip
unzip opencv-4.8.0-android-sdk.zip
```

### Step 2: Configure OpenCV in Project

**Option A: Copy OpenCV to project**
```bash
cd flamapp
mkdir -p app/src/main/jniLibs/opencv
cp -r ~/Downloads/OpenCV-android-sdk/sdk/native app/src/main/jniLibs/opencv/
```

**Option B: Use system-wide OpenCV**
Edit `jni/CMakeLists.txt` and set:
```cmake
set(OpenCV_DIR "/path/to/OpenCV-android-sdk/sdk/native/jni")
```

### Step 3: Configure local.properties

Edit `local.properties`:
```properties
sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk
ndk.dir=/Users/YOUR_USERNAME/Library/Android/sdk/ndk/25.1.8937393
```

### Step 4: Build Native Libraries

```bash
cd flamapp
./gradlew assembleDebug
```

### Step 5: Install on Device

```bash
# Connect Android device with USB debugging enabled
adb devices

# Install APK
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Troubleshooting Android Build

**OpenCV not found:**
```bash
# Verify OpenCV path
ls app/src/main/jniLibs/opencv/native/jni

# Should see: abi-*/
```

**NDK build error:**
```bash
# Check NDK version
ls $ANDROID_HOME/ndk/

# Use compatible version (21+)
```

**CMake error:**
```bash
# Install CMake via SDK Manager
# Android Studio > Preferences > SDK Manager > SDK Tools > CMake
```

## Part 2: Web Viewer Setup

### Step 1: Install Dependencies

```bash
cd web
npm install
```

### Step 2: Build TypeScript

```bash
npm run build
```

### Step 3: Run Local Server

**Option A: Using npx http-server**
```bash
npx http-server . -p 8000
```

**Option B: Using Python**
```bash
python3 -m http.server 8000
```

**Option C: Using serve**
```bash
npx serve . -l 8000
```

### Step 4: Open in Browser

```
http://localhost:8000
```

## Part 3: Testing

### Test Android App

1. Launch app on device
2. Grant camera permission
3. Verify live camera feed appears
4. Check FPS counter in top-left
5. Tap "Toggle Mode" to switch between Canny/Grayscale

### Test Web Viewer

1. Open http://localhost:8000
2. Click "Load Sample Frame"
3. Verify processed frame displays
4. Click "Toggle Mode" to see different processing
5. Check FPS and resolution values update

## Part 4: Advanced Configuration

### Adjust Camera Resolution

Edit `app/src/main/java/com/flamapp/CameraActivity.java`:
```java
private static final int PREVIEW_WIDTH = 1280;
private static final int PREVIEW_HEIGHT = 720;
```

### Change OpenCV Processing Parameters

Edit `jni/native_processor.cpp`:
```cpp
// Canny thresholds
Canny(gray, edges, 50, 150);  // Adjust 50, 150

// Gaussian blur
GaussianBlur(gray, gray, Size(5, 5), 1.5);  // Adjust kernel size
```

### Optimize Performance

**Build release APK:**
```bash
./gradlew assembleRelease
```

**Enable ProGuard:**
Edit `app/build.gradle`:
```gradle
buildTypes {
    release {
        minifyEnabled true
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt')
    }
}
```

## Part 5: Deployment

### Generate Signed APK

1. Android Studio > Build > Generate Signed Bundle/APK
2. Create new keystore or use existing
3. Fill in keystore details
4. Select release build variant
5. Build APK

### Deploy Web Viewer

**Option A: GitHub Pages**
```bash
cd web
npm run build
# Push dist/ folder to gh-pages branch
```

**Option B: Netlify**
```bash
cd web
npm run build
netlify deploy --dir=. --prod
```

**Option C: Vercel**
```bash
cd web
npm run build
vercel --prod
```

## Common Issues

### Camera Permission Denied
- Settings > Apps > FlamApp > Permissions > Enable Camera

### Black Screen
- Check camera2 API support: `adb shell getprop ro.hardware.camera`
- Try different camera ID in code

### Low FPS
- Reduce camera resolution
- Use release build
- Optimize OpenCV parameters

### Native Library Not Found
```bash
# Check library was built
ls app/build/intermediates/cmake/debug/obj/

# Should see: arm64-v8a/libflamapp_native.so
```

### OpenGL Rendering Issues
- Check EGL version: Device must support OpenGL ES 2.0+
- Verify in AndroidManifest.xml

## Performance Benchmarks

Target performance:
- **FPS**: 10-15 on mid-range devices (2019+)
- **Processing Time**: 30-50ms per frame
- **Memory**: ~50-100MB RAM usage
- **Battery**: Moderate drain (camera intensive)

## Development Tips

### Debug Native Code

```bash
# Enable native debugging in Android Studio
# Run > Edit Configurations > Debugger > Debug Type > Dual
```

### View Native Logs

```bash
adb logcat | grep -E "(FlamApp|OpenCV|GLRenderer)"
```

### Profile Performance

```bash
# Use Android Profiler in Android Studio
# View > Tool Windows > Profiler
```

## Next Steps

- [ ] Add video recording functionality
- [ ] Implement more OpenCV algorithms
- [ ] Add WebSocket for real-time frame streaming
- [ ] Create iOS version
- [ ] Add ML model integration

## Support

For issues:
1. Check logs: `adb logcat`
2. Verify OpenCV installation
3. Test on different device
4. Open GitHub issue with logs

## Resources

- Android Camera2 API: https://developer.android.com/training/camera2
- OpenCV Android: https://docs.opencv.org/4.x/d5/df8/tutorial_dev_with_OCV_on_Android.html
- OpenGL ES 2.0: https://www.khronos.org/opengles/
- TypeScript: https://www.typescriptlang.org/

---

**Happy Coding! 🚀**
