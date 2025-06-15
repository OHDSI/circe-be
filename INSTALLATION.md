# Circe Rust Wrapper - Installation Guide

This document provides detailed instructions for using the circe-rust-wrapper library in your Rust projects.

## Option 1: Git Dependency (Recommended - No Build Required)

This is the easiest method. Add this to your `Cargo.toml`:

```toml
[dependencies]
circe = { git = "https://github.com/p-hoffmann/circe-be.git", package = "circe-rust-wrapper" }
```

**Advantages:**
- ✅ No manual building required
- ✅ Always gets the latest version
- ✅ Cargo handles everything automatically
- ✅ Works with `cargo build`, `cargo run`, etc.
- ✅ **New**: Automatically downloads native binaries from GitHub if needed

## Option 2: Release Package (For Offline/Controlled Environments)

1. **Download the Release:**
   - Go to [Releases](https://github.com/p-hoffmann/circe-be/releases)
   - Download `circe-rust-wrapper-package.tar.gz`

2. **Extract and Install:**
   ```bash
   tar -xzf circe-rust-wrapper-*.tar.gz
   cd circe-rust-wrapper/
   ./install.sh
   ```

3. **Use in Your Project:**
   ```toml
   [dependencies]
   circe = { path = "/path/to/extracted/circe-rust-wrapper", package = "circe-rust-wrapper" }
   ```

## Complete Example Project

Here's a complete working example:

### Cargo.toml
```toml
[package]
name = "my-cohort-analyzer"
version = "0.1.0"
edition = "2021"

[dependencies]
# Method 1: Git dependency (recommended)
circe = { git = "https://github.com/p-hoffmann/circe-be.git", package = "circe-rust-wrapper" }

# Optional: JSON handling
serde = { version = "1.0", features = ["derive"] }
serde_json = "1.0"
```

### src/main.rs
```rust
use circe::{
    build_expression_query, render_and_translate_sql, 
    BuildExpressionQueryOptions, init_jvm
};

fn main() -> Result<(), Box<dyn std::error::Error>> {
    // Initialize the library
    init_jvm()?;

    // Sample cohort definition
    let expression_json = r#"
    {
        "title": "My Sample Cohort",
        "description": "A cohort for demonstration",
        "expression": {
            "primaryCriteria": {
                "criteriaList": [],
                "observationWindow": {
                    "priorDays": 0,
                    "postDays": 0
                }
            }
        }
    }
    "#;

    // Configure build options
    let options = BuildExpressionQueryOptions {
        cohort_id: Some(1),
        cdm_schema: Some("cdm".to_string()),
        result_schema: Some("results".to_string()),
        generate_stats: true,
        ..Default::default()
    };

    // Build the SQL query
    let sql_query = build_expression_query(expression_json, options)?;
    println!("Generated SQL:\n{}", sql_query);

    // Translate to PostgreSQL
    let pg_sql = render_and_translate_sql(&sql_query, "postgresql")?;
    println!("PostgreSQL SQL:\n{}", pg_sql);

    Ok(())
}
```

## Requirements

### For Git Dependency Method:
- **Rust 1.70+** with Cargo
- **Internet connection** (for initial download only)
- **No Java or Maven required** (native implementation included)
- **curl or wget** (for automatic binary download if needed)

### For Release Package Method:
- **Rust 1.70+** with Cargo
- **No Java or Maven required** (native implementation included)

## Automatic Native Binary Download (New Feature)

Starting with this version, the library automatically downloads native binaries from the GitHub repository during the build process when they're not available locally. This provides several benefits:

### How It Works

1. **Local First**: Checks for native binaries in the project directory
2. **Auto-Download**: If not found, downloads from GitHub repository automatically
3. **Smart Caching**: Caches downloaded binaries in `~/.cargo/circe-native-cache/`
4. **Multiple Fallbacks**: Tries different branches and sources
5. **Graceful Degradation**: Falls back to system PATH if all else fails

### Cache Location

Downloaded binaries are cached in:
- `$CARGO_HOME/circe-native-cache/` (if CARGO_HOME is set)
- `~/.cargo/circe-native-cache/` (default)
- Current directory (fallback)

### Network Requirements

- **curl** or **wget** must be available on the system
- Internet connection required only for initial download
- Subsequent builds use cached binaries

## Build From Source (Development)

If you want to contribute or modify the library:

```bash
# Clone repository
git clone https://github.com/p-hoffmann/circe-be.git
cd circe-be

# Build Rust library (includes native implementation)
cargo build --release

# Run tests
cargo test
```

**Note:** No Java build step is required. The library includes a native implementation.
```

## Troubleshooting

### Network Issues with Git Dependencies
If your organization blocks Git access, use the release package method instead.

### Compilation Errors
Make sure you're using a compatible Rust version:
```bash
rustc --version  # Should be 1.70 or newer
```

## API Reference

### Core Functions
- `build_expression_query(json, options)` - Build SQL from cohort expression
- `render_and_translate_sql(sql, dialect)` - Transform SQL for different databases  
- `validate_cohort_expression(json)` - Validate cohort definitions
- `validate_concept_set_expression(json)` - Validate concept sets

### Supported SQL Dialects
- PostgreSQL (`"postgresql"`)
- SQL Server (`"sqlserver"` or `"sql server"`)
- Oracle (`"oracle"`)

See the [main README](README.md) for complete API documentation and examples.