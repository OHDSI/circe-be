# Circe GraalVM Native Build

This directory contains scripts and configuration for building Circe as a GraalVM native image.

## Prerequisites

1. **GraalVM**: Install GraalVM (version 17 or later recommended)
   ```bash
   # Download from https://www.graalvm.org/downloads/
   # Or use SDKMAN:
   sdk install java 17.0.7-graal
   ```

2. **Native Image**: Install the native-image component
   ```bash
   gu install native-image
   ```

## Building

1. **Build the JAR file**:
   ```bash
   mvn package -DskipTests
   ```

2. **Build the native image**:
   ```bash
   ./build-graalvm.sh
   ```

3. **Test the native image**:
   ```bash
   ./target/circe-cli-native version
   ./target/circe-cli-native validate-cohort '{"Title":"Test"}'
   ```

## Rust Integration

A Rust wrapper is provided to demonstrate how to use the native library:

1. **Build the Rust wrapper**:
   ```bash
   make all
   ```

2. **Test the Rust wrapper**:
   ```bash
   make test
   ```

## Files

- `build-graalvm.sh` - Main build script for GraalVM native image
- `graalvm-config/` - GraalVM configuration files
  - `reflect-config.json` - Reflection configuration
  - `resource-config.json` - Resource inclusion configuration
- `src/main.rs` - Rust wrapper example
- `Cargo.toml` - Rust project configuration
- `Makefile` - Build configuration for Rust wrapper

## Usage

The native image provides a CLI interface:

```bash
./target/circe-cli-native [command] [options]

Commands:
  validate-cohort <json>     Validate a cohort definition JSON
  validate-conceptset <json> Validate a concept set expression JSON
  version                    Show version information
```

## Benefits of Native Image

- **Fast startup**: Near-instantaneous startup time
- **Low memory usage**: Reduced memory footprint
- **No JVM required**: Self-contained executable
- **Native integration**: Can be called from Rust and other native code

## Configuration

The GraalVM configuration files are located in `graalvm-config/`:

- Reflection configuration for Jackson JSON processing
- Resource configuration for templates and SQL files
- Additional configuration can be added as needed

## Troubleshooting

If the build fails:
1. Check that GraalVM and native-image are properly installed
2. Ensure the JAR file was built successfully
3. Review the error messages for missing reflection configuration
4. Add additional configuration to `graalvm-config/` as needed