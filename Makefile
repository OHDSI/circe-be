# Makefile for Circe Rust Wrapper
TARGET = circe-rust-wrapper

.PHONY: all clean test build release

all: build

build:
	cargo build

release:
	cargo build --release

clean:
	cargo clean

test: build
	@echo "Testing Rust wrapper..."
	@echo "Note: This requires the native image to be built first with './build-graalvm.sh'"
	@if [ -f "./target/circe-cli-native" ]; then \
		echo "Running version test:"; \
		./target/debug/$(TARGET) test-version; \
		echo ""; \
		echo "Running cohort test:"; \
		./target/debug/$(TARGET) test-cohort; \
		echo ""; \
		echo "Running concept set test:"; \
		./target/debug/$(TARGET) test-conceptset; \
	else \
		echo "Error: Native image not found at ./target/circe-cli-native"; \
		echo "Please run './build-graalvm.sh' first to build the native image."; \
	fi

test-compilation: build
	@echo "Testing Rust wrapper compilation..."
	@./target/debug/$(TARGET) 2>&1 | grep -q "Usage:" && echo "Rust wrapper compiled successfully"

help:
	@echo "Available targets:"
	@echo "  all     - Build the Rust wrapper (debug mode)"
	@echo "  build   - Build the Rust wrapper (debug mode)"
	@echo "  release - Build the Rust wrapper (release mode)"
	@echo "  clean   - Remove built files"
	@echo "  test    - Test the Rust wrapper (requires native image)"
	@echo "  test-compilation - Test that the wrapper compiles and shows usage"
	@echo "  help    - Show this help message"