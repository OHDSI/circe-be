# Circe Rust Library

A Rust library that provides an interface to the Java Circe library for OMOP Common Data Model cohort definition processing.

## Overview

This library exposes the key functionality from the Java Circe library through a Rust interface, specifically:

- **`build_expression_query`**: Equivalent to `CohortExpressionQueryBuilder.buildExpressionQuery(expression, options)`
- **`render_and_translate_sql`**: Equivalent to `SqlRender.renderSql(SqlTranslate.translateSql(cohortSql, "postgresql"), null, null)`
- **`validate_cohort_expression`**: Validate cohort definition JSON
- **`validate_concept_set_expression`**: Validate concept set expression JSON

## Architecture

The library supports two implementation approaches:

### 1. Current Implementation (CLI-based)
- Uses the Java CLI (`circe-cli.jar`) via process calls
- Provides basic SQL transformation for different database dialects
- Works immediately without additional dependencies
- Suitable for development and testing

### 2. Full Implementation (JNI-based)
- Direct JVM integration using JNI (Java Native Interface)
- Calls Java methods directly for maximum performance
- Requires `jni` crate and full OHDSI dependencies
- Production-ready implementation

## Usage

### Basic Example

```rust
use circe::{init_jvm, build_expression_query, BuildExpressionQueryOptions};

// Initialize the environment
init_jvm()?;

// Define cohort expression
let expression = r#"{
    "title": "My Cohort",
    "primaryCriteria": {
        "criteriaList": [],
        "observationWindow": {"priorDays": 0, "postDays": 0},
        "primaryLimit": {"type": "First"}
    },
    "conceptSets": [],
    "qualifiedLimit": {"type": "First"},
    "expressionLimit": {"type": "First"},
    "inclusionRules": [],
    "collapseSettings": {"collapseType": "ERA", "eraPad": 0}
}"#;

// Configure options
let options = BuildExpressionQueryOptions {
    cohort_id: Some(1),
    cdm_schema: Some("cdm".to_string()),
    vocabulary_schema: Some("cdm".to_string()),
    generate_stats: false,
    ..Default::default()
};

// Build the SQL query
let sql = build_expression_query(expression, options)?;
println!("Generated SQL: {}", sql);
```

### SQL Rendering Example

```rust
use circe::render_and_translate_sql;

let sql = "SELECT * FROM @cdm_database_schema.person WHERE person_id = @person_id";
let rendered = render_and_translate_sql(sql, "postgresql")?;
println!("Rendered SQL: {}", rendered);
// Output: SELECT * FROM cdm.person WHERE person_id = @person_id
```

### Command Line Interface

The library includes a CLI application:

```bash
# Test the library functionality
./target/debug/circe-rust-wrapper test-build-query
./target/debug/circe-rust-wrapper test-sql-render
./target/debug/circe-rust-wrapper test-validation

# Build cohort query from JSON
./target/debug/circe-rust-wrapper build-query '{"title":"Test"}' cdm postgresql

# Render SQL for different dialects
./target/debug/circe-rust-wrapper render-sql 'SELECT * FROM @cdm.person' postgresql

# Validate expressions
./target/debug/circe-rust-wrapper validate-cohort '{"title":"Test Cohort"}'
```

## Building

### Prerequisites

1. **Java 8+** with Maven
2. **Rust** (latest stable)
3. Built Circe JAR file

### Build Steps

```bash
# Build the Java components first
mvn package -Dmaven.test.skip=true

# Build the Rust library
cargo build --release

# Run tests
cargo test
```

## API Reference

### Functions

#### `init_jvm() -> Result<(), CirceError>`
Initialize the Java environment. Must be called before using other functions.

#### `build_expression_query(expression_json: &str, options: BuildExpressionQueryOptions) -> Result<String, CirceError>`
Build a SQL query from a cohort expression JSON.

**Parameters:**
- `expression_json`: JSON string containing the cohort expression
- `options`: Configuration options for query building

**Returns:** Generated SQL query string

#### `render_and_translate_sql(sql: &str, target_dialect: &str) -> Result<String, CirceError>`
Transform SQL for a specific database dialect.

**Parameters:**
- `sql`: Input SQL with template parameters
- `target_dialect`: Target database dialect ("postgresql", "sql server", "oracle")

**Returns:** Transformed SQL string

#### `validate_cohort_expression(expression_json: &str) -> Result<String, CirceError>`
Validate a cohort definition JSON.

#### `validate_concept_set_expression(expression_json: &str) -> Result<String, CirceError>`
Validate a concept set expression JSON.

### Types

#### `BuildExpressionQueryOptions`
Configuration options for building expression queries.

```rust
pub struct BuildExpressionQueryOptions {
    pub cohort_id_field_name: Option<String>,
    pub cohort_id: Option<i32>,
    pub cdm_schema: Option<String>,
    pub target_table: Option<String>,
    pub result_schema: Option<String>,
    pub vocabulary_schema: Option<String>,
    pub generate_stats: bool,
}
```

#### `CirceError`
Error type for library operations.

```rust
pub enum CirceError {
    JsonError(String),
    JavaException(String),
    InitializationError(String),
    ProcessError(String),
}
```

## Supported Database Dialects

- **PostgreSQL**: Default transformations for PostgreSQL syntax
- **SQL Server**: Bracket notation for schema/table names
- **Oracle**: Uppercase transformation and Oracle-specific syntax

## Testing

The library includes comprehensive tests that mirror the Java test suite:

```bash
# Run all tests
cargo test

# Run specific test categories
cargo test integration_tests
cargo test benchmark_tests
```

## Limitations (Current Implementation)

1. **Full CohortExpressionQueryBuilder not available**: The current implementation uses validation as a proxy since the full OHDSI dependencies (including SqlRender) are not available in the CI environment.

2. **Basic SQL transformation**: Without SqlRender, only basic parameter substitution is performed.

3. **CLI-based approach**: Process overhead for each operation.

## Migration to Full Implementation

To enable the full JNI-based implementation:

1. **Add dependencies** to `Cargo.toml`:
   ```toml
   [dependencies]
   jni = "0.21"
   serde = { version = "1.0", features = ["derive"] }
   serde_json = "1.0"
   ```

2. **Restore OHDSI dependencies** in `pom.xml`:
   ```xml
   <dependency>
     <groupId>org.ohdsi.sql</groupId>
     <artifactId>SqlRender</artifactId>
     <version>1.6.8</version>
   </dependency>
   ```

3. **Uncomment JNI code**: The library includes commented JNI implementation ready for activation.

## Examples

See the `src/tests.rs` file for comprehensive examples of library usage, including:
- Basic expression query building
- SQL rendering for multiple dialects
- Error handling
- Performance benchmarking

## Contributing

When contributing to this library:

1. Maintain compatibility with both CLI-based and JNI-based implementations
2. Add tests for new functionality
3. Update documentation for API changes
4. Follow Rust best practices for error handling and memory safety