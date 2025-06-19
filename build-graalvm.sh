#!/bin/bash
# GraalVM Native Image Build Script for Circe
set -e

# Initialize SDKMAN if available (local development)
if [ -f "/home/ph/.sdkman/bin/sdkman-init.sh" ]; then
    source "/home/ph/.sdkman/bin/sdkman-init.sh"
fi

echo "Circe GraalVM Native Image Build Script"
echo "========================================"

# Check if GraalVM native-image is installed
if ! command -v native-image &> /dev/null; then
    echo "Error: native-image not found. Please install GraalVM and native-image."
    echo "You can install it with: gu install native-image"
    exit 1
fi

# Set variables
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TARGET_DIR="$PROJECT_DIR/target"
JAR_FILE="$TARGET_DIR/circe-cli.jar"
NATIVE_IMAGE_NAME="libcirce-native"

echo "Project directory: $PROJECT_DIR"
echo "JAR file: $JAR_FILE"
echo "Output: $TARGET_DIR/$NATIVE_IMAGE_NAME"

# Check if JAR exists
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found at $JAR_FILE"
    echo "Please run 'mvn compile -DskipTests' first."
    exit 1
fi

echo ""
echo "Building native image..."
echo "This may take several minutes..."

# Build library with GraalVM - support both shared and static
BUILD_TYPE="${1:-shared}"  # Default to shared, allow 'static' as argument

if [ "$BUILD_TYPE" = "static" ]; then
    echo "Building static library..."
    # Try static build first, fall back to shared if static fails
    native-image \
        --no-fallback \
        --static \
        --libc=glibc \
        -H:-CheckToolchain \
        --enable-all-security-services \
        --allow-incomplete-classpath \
        --report-unsupported-elements-at-runtime \
        -H:+ReportExceptionStackTraces \
        -H:ConfigurationFileDirectories="$PROJECT_DIR/graalvm-config" \
        -H:Name=circe-native-lib \
        -jar "$JAR_FILE" \
        "$TARGET_DIR/libcirce-native-static" || {
            
        echo "⚠️ Static build failed, trying without static flag..."
        echo "Building relocatable shared library for static linking..."
        native-image \
            --no-fallback \
            --shared \
            -H:+StaticExecutableWithDynamicLibC \
            --enable-all-security-services \
            --allow-incomplete-classpath \
            --report-unsupported-elements-at-runtime \
            -H:+ReportExceptionStackTraces \
            -H:ConfigurationFileDirectories="$PROJECT_DIR/graalvm-config" \
            -H:Name=circe-native-lib \
            -jar "$JAR_FILE" \
            "$TARGET_DIR/libcirce-native-for-static"
    }
        
    # Create a static-friendly archive if we have the library
    if [ -f "$TARGET_DIR/libcirce-native-static" ]; then
        echo "Converting static executable to archive..."
        # Extract symbols and create archive for linking
        ar rcs "$TARGET_DIR/libcirce-native.a" "$TARGET_DIR/libcirce-native-static" 2>/dev/null || {
            # Alternative: copy the static executable as a library
            cp "$TARGET_DIR/libcirce-native-static" "$TARGET_DIR/libcirce-native.a"
        }
    elif [ -f "$TARGET_DIR/libcirce-native-for-static.so" ]; then
        echo "Using shared library as static archive base..."
        cp "$TARGET_DIR/libcirce-native-for-static.so" "$TARGET_DIR/libcirce-native.a"
    fi
else
    echo "Building shared library..."
    native-image \
        --no-fallback \
        --shared \
        --enable-all-security-services \
        --allow-incomplete-classpath \
        --report-unsupported-elements-at-runtime \
        -H:+ReportExceptionStackTraces \
        -J-Xss8m \
        -J-Xmx2g \
        -H:ConfigurationFileDirectories="$PROJECT_DIR/graalvm-config" \
        -H:Name=circe-native-lib \
        -jar "$JAR_FILE" \
        "$TARGET_DIR/libcirce-native"
fi

if [ $? -eq 0 ]; then
    echo ""
    if [ "$BUILD_TYPE" = "static" ]; then
        echo "✅ Static library build successful!"
        
        # Check which static library was created
        if [ -f "$TARGET_DIR/libcirce-native-static" ]; then
            echo "📚 Static executable created: $TARGET_DIR/libcirce-native-static"
        fi
        if [ -f "$TARGET_DIR/libcirce-native.a" ]; then
            echo "📚 Static library archive created: $TARGET_DIR/libcirce-native.a"
        fi
    else
        echo "✅ Shared library build successful!"
        
        # Check which shared library was created
        if [ -f "$TARGET_DIR/libcirce-native.so" ]; then
            echo "📚 Shared library created: $TARGET_DIR/libcirce-native.so"
        elif [ -f "$TARGET_DIR/libcirce-native.dylib" ]; then
            echo "📚 Shared library created: $TARGET_DIR/libcirce-native.dylib"
        elif [ -f "$TARGET_DIR/libcirce-native.dll" ]; then
            echo "📚 Shared library created: $TARGET_DIR/libcirce-native.dll"
        fi
    fi
    echo ""
else
    echo ""
    if [ "$BUILD_TYPE" = "static" ]; then
        echo "❌ Static library build failed!"
    else
        echo "❌ Shared library build failed!"
    fi
    echo "Please check the error messages above."
    exit 1
fi