Circe-be
========

[![Build Status](https://travis-ci.com/OHDSI/circe-be.svg?branch=master)](https://travis-ci.com/OHDSI/circe-be) [![codecov.io](http://codecov.io/github/OHDSI/circe-be/coverage.svg?branch=master)](http://codecov.io/github/OHDSI/circe-be?branch=master)

Introduction
============
A Java library used to create queries for the OMOP Common Data Model v5.0-v5.3.  These queries are used in cohort definitions (CohortExpression) as well as custom features (CriteriaFeature).

Features
========
- Defines an object model for domain-specific queries (eg: Condition occurrence), correlated criteria (at least N records between X days before and Y days after) and concept set expressions
- Defines an object model for cohort definitions (CohortExpression) where the cohort is defined by Cohort Entry Events, Inclusion Rules and Cohort Exit Strategy.  Resulting episodes are combined into non-overlapping periods of time.
- Generate Negative Control cohorts from a Concept Set expression
- Import a cohort expression from a JSON file and rehydrates the CohortExpression object.
- A suite of unit tests using an embedded instance of Postgres 9.6 to validate proper specification.

Getting Started
===============

## Java Library

Add dependency (and repository) to maven:
```
  <repositories>    
    <repository>
      <id>ohdsi</id>
      <name>repo.ohdsi.org</name>
      <url>http://repo.ohdsi.org:8085/nexus/content/repositories/releases</url>
    </repository>
  </repositories>
  <dependencies>
    <dependency>
      <groupId>org.ohdsi</groupId>
      <artifactId>circe</artifactId>
      <version>1.9.0</version>
    </dependency>
  </dependencies>    
```

## Rust Library

The Rust wrapper provides a native interface to the Circe library functionality. You can use it as a library in your Rust projects or as a standalone CLI tool.

### Using as a Rust Library (No Build Required)

#### Method 1: Direct Git Dependency (Recommended)

Add this to your `Cargo.toml`:

```toml
[dependencies]
circe = { git = "https://github.com/p-hoffmann/circe-be.git", package = "circe-rust-wrapper" }
```

This method automatically downloads and builds the library for you - no manual building required!

#### Method 2: Download Pre-built Release

1. Go to the [Releases page](https://github.com/p-hoffmann/circe-be/releases)
2. Download the latest `circe-rust-wrapper-package.tar.gz`
3. Extract: `tar -xzf circe-rust-wrapper-*.tar.gz`
4. Run the installation script: `./install.sh`
5. Add as a local dependency:

```toml
[dependencies]
circe = { path = "/path/to/extracted/circe-rust-wrapper" }
```

#### Method 3: GitHub Packages (Alternative)

For advanced users who want to use GitHub's package registry:

First, add to your `~/.cargo/config.toml`:

```toml
[registries.github]
index = "sparse+https://github.com/p-hoffmann/circe-be.git"

[net]
git-fetch-with-cli = true
```

Then in your `Cargo.toml`:

```toml
[dependencies]
circe-rust-wrapper = { version = "0.1.0", registry = "github" }
```

### Example Usage

```rust
use circe_rust_wrapper::{
    build_expression_query, render_and_translate_sql, 
    BuildExpressionQueryOptions, init_jvm
};

fn main() -> Result<(), Box<dyn std::error::Error>> {
    // Initialize the library (ensures Java components are available)
    init_jvm()?;

    // Build a cohort expression query
    let expression_json = r#"
    {
        "title": "Sample Cohort",
        "description": "A sample cohort definition",
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

    let options = BuildExpressionQueryOptions {
        cohort_id: Some(1),
        cdm_schema: Some("cdm".to_string()),
        result_schema: Some("results".to_string()),
        vocabulary_schema: Some("vocab".to_string()),
        generate_stats: true,
        ..Default::default()
    };

    // Build the expression query (equivalent to builder.buildExpressionQuery)
    let sql_query = build_expression_query(expression_json, options)?;
    println!("Generated SQL: {}", sql_query);

    // Render and translate SQL to PostgreSQL 
    // (equivalent to SqlRender.renderSql(SqlTranslate.translateSql(...)))
    let translated_sql = render_and_translate_sql(&sql_query, "postgresql")?;
    println!("Translated SQL: {}", translated_sql);

    Ok(())
}
```

### Available Functions

The Rust library exposes the following main functions:

- **`build_expression_query(expression_json, options)`** - Equivalent to `builder.buildExpressionQuery(expression, options)`
- **`render_and_translate_sql(sql, target_dialect)`** - Equivalent to `SqlRender.renderSql(SqlTranslate.translateSql(cohortSql, "postgresql"), null, null)`
- **`validate_cohort_expression(expression_json)`** - Validate a cohort expression
- **`validate_concept_set_expression(expression_json)`** - Validate a concept set expression

### CLI Usage

You can also use the library as a command-line tool:

```bash
# Build and run
cargo build --release

# Test basic functionality
./target/release/circe-rust-wrapper test-validation
./target/release/circe-rust-wrapper test-sql-render

# Or use the helper script
./test-cli-wrapper.sh
```

### Full Cargo.toml Example (No Build Required)

Here's a complete example of using the library in your project:

```toml
[package]
name = "my-circe-project"
version = "0.1.0"
edition = "2021"

[dependencies]
# Method 1: Direct Git dependency (easiest, no manual building needed)
circe = { git = "https://github.com/p-hoffmann/circe-be.git", package = "circe-rust-wrapper" }

# Optional: for JSON handling in your application
serde = { version = "1.0", features = ["derive"] }
serde_json = "1.0"

[[bin]]
name = "my-cohort-analyzer"
path = "src/main.rs"
```

### Alternative Cargo.toml for Local Installation

If you've downloaded and extracted a release package:

```toml
[package]
name = "my-circe-project"
version = "0.1.0"
edition = "2021"

[dependencies]
# Method 2: Local path (after downloading and extracting release)
circe = { path = "../circe-rust-wrapper" }

# Optional dependencies
serde = { version = "1.0", features = ["derive"] }
serde_json = "1.0"
```

### Alternative Cargo.toml for GitHub Packages

For users who prefer package registries:

```toml
[package]
name = "my-circe-project"
version = "0.1.0"
edition = "2021"

[dependencies]
# Method 3: GitHub Packages (requires registry configuration)
circe-rust-wrapper = { version = "0.1.0", registry = "github" }

# Optional dependencies
serde = { version = "1.0", features = ["derive"] }
serde_json = "1.0"
```

**Note:** With Method 1 (Git dependency), Cargo automatically handles downloading and building the library. You don't need to manually build anything!

### Requirements

- **Java 17+** - The library requires Java to run the underlying Circe components
- **Maven** - For building the Java components (`mvn package`)
- **Rust 1.70+** - For building the Rust wrapper

### Building from Source

```bash
# Clone the repository
git clone https://github.com/p-hoffmann/circe-be.git
cd circe-be

# Build Java components
mvn package -DskipTests

# Build Rust library
cargo build --release

# Run tests
cargo test
```

License
=======
Circe is licensed under Apache License 2.0
