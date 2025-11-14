# 🚀 Quick Start Guide - FlamApp

## ⚡ Fast Track to Running FlamApp

### Current Status Check

Run this first to see what you need:
```bash
./setup-interactive.sh
```

---

## 📋 Prerequisites Checklist

### ✅ You Already Have:
- [x] Java 21 installed
- [x] Project files generated

### ⚠️ You Need to Install:

#### 1. **Android Studio** (REQUIRED)
```bash
# Download from:
https://developer.android.com/studio

# Install it, then:
# - Open Android Studio
# - Follow the setup wizard
# - Install Android SDK (it will be at ~/Library/Android/sdk)
```

#### 2. **Android SDK Components** (REQUIRED)
Open Android Studio → Preferences → Android SDK:

**SDK Platforms tab:**
- [x] Android 14.0 (API 34) - Check this

**SDK Tools tab:**
- [x] Android SDK Build-Tools
- [x] NDK (Side by side) - IMPORTANT!
- [x] CMake - IMPORTANT!
- [x] Android SDK Command-line Tools

Click "Apply" to install.

#### 3. **OpenCV Android SDK** (REQUIRED)
```bash
# Download OpenCV
cd ~/Downloads
curl -L -o opencv-android.zip \
  https://github.com/opencv/opencv/releases/download/4.8.0/opencv-4.8.0-android-sdk.zip

# Extract it
unzip opencv-android.zip

# Copy to project
cd /Users/tanishpd/Desktop/flamapp
mkdir -p app/src/main/jniLibs/opencv
cp -r ~/Downloads/OpenCV-android-sdk/sdk/native/* app/src/main/jniLibs/opencv/
```

---

## 🔨 Build Steps (Once Prerequisites are Ready)

### Step 1: Configure SDK Path
```bash
cd /Users/tanishpd/Desktop/flamapp

# The setup script will do this automatically:
./setup-interactive.sh
```

### Step 2: Build the App
```bash
# Clean build (recommended first time)
./gradlew clean

# Build debug APK
./gradlew assembleDebug
```

**Build time:** 2-5 minutes (first time with downloads)

---

## 📱 Install on Android Device

### Step 1: Prepare Your Device
1. Enable **Developer Options**:
   - Settings → About Phone → Tap "Build Number" 7 times

2. Enable **USB Debugging**:
   - Settings → Developer Options → USB Debugging → ON

3. Connect device via USB cable

### Step 2: Verify Connection
```bash
adb devices
# Should show your device
```

If `adb` not found:
```bash
export PATH="$PATH:~/Library/Android/sdk/platform-tools"
```

### Step 3: Install APK
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Step 4: Run the App
- Look for "FlamApp" on your device
- Tap to open
- Grant camera permission
- You should see live camera feed with processing!

---

## 🌐 Web Viewer (Optional)

```bash
cd web
npm install
npm run build
npx http-server . -p 8000
```

Open: http://localhost:8000

---

## ⚡ Quick Command Reference

```bash
# Full build sequence
cd /Users/tanishpd/Desktop/flamapp
./setup-interactive.sh              # Check prerequisites
./gradlew clean assembleDebug       # Build
adb devices                          # Check device
adb install -r app/build/outputs/apk/debug/app-debug.apk  # Install

# If build fails
./gradlew clean
rm -rf .gradle build app/build
./gradlew assembleDebug

# View logs while app runs
adb logcat | grep FlamApp
```

---

## 🐛 Common Issues

### Issue 1: "Android SDK not found"
**Solution:** Install Android Studio first, then run `./setup-interactive.sh`

### Issue 2: "NDK not found"
**Solution:** 
```bash
# Open Android Studio
# Preferences → SDK → SDK Tools → Check "NDK (Side by side)" → Apply
```

### Issue 3: "OpenCV headers not found"
**Solution:** Make sure OpenCV is copied to the right location:
```bash
ls app/src/main/jniLibs/opencv/jni/include/opencv2/
# Should show OpenCV headers
```

### Issue 4: Build takes forever
**First build downloads Gradle and dependencies (normal)**
- Wait 5-10 minutes
- Check internet connection
- Subsequent builds will be faster

---

## 📊 What to Expect

### Build Output:
```
BUILD SUCCESSFUL in 3m 45s
```

### App Features:
- ✅ Live camera preview
- ✅ Real-time processing (10-15 FPS)
- ✅ FPS counter (top-left)
- ✅ Toggle button (Canny/Grayscale)
- ✅ Material Design UI

### Performance:
- **FPS:** 10-15 on modern devices
- **Processing:** Canny edge detection or Grayscale
- **Resolution:** 640x480 (configurable)

---

## 🎯 Success Checklist

After following all steps, you should have:

- [ ] Android Studio installed
- [ ] SDK path configured in `local.properties`
- [ ] OpenCV copied to `app/src/main/jniLibs/opencv/`
- [ ] App builds without errors: `./gradlew assembleDebug`
- [ ] Device connected and recognized: `adb devices`
- [ ] APK installed on device
- [ ] App runs and shows camera feed
- [ ] FPS counter visible
- [ ] Processing modes work

---

## 💡 Tips

1. **First time?** Run `./setup-interactive.sh` - it checks everything!

2. **Clean build** if something goes wrong:
   ```bash
   ./gradlew clean
   rm -rf .gradle build app/build
   ```

3. **Check logs** if app crashes:
   ```bash
   adb logcat | grep -E "FlamApp|AndroidRuntime"
   ```

4. **Use release build** for better performance:
   ```bash
   ./gradlew assembleRelease
   ```

---

## 🆘 Need More Help?

- **Detailed Setup:** Read `SETUP.md`
- **Troubleshooting:** Read `TROUBLESHOOTING.md`
- **Architecture:** Read `STRUCTURE.md`
- **Overview:** Read `PROJECT_OVERVIEW.md`

---

## 🎉 Ready to Start?

Run this now:
```bash
./setup-interactive.sh
```

It will check your system and tell you exactly what to do next!

---

**Last Updated:** November 13, 2025
**Platform:** macOS
**Min Android:** API 24 (Android 7.0)
