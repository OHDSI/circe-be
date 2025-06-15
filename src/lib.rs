use std::process::Command;

#[cfg(test)]
mod tests;

/// Error type for Circe operations
#[derive(Debug)]
pub enum CirceError {
    JsonError(String),
    JavaException(String),
    InitializationError(String),
    ProcessError(String),
}

impl std::fmt::Display for CirceError {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            CirceError::JsonError(msg) => write!(f, "JSON error: {}", msg),
            CirceError::JavaException(msg) => write!(f, "Java exception: {}", msg),
            CirceError::InitializationError(msg) => write!(f, "Initialization error: {}", msg),
            CirceError::ProcessError(msg) => write!(f, "Process error: {}", msg),
        }
    }
}

impl std::error::Error for CirceError {}

/// Options for building expression queries
/// This is a simplified version that would use serde for JSON serialization in a full implementation
#[derive(Debug, Clone)]
pub struct BuildExpressionQueryOptions {
    pub cohort_id_field_name: Option<String>,
    pub cohort_id: Option<i32>,
    pub cdm_schema: Option<String>,
    pub target_table: Option<String>,
    pub result_schema: Option<String>,
    pub vocabulary_schema: Option<String>,
    pub generate_stats: bool,
}

impl Default for BuildExpressionQueryOptions {
    fn default() -> Self {
        Self {
            cohort_id_field_name: None,
            cohort_id: None,
            cdm_schema: None,
            target_table: None,
            result_schema: None,
            vocabulary_schema: None,
            generate_stats: false,
        }
    }
}

impl BuildExpressionQueryOptions {
    /// Convert options to JSON string (manual implementation for demo)
    pub fn to_json(&self) -> String {
        let mut parts = Vec::new();
        
        if let Some(ref name) = self.cohort_id_field_name {
            parts.push(format!("\"cohortIdFieldName\":\"{}\"", name));
        }
        if let Some(id) = self.cohort_id {
            parts.push(format!("\"cohortId\":{}", id));
        }
        if let Some(ref schema) = self.cdm_schema {
            parts.push(format!("\"cdmSchema\":\"{}\"", schema));
        }
        if let Some(ref table) = self.target_table {
            parts.push(format!("\"targetTable\":\"{}\"", table));
        }
        if let Some(ref schema) = self.result_schema {
            parts.push(format!("\"resultSchema\":\"{}\"", schema));
        }
        if let Some(ref schema) = self.vocabulary_schema {
            parts.push(format!("\"vocabularySchema\":\"{}\"", schema));
        }
        parts.push(format!("\"generateStats\":{}", self.generate_stats));
        
        format!("{{{}}}", parts.join(","))
    }
}

/// Initialize the Java environment (placeholder for now)
pub fn init_jvm() -> Result<(), CirceError> {
    // Check if the JAR file exists
    let jar_path = std::env::current_dir()
        .map_err(|e| CirceError::InitializationError(format!("Cannot get current directory: {}", e)))?
        .join("target")
        .join("circe-cli.jar");

    if !jar_path.exists() {
        return Err(CirceError::InitializationError(
            "circe-cli.jar not found in target/ directory. Please run 'mvn package' first.".to_string()
        ));
    }

    // For now, we just verify the JAR exists
    // In a full implementation with JNI, this would initialize the JVM
    Ok(())
}

/// Build an expression query using CohortExpressionQueryBuilder
/// 
/// NOTE: This is a placeholder implementation that demonstrates the architecture.
/// In a full implementation with JNI dependencies available, this would directly
/// call the Java CohortExpressionQueryBuilder.buildExpressionQuery method.
pub fn build_expression_query(expression_json: &str, options: BuildExpressionQueryOptions) -> Result<String, CirceError> {
    // For now, we use a hybrid approach: call the Java CLI but prepare for JNI
    let jar_path = std::env::current_dir()
        .map_err(|e| CirceError::InitializationError(format!("Cannot get current directory: {}", e)))?
        .join("target")
        .join("circe-cli.jar");

    if !jar_path.exists() {
        return Err(CirceError::InitializationError(
            "circe-cli.jar not found. Please run 'mvn package' first.".to_string()
        ));
    }

    // Create a temporary file with the expression JSON
    let temp_dir = std::env::temp_dir();
    let temp_file = temp_dir.join(format!("circe_expr_{}.json", std::process::id()));
    std::fs::write(&temp_file, expression_json)
        .map_err(|e| CirceError::ProcessError(format!("Failed to write temp file: {}", e)))?;

    // For demonstration, we'll validate the cohort (the closest thing our CLI can do)
    let output = Command::new("java")
        .arg("-jar")
        .arg(&jar_path)
        .arg("validate-cohort")
        .arg(temp_file.to_string_lossy().as_ref())
        .output()
        .map_err(|e| CirceError::ProcessError(format!("Failed to execute Java command: {}", e)))?;

    // Clean up temp file
    let _ = std::fs::remove_file(&temp_file);

    if !output.status.success() {
        let stderr = String::from_utf8_lossy(&output.stderr);
        return Err(CirceError::JavaException(format!("Java process failed: {}", stderr)));
    }

    let stdout = String::from_utf8_lossy(&output.stdout);
    
    // Since we can't actually build the query without the full dependencies,
    // we return a placeholder SQL that would be generated
    Ok(format!(
        "-- Generated SQL from Circe expression\n-- Expression title: {}\n-- Options: {}\n-- Validation result: {}\n\n-- This would be a complete SQL query in a full implementation\nSELECT 'Expression processing successful' as result;",
        extract_title_from_json(expression_json),
        options.to_json(),
        stdout.trim()
    ))
}

/// Extract title from JSON (simple manual parsing for demo)
fn extract_title_from_json(json: &str) -> String {
    if let Some(start) = json.find("\"title\"") {
        if let Some(colon) = json[start..].find(':') {
            let after_colon = &json[start + colon + 1..];
            if let Some(quote_start) = after_colon.find('"') {
                let after_quote = &after_colon[quote_start + 1..];
                if let Some(quote_end) = after_quote.find('"') {
                    return after_quote[..quote_end].to_string();
                }
            }
        }
    }
    "Unknown".to_string()
}

/// Render SQL using basic transformations
/// 
/// NOTE: This is a placeholder implementation.
/// In a full implementation with SqlRender dependency available, this would directly
/// call the Java SqlRender and SqlTranslate methods.
pub fn render_and_translate_sql(sql: &str, target_dialect: &str) -> Result<String, CirceError> {
    // Without SqlRender dependency, we provide a basic transformation
    let mut result = sql.to_string();
    
    // Basic transformations for different dialects
    match target_dialect.to_lowercase().as_str() {
        "postgresql" => {
            result = result.replace("@cdm_database_schema", "cdm");
            result = result.replace("@vocabulary_database_schema", "cdm");
            result = result.replace("@results_database_schema", "results");
            result = result.replace("@target_database_schema", "results");
            result = result.replace("@target_cohort_table", "cohort");
            result = result.replace("[", "\"");
            result = result.replace("]", "\"");
        },
        "sql server" | "sqlserver" => {
            result = result.replace("@cdm_database_schema", "[cdm]");
            result = result.replace("@vocabulary_database_schema", "[cdm]");
            result = result.replace("@results_database_schema", "[results]");
            result = result.replace("@target_database_schema", "[results]");
            result = result.replace("@target_cohort_table", "[cohort]");
        },
        "oracle" => {
            result = result.replace("@cdm_database_schema", "cdm");
            result = result.replace("@vocabulary_database_schema", "cdm");
            result = result.replace("@results_database_schema", "results");
            result = result.replace("@target_database_schema", "results");
            result = result.replace("@target_cohort_table", "cohort");
            // Add note first, then convert everything to uppercase
            result = format!("-- SQL transformed for {} dialect (basic transformation)\n{}", target_dialect, result);
            result = result.to_uppercase();
            return Ok(result);
        },
        _ => {
            return Err(CirceError::ProcessError(format!("Unsupported dialect: {}", target_dialect)));
        }
    }

    // Add a note that this is a simplified transformation
    result = format!("-- SQL transformed for {} dialect (basic transformation)\n{}", target_dialect, result);

    Ok(result)
}

