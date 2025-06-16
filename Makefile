# Makefile for Circe Native Library

.PHONY: all clean build-java build-shared test-shared install

# Default target
all: build-java build-shared

# Build the Java JAR
build-java:
	@echo "Building Java components..."
	mvn clean package -DskipTests
	@echo "Creating fat JAR with dependencies..."
	cp target/circe-cli.jar target/circe-shared.jar

# Build shared library only
build-shared: build-java
	@echo "Building shared library..."
	chmod +x build-graalvm.sh
	./build-graalvm.sh

# Build Rust shared library only (bypasses Java dependencies)
build-rust-shared:
	@echo "Building Rust shared library..."
	cargo build --release --features shared-lib --lib
	@echo "Rust shared library built at: target/release/libcirce.so"

# Test the shared library
test-shared: build-rust-shared
	@echo "Building and running shared library test..."
	gcc -o test/test_runner test/test_native_lib.c -L./target/release -lcirce -Wl,-rpath,./target/release
	./test/test_runner

# Test Rust wrapper with shared library mode  
test-rust-shared: build-shared
	@echo "Testing Rust wrapper (shared library mode)..."
	cargo test --features shared-lib

# Install to system (requires sudo)
install: build-rust-shared
	@echo "Installing to /usr/local..."
	sudo cp target/release/libcirce.so /usr/local/lib/
	sudo cp include/circe_native_lib.h /usr/local/include/
	sudo ldconfig 2>/dev/null || true

# Clean build artifacts
clean:
	@echo "Cleaning build artifacts..."
	mvn clean
	cargo clean
	rm -f test/test_runner
	rm -f test/test_native_lib.o

# Show build information
info:
	@echo "Circe Native Library Build Information"
	@echo "====================================="
	@echo "Java version: $(shell java -version 2>&1 | head -n 1)"
	@echo "Maven version: $(shell mvn -version 2>&1 | head -n 1)"
	@echo "GraalVM version: $(shell native-image --version 2>&1 | head -n 1)"
	@echo "Current directory: $(shell pwd)"
	@echo ""
	@echo "Available targets:"
	@echo "  all          - Build everything (default)"
	@echo "  build-java   - Build Java JAR only"
	@echo "  build-shared - Build shared library"
	@echo "  test-shared  - Test shared library"
	@echo "  test-rust-shared - Test Rust wrapper (shared library mode)"
	@echo "  install      - Install to system (requires sudo)"
	@echo "  clean        - Clean all build artifacts"
	@echo "  info         - Show this information"