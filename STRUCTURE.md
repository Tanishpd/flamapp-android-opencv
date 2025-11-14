# Project Directory Structure

```
flamapp/
│
├── README.md                          # Main project documentation
├── SETUP.md                           # Detailed setup instructions
├── GIT_COMMIT_PLAN.md                 # Git workflow guide
├── .gitignore                         # Git ignore rules
├── settings.gradle                    # Gradle settings
├── build.gradle                       # Root build configuration
├── local.properties                   # Local SDK/NDK paths
│
├── app/                               # Android application
│   ├── build.gradle                   # App build configuration
│   ├── proguard-rules.pro            # ProGuard rules
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml   # App manifest
│           ├── java/com/flamapp/
│           │   ├── CameraActivity.java      # Main camera activity
│           │   ├── GLRenderer.java          # OpenGL renderer
│           │   └── NativeProcessor.java     # JNI bridge
│           └── res/
│               ├── layout/
│               │   └── activity_camera.xml  # UI layout
│               ├── values/
│               │   ├── strings.xml          # String resources
│               │   ├── colors.xml           # Color resources
│               │   └── themes.xml           # App themes
│               └── mipmap/                  # App icons
│
├── jni/                               # Native C++ code
│   ├── CMakeLists.txt                # CMake build config
│   ├── native_processor.h            # Processor header
│   └── native_processor.cpp          # OpenCV processing
│
├── gl/                                # OpenGL ES renderer
│   ├── renderer.h                    # Renderer header
│   ├── renderer.cpp                  # Renderer implementation
│   └── shaders/
│       ├── vertex.glsl               # Vertex shader
│       └── fragment.glsl             # Fragment shader
│
└── web/                               # TypeScript web viewer
    ├── package.json                  # NPM configuration
    ├── tsconfig.json                 # TypeScript config
    ├── .gitignore                    # Web-specific ignores
    ├── README.md                     # Web viewer docs
    ├── index.html                    # Main HTML page
    └── src/
        └── main.ts                   # TypeScript logic
```

## Component Descriptions

### Android App (`/app`)
- **CameraActivity.java**: Manages Camera2 API, captures frames, displays FPS
- **GLRenderer.java**: OpenGL ES 2.0 renderer for texture display
- **NativeProcessor.java**: JNI interface to native C++ code

### Native Processing (`/jni`)
- **native_processor.cpp**: OpenCV C++ implementation
  - Canny edge detection
  - Grayscale conversion
  - RGBA frame processing

### OpenGL Renderer (`/gl`)
- **renderer.cpp**: Native OpenGL rendering
- **vertex.glsl**: Vertex shader for quad rendering
- **fragment.glsl**: Fragment shader for texture sampling

### Web Viewer (`/web`)
- **main.ts**: TypeScript viewer logic
- **index.html**: Web interface with canvas display
- Displays processed frames, FPS, and resolution

## Build Outputs

```
flamapp/
├── app/build/
│   ├── intermediates/
│   │   └── cmake/           # Native library builds
│   └── outputs/
│       └── apk/             # APK files
└── web/dist/                # Compiled TypeScript
```

## Key Files to Configure

1. **local.properties** - Set Android SDK/NDK paths
2. **jni/CMakeLists.txt** - Configure OpenCV paths
3. **app/build.gradle** - Android build settings
4. **web/tsconfig.json** - TypeScript compiler options

## Data Flow

```
Camera → YUV Frame
    ↓
RGB Conversion
    ↓
JNI Bridge → native_processor.cpp
    ↓
OpenCV Processing (Canny/Grayscale)
    ↓
RGBA Buffer
    ↓
GLRenderer → OpenGL Texture
    ↓
Display on Screen
```
