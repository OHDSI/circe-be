#!/bin/bash
# Build Circe with Static Linking
set -e

echo "Building Circe with Static GraalVM Library"
echo "==========================================="

# Step 1: Build Java JAR
echo "Step 1: Building Java JAR..."
mvn package -DskipTests -q

# Step 2: Build GraalVM static library
echo "Step 2: Building GraalVM static library..."
./build-graalvm.sh static

# Step 3: Build Rust library with static linking
echo "Step 3: Building Rust library with static linking..."
export CIRCE_STATIC_LINK=1
cargo clean  # Clean to ensure fresh build
cargo build --release

echo ""
echo "✅ Static build complete!"
echo ""
echo "Built artifacts:"
echo "- Static library: target/libcirce-native.a"
echo "- Rust library: target/release/libcirce.so"
echo "- Rust rlib: target/release/libcirce.rlib"
echo ""
echo "The Rust library now contains the GraalVM functionality statically linked."
echo "You can distribute target/release/libcirce.so without libcirce-native.so dependency."
