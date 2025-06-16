#!/bin/bash
# GraalVM Native Image Build Script for Circe
set -e

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
NATIVE_IMAGE_NAME="circe-cli-native"

echo "Project directory: $PROJECT_DIR"
echo "JAR file: $JAR_FILE"
echo "Output: $TARGET_DIR/$NATIVE_IMAGE_NAME"

# Check if JAR exists
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found at $JAR_FILE"
    echo "Please run 'mvn package -DskipTests' first."
    exit 1
fi

echo ""
echo "Building native image..."
echo "This may take several minutes..."

# Build shared library (.so/.dll/.dylib) with GraalVM
echo "Building shared library..."
native-image \
    --no-fallback \
    --shared \
    --enable-all-security-services \
    --allow-incomplete-classpath \
    --report-unsupported-elements-at-runtime \
    -H:+ReportExceptionStackTraces \
    -H:ConfigurationFileDirectories="$PROJECT_DIR/graalvm-config" \
    -H:Name=circe-native-lib \
    -jar "$JAR_FILE" \
    "$TARGET_DIR/libcirce-native"

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Shared library build successful!"
    
    # Check which shared library was created
    if [ -f "$TARGET_DIR/libcirce-native.so" ]; then
        echo "📚 Shared library created: $TARGET_DIR/libcirce-native.so"
    elif [ -f "$TARGET_DIR/libcirce-native.dylib" ]; then
        echo "📚 Shared library created: $TARGET_DIR/libcirce-native.dylib"
    elif [ -f "$TARGET_DIR/libcirce-native.dll" ]; then
        echo "📚 Shared library created: $TARGET_DIR/libcirce-native.dll"
    fi
    echo ""
else
    echo ""
    echo "❌ Shared library build failed!"
    echo "Please check the error messages above."
    exit 1
fi
fi