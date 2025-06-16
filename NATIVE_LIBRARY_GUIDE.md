# Circe Native Library Guide

This document explains how to build and use the Circe native shared library for different integration scenarios.

## Build Mode

### Shared Library Mode
Builds a shared library (.so/.dll/.dylib) that can be linked directly into other programs:
```bash
# Build the shared library
make build-shared

# Or use the script directly
./build-graalvm.sh

# The shared library will be created as:
# Linux: target/libcirce-native.so
# macOS: target/libcirce-native.dylib  
# Windows: target/libcirce-native.dll
```

## Usage Examples

### C/C++ Integration
```c
#include "include/circe_native_lib.h"
#include <stdio.h>
#include <stdlib.h>

int main() {
    // Get version
    const char* version = circe_get_version();
    printf("Circe version: %s\n", version);
    
    // Build a query
    const char* cohort_json = "{\"expression\": {...}}";
    char* sql = circe_build_expression_query(cohort_json, NULL);
    if (sql) {
        printf("Generated SQL: %s\n", sql);
        free(sql);  // Free the returned string
    }
    
    return 0;
}
```

Compile with:
```bash
gcc -o my_app my_app.c -L./target -lcirce-native
```

### Rust Integration with Shared Library
```toml
# Cargo.toml
[dependencies]
circe-rust-wrapper = { path = "." }
```

```rust
use circe::*;

fn main() -> Result<(), CirceError> {
    // Initialize (loads and verifies the shared library)
    init_jvm()?;
    
    // Use the library
    let version = get_version()?;
    println!("Circe version: {}", version);
    
    let cohort_json = r#"{"expression": {...}}"#;
    let sql = build_expression_query(cohort_json, None)?;
    println!("Generated SQL: {}", sql);
    
    Ok(())
}
```

### Python Integration (via ctypes)
```python
import ctypes
import json

# Load the shared library
lib = ctypes.CDLL('./target/libcirce-native.so')

# Define function signatures
lib.circe_get_version.restype = ctypes.c_char_p
lib.circe_build_expression_query.argtypes = [ctypes.c_char_p, ctypes.c_char_p]
lib.circe_build_expression_query.restype = ctypes.c_char_p

# Use the library
version = lib.circe_get_version().decode('utf-8')
print(f"Circe version: {version}")

cohort_json = json.dumps({"expression": {...}})
sql = lib.circe_build_expression_query(cohort_json.encode('utf-8'), None)
if sql:
    print(f"Generated SQL: {sql.decode('utf-8')}")
```

### Node.js Integration (via ffi-napi)
```javascript
const ffi = require('ffi-napi');
const ref = require('ref-napi');

// Load the shared library
const lib = ffi.Library('./target/libcirce-native.so', {
  'circe_get_version': ['string', []],
  'circe_build_expression_query': ['string', ['string', 'string']],
  'circe_validate_cohort_expression': ['string', ['string']]
});

// Use the library
const version = lib.circe_get_version();
console.log(`Circe version: ${version}`);

const cohortJson = JSON.stringify({expression: {...}});
const sql = lib.circe_build_expression_query(cohortJson, null);
console.log(`Generated SQL: ${sql}`);
```

## API Functions

### Core Functions
- `circe_get_version()` - Returns library version string
- `circe_build_expression_query(json_expression, options)` - Build SQL from cohort definition
- `circe_render_and_translate_sql(sql, target_dialect)` - Translate SQL between database dialects
- `circe_validate_cohort_expression(json_expression)` - Validate cohort definition JSON
- `circe_validate_concept_set_expression(json_expression)` - Validate concept set JSON

### Memory Management
- All returned strings must be freed by the caller using `free()`
- The library manages its own internal memory

### Error Handling
- Functions return NULL on error
- Check return values before using them
- Error details may be included in the returned string (check for "Error:" prefix)

## Build Requirements

### For Shared Library:
- GraalVM with native-image support
- Maven for Java build
- C compiler for linking (gcc/clang)

## Supported Platforms
- Linux x86_64
- macOS (Apple Silicon and Intel)
- Windows x86_64

## Troubleshooting

### Library Not Found
Ensure the shared library is in:
1. The same directory as your executable
2. A directory in your system's library path (LD_LIBRARY_PATH, PATH, etc.)
3. Specified via rpath during compilation

### Symbol Not Found
Verify the library was built with the correct GraalVM configuration and includes all necessary reflection metadata.

### Runtime Errors
Check that:
1. The Java dependencies are included in the native image
2. The GraalVM configuration files are up to date
3. The library was built for the correct platform architecture
