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

# Build native image with GraalVM
native-image \
    --no-fallback \
    --enable-all-security-services \
    --allow-incomplete-classpath \
    --report-unsupported-elements-at-runtime \
    -H:+ReportExceptionStackTraces \
    -H:ConfigurationFileDirectories="$PROJECT_DIR/graalvm-config" \
    -jar "$JAR_FILE" \
    "$TARGET_DIR/$NATIVE_IMAGE_NAME"

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Native image build successful!"
    echo "Executable created: $TARGET_DIR/$NATIVE_IMAGE_NAME"
    echo ""
    echo "File size comparison:"
    echo "JAR file: $(du -h "$JAR_FILE" | cut -f1)"
    echo "Native image: $(du -h "$TARGET_DIR/$NATIVE_IMAGE_NAME" | cut -f1)"
    echo ""
    echo "Test the native image:"
    echo "$TARGET_DIR/$NATIVE_IMAGE_NAME version"
else
    echo "❌ Native image build failed!"
    exit 1
fi