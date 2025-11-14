# FlamApp - Real-time Computer Vision Processing

Android application with native OpenCV C++ processing, OpenGL ES rendering, and TypeScript web viewer.

## 🎯 Features

- **Real-time Camera Processing**: Live camera feed with 10-15 FPS
- **Native OpenCV C++**: Canny edge detection and grayscale processing via JNI
- **OpenGL ES 2.0 Rendering**: Hardware-accelerated frame rendering
- **TypeScript Web Viewer**: Display processed frames with FPS and resolution info
- **Modular Architecture**: Clean separation of concerns

## 📐 Architecture

```
FlamApp/
├── app/               # Android Java/Kotlin source
│   ├── src/main/
│   │   ├── java/      # Camera, UI, JNI bridge
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── jni/               # C++ OpenCV processing
│   ├── native_processor.cpp
│   ├── native_processor.h
│   └── CMakeLists.txt
├── gl/                # OpenGL ES renderer
│   ├── renderer.cpp
│   ├── renderer.h
│   └── shaders/
│       ├── vertex.glsl
│       └── fragment.glsl
├── web/               # TypeScript viewer
│   ├── src/
│   │   └── main.ts
│   ├── index.html
│   └── tsconfig.json
└── README.md
```

## 🔄 Frame Pipeline

```
Camera2 API → YUV to RGB → JNI Bridge → OpenCV C++ Processing
                                          ↓
                                    RGBA Buffer
                                          ↓
                               OpenGL ES Texture → Render → Display
```

## 🚀 Setup Instructions

### Prerequisites

- Android Studio Arctic Fox or later
- Android NDK (r21 or later)
- CMake 3.10+
- OpenCV Android SDK 4.5.0+
- Node.js 14+ and npm (for TypeScript viewer)

### Android Setup

1. **Clone the repository**
   ```bash
   git clone <repo-url>
   cd flamapp
   ```

2. **Download OpenCV Android SDK**
   - Download from https://opencv.org/releases/
   - Extract to `app/src/main/jniLibs/opencv`
   - Or use the provided gradle dependency

3. **Configure NDK path**
   - Open `local.properties`
   - Add: `ndk.dir=/path/to/android-ndk`

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

### TypeScript Web Viewer Setup

1. **Navigate to web directory**
   ```bash
   cd web
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Build TypeScript**
   ```bash
   npm run build
   # or
   tsc
   ```

4. **Run locally**
   ```bash
   # Use any static server
   npx http-server .
   # or
   python3 -m http.server 8000
   ```

5. **Open in browser**
   ```
   http://localhost:8000
   ```

## 📱 Usage

### Android App

1. Launch the app
2. Grant camera permissions
3. View live processed feed
4. Toggle between raw and processed modes (optional)
5. FPS counter displays in top-left corner

### Web Viewer

1. Open `index.html` in browser
2. View static processed frame
3. Check FPS and resolution info

## 🎨 Processing Modes

- **Canny Edge Detection**: Detects edges in the camera feed
- **Grayscale**: Converts to grayscale with intensity mapping

## 🔧 Configuration

### Android

Edit `app/src/main/java/com/flamapp/CameraActivity.java`:
```java
// Change processing mode
private static final int PROCESSING_MODE = 1; // 0=grayscale, 1=canny
```

### Native Processing

Edit `jni/native_processor.cpp`:
```cpp
// Adjust Canny thresholds
cv::Canny(gray, edges, 50, 150);
```

### OpenGL

Edit `gl/renderer.cpp` for texture parameters and rendering pipeline.

## 📊 Performance

- Target FPS: 10-15 FPS
- Resolution: 640x480 (configurable)
- Processing Time: ~30-50ms per frame
- Rendering: Hardware-accelerated via OpenGL ES 2.0

## 📝 Git Workflow

### Initial Commit Plan

```bash
git init
git add .
git commit -m "feat: initial project structure"
git commit -m "feat: add Android camera capture"
git commit -m "feat: implement JNI bridge"
git commit -m "feat: add OpenCV C++ processing"
git commit -m "feat: implement OpenGL ES renderer"
git commit -m "feat: add TypeScript web viewer"
git commit -m "docs: add comprehensive README"
```

## 🐛 Troubleshooting

### Camera not working
- Check camera permissions in AndroidManifest.xml
- Grant runtime permissions on Android 6.0+

### OpenCV not linking
- Verify OpenCV SDK path in `CMakeLists.txt`
- Check ABI compatibility (arm64-v8a, armeabi-v7a)

### Low FPS
- Reduce camera resolution
- Optimize OpenCV processing parameters
- Use release build instead of debug

### OpenGL rendering issues
- Check EGL context creation
- Verify shader compilation
- Ensure texture format matches (RGBA8888)

## 📄 License

MIT License - feel free to use in your projects

## 👨‍💻 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'feat: add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📧 Contact

For issues and questions, please open a GitHub issue.

---

**Built with ❤️ using Android NDK, OpenCV, OpenGL ES, and TypeScript**