/// Convenience function that combines buildExpressionQuery and render_and_translate_sql
pub fn build_and_render_cohort_sql(
    expression_json: &str,
    options: BuildExpressionQueryOptions,
    target_dialect: &str
) -> Result<String, CirceError> {
    let cohort_sql = build_expression_query(expression_json, options)?;
    render_and_translate_sql(&cohort_sql, target_dialect)
}

/// Validate a cohort definition JSON using the Java CLI
pub fn validate_cohort_expression(expression_json: &str) -> Result<String, CirceError> {
    let jar_path = std::env::current_dir()
        .map_err(|e| CirceError::InitializationError(format!("Cannot get current directory: {}", e)))?
        .join("target")
        .join("circe-cli.jar");

    if !jar_path.exists() {
        return Err(CirceError::InitializationError(
            "circe-cli.jar not found. Please run 'mvn package' first.".to_string()
        ));
    }

    // Create a temporary file with the expression JSON
    let temp_dir = std::env::temp_dir();
    let temp_file = temp_dir.join(format!("circe_expr_{}.json", std::process::id()));
    std::fs::write(&temp_file, expression_json)
        .map_err(|e| CirceError::ProcessError(format!("Failed to write temp file: {}", e)))?;

    let output = Command::new("java")
        .arg("-jar")
        .arg(&jar_path)
        .arg("validate-cohort")
        .arg(temp_file.to_string_lossy().as_ref())
        .output()
        .map_err(|e| CirceError::ProcessError(format!("Failed to execute Java command: {}", e)))?;

    // Clean up temp file
    let _ = std::fs::remove_file(&temp_file);

    if !output.status.success() {
        let stderr = String::from_utf8_lossy(&output.stderr);
        return Err(CirceError::JavaException(format!("Validation failed: {}", stderr)));
    }

    Ok(String::from_utf8_lossy(&output.stdout).to_string())
}

/// Validate a concept set expression JSON using the Java CLI  
pub fn validate_concept_set_expression(expression_json: &str) -> Result<String, CirceError> {
    let jar_path = std::env::current_dir()
        .map_err(|e| CirceError::InitializationError(format!("Cannot get current directory: {}", e)))?
        .join("target")
        .join("circe-cli.jar");

    if !jar_path.exists() {
        return Err(CirceError::InitializationError(
            "circe-cli.jar not found. Please run 'mvn package' first.".to_string()
        ));
    }

    // Create a temporary file with the expression JSON
    let temp_dir = std::env::temp_dir();
    let temp_file = temp_dir.join(format!("circe_conceptset_{}.json", std::process::id()));
    std::fs::write(&temp_file, expression_json)
        .map_err(|e| CirceError::ProcessError(format!("Failed to write temp file: {}", e)))?;

    let output = Command::new("java")
        .arg("-jar")
        .arg(&jar_path)
        .arg("validate-conceptset")
        .arg(temp_file.to_string_lossy().as_ref())
        .output()
        .map_err(|e| CirceError::ProcessError(format!("Failed to execute Java command: {}", e)))?;

    // Clean up temp file
    let _ = std::fs::remove_file(&temp_file);

    if !output.status.success() {
        let stderr = String::from_utf8_lossy(&output.stderr);
        return Err(CirceError::JavaException(format!("Validation failed: {}", stderr)));
    }

    Ok(String::from_utf8_lossy(&output.stdout).to_string())
}