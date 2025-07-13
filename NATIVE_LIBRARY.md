# Prebuilt Native Library

This project includes a prebuilt GraalVM native library for Linux x86_64 systems, so users don't need to build the native components themselves.

## What's Included

The `native-libs/linux-x86_64/` directory contains:

- `libcirce-native.so` - The GraalVM native library that provides OHDSI Circe functionality
- `libcirce-native.h` - The C header file for the native library functions

## How It Works

The build system automatically detects and uses the prebuilt native library:

1. **Priority Order**: The build script looks for libraries in this order:
   - Development builds in `target/` (for developers rebuilding locally)
   - Prebuilt libraries in `native-libs/linux-x86_64/` (for end users)

2. **Automatic Configuration**: The `build.rs` script automatically configures Rust to link against the appropriate library

3. **No Manual Steps Required**: Users can simply run `cargo build` or `cargo test` and everything works

## Supported Platforms

Currently, only Linux x86_64 is supported with a prebuilt library. For other platforms, you'll need to build the native library manually.

## Building the Native Library Manually

If you need to rebuild the native library (for development or other platforms):

1. Install GraalVM with native-image support
2. Run: `mvn package -DskipTests`
3. Run: `./build-graalvm.sh`
4. The library will be created in `target/libcirce-native.so`

## GitHub Action for Building Native Library

For maintainers, there's a GitHub Action that can build and check in the native library automatically:

1. Go to the "Actions" tab in the GitHub repository
2. Select "Build and Check-in Native Library"
3. Click "Run workflow"
4. Optionally customize the commit message
5. The action will:
   - Build the native library using GraalVM
   - Copy it to `native-libs/linux-x86_64/`
   - Create or update the header file
   - Commit and push the changes to the repository

This action is useful when updating dependencies or making changes that require rebuilding the native library.

## Memory Management

The native library uses GraalVM's automatic memory management. The Rust wrapper automatically handles:

- Creating and managing GraalVM isolates
- Converting between Rust and C strings
- Proper cleanup without double-free issues

## Testing

All functionality is thoroughly tested. Run tests with:

```bash
cargo test -- --test-threads=1
```

Note: Tests are run single-threaded to avoid GraalVM isolate conflicts.

## Library Functions

The native library provides these core functions:

- `circe_build_cohort_sql` - Generate SQL from cohort definition JSON
- `circe_check_cohort_expression` - Validate cohort definition JSON
- GraalVM isolate management functions

These are automatically called by the high-level Rust API functions like `build_expression_query()` and `validate_cohort_expression()`.
