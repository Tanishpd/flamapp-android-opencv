#!/bin/bash

# Script to install Android NDK and CMake

echo "========================================="
echo "  Android NDK and CMake Installer"
echo "========================================="
echo ""

SDK_ROOT="/Users/tanishpd/Library/Android/sdk"

# Check if SDK exists
if [ ! -d "$SDK_ROOT" ]; then
    echo "❌ Android SDK not found at: $SDK_ROOT"
    echo "Please install Android SDK first."
    exit 1
fi

echo "✅ Android SDK found at: $SDK_ROOT"
echo ""

# Set up sdkmanager path
SDKMANAGER="$SDK_ROOT/cmdline-tools/latest/bin/sdkmanager"

# Check if sdkmanager exists
if [ ! -f "$SDKMANAGER" ]; then
    echo "⚠️  Command-line tools not found. Checking alternative locations..."
    
    # Try other possible locations
    if [ -d "$SDK_ROOT/cmdline-tools" ]; then
        LATEST_VERSION=$(ls -1 "$SDK_ROOT/cmdline-tools" | grep -v latest | sort -V | tail -n 1)
        if [ -n "$LATEST_VERSION" ]; then
            SDKMANAGER="$SDK_ROOT/cmdline-tools/$LATEST_VERSION/bin/sdkmanager"
        fi
    fi
    
    if [ ! -f "$SDKMANAGER" ]; then
        echo ""
        echo "❌ sdkmanager not found!"
        echo ""
        echo "You have two options:"
        echo ""
        echo "Option 1: Install via Android Studio (Recommended)"
        echo "  1. Open Android Studio"
        echo "  2. Go to: Android Studio → Settings (or Preferences on Mac)"
        echo "  3. Navigate to: Appearance & Behavior → System Settings → Android SDK"
        echo "  4. Click on the 'SDK Tools' tab"
        echo "  5. Check these boxes:"
        echo "     ✓ NDK (Side by side)"
        echo "     ✓ CMake"
        echo "  6. Click 'Apply' to download and install"
        echo ""
        echo "Option 2: Manual Installation"
        echo "  Download command-line tools from:"
        echo "  https://developer.android.com/studio#command-line-tools-only"
        echo ""
        exit 1
    fi
fi

echo "✅ Found sdkmanager at: $SDKMANAGER"
echo ""

# Accept licenses
echo "📄 Accepting Android SDK licenses..."
yes | "$SDKMANAGER" --licenses 2>&1 | grep -v "Warning:"

# Install NDK and CMake
echo ""
echo "📥 Installing Android NDK and CMake..."
echo "   This will download ~1GB of files. Please wait..."
echo ""

"$SDKMANAGER" "ndk;25.1.8937393" "cmake;3.22.1" 2>&1 | while IFS= read -r line; do
    echo "   $line"
done

# Check if installation was successful
if [ -d "$SDK_ROOT/ndk/25.1.8937393" ] && [ -f "$SDK_ROOT/ndk/25.1.8937393/source.properties" ]; then
    echo ""
    echo "✅ NDK installed successfully!"
    echo "   Location: $SDK_ROOT/ndk/25.1.8937393"
else
    echo ""
    echo "❌ NDK installation may have failed."
    echo ""
    echo "Please try Option 1 (Android Studio) instead:"
    echo "  1. Open Android Studio"
    echo "  2. Settings → Android SDK → SDK Tools tab"
    echo "  3. Check: NDK (Side by side) and CMake"
    echo "  4. Click Apply"
    exit 1
fi

if [ -d "$SDK_ROOT/cmake/3.22.1" ]; then
    echo "✅ CMake installed successfully!"
    echo "   Location: $SDK_ROOT/cmake/3.22.1"
else
    echo "⚠️  CMake installation may have failed, but may not be critical."
fi

echo ""
echo "========================================="
echo "✅ Installation Complete!"
echo "========================================="
echo ""
echo "Next steps:"
echo "  1. Run: ./gradlew clean"
echo "  2. Run: ./gradlew assembleDebug"
echo ""
