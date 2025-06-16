#!/bin/bash
# GraalVM Environment Setup Script for Circe
# This script ensures GraalVM is available for building the native shared library

# Source SDKMAN
if [ -f "$HOME/.sdkman/bin/sdkman-init.sh" ]; then
    source "$HOME/.sdkman/bin/sdkman-init.sh"
    echo "✅ SDKMAN initialized"
else
    echo "❌ SDKMAN not found. Please install SDKMAN first."
    exit 1
fi

# Check if GraalVM is active
if java -version 2>&1 | grep -q "GraalVM"; then
    echo "✅ GraalVM is active:"
    java -version
    echo ""
else
    echo "⚠️  Switching to GraalVM..."
    sdk use java 21.0.2-graalce
    echo "✅ GraalVM activated:"
    java -version
    echo ""
fi

# Check native-image availability
if command -v native-image >/dev/null 2>&1; then
    echo "✅ native-image is available:"
    native-image --version
    echo ""
else
    echo "❌ native-image not found"
    exit 1
fi

# Display build information
echo "🔧 Circe Build Environment Ready!"
echo "=================================="
echo "Java: $(java -version 2>&1 | head -n 1)"
echo "Native Image: $(native-image --version 2>&1 | head -n 1)"
echo "Maven: $(mvn -version 2>&1 | head -n 1)"
echo ""
echo "Available commands:"
echo "  make info         - Show detailed build info"
echo "  make build-shared - Build native shared library"
echo "  make test-shared  - Test the shared library"
echo ""
echo "To build the shared library, run:"
echo "  make build-shared"
