#!/bin/bash
# Complete Static Distribution Build for Circe
set -e

echo "🔧 Building Circe for Static Distribution"
echo "=========================================="

BUILD_MODE="${1:-auto}"  # auto, static, embed

# Ensure we have the JAR
if [ ! -f "target/circe-cli.jar" ]; then
    echo "📦 Building Java JAR..."
    mvn package -DskipTests -q
fi

case "$BUILD_MODE" in
    "static")
        echo "🔗 Mode: True Static Linking"
        # Try to build actual static library
        ./build-graalvm.sh static
        if [ -f "target/libcirce-native.a" ]; then
            export CIRCE_STATIC_LINK=1
            echo "✅ Using static library"
        else
            echo "⚠️ Static library creation failed, falling back to embed mode"
            BUILD_MODE="embed"
        fi
        ;;
    "embed")
        echo "📦 Mode: Embedded Library"
        # Build shared library and embed it
        ./build-graalvm.sh shared
        cp build-embed.rs build.rs
        ;;
    *)
        echo "🤖 Mode: Auto-detect best option"
        # Try static first, fall back to embedded
        ./build-graalvm.sh static
        if [ -f "target/libcirce-native.a" ]; then
            echo "✅ Using static library"
            export CIRCE_STATIC_LINK=1
        else
            echo "📦 Falling back to embedded shared library"
            ./build-graalvm.sh shared
            cp build-embed.rs build.rs
        fi
        ;;
esac

# Build Rust library
echo "🦀 Building Rust library..."
cargo clean
cargo build --release

# Verify the result
echo ""
echo "✅ Build Complete!"
echo "==================="

if [ -f "target/release/libcirce.so" ]; then
    echo "📚 Rust library: target/release/libcirce.so"
    
    # Check dependencies
    echo "🔍 Library dependencies:"
    ldd target/release/libcirce.so | grep -E "(circe|graal)" || echo "   No GraalVM dependencies (good for static distribution!)"
    
    echo ""
    echo "Size comparison:"
    ls -lh target/release/libcirce.so
    if [ -f "target/libcirce-native.so" ]; then
        ls -lh target/libcirce-native.so
    fi
    
else
    echo "❌ Build failed - no Rust library generated"
    exit 1
fi

echo ""
echo "🚀 Distribution ready!"
echo "The Rust library can now be distributed without requiring separate GraalVM native library installation."
