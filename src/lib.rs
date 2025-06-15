use std::process::Command;
use std::path::PathBuf;

#[cfg(test)]
mod tests;

/// Get the path to the native executable
/// This function looks for the native executable in the expected locations
fn get_native_executable_path() -> Result<PathBuf, CirceError> {
    let native_binary_name = if cfg!(target_os = "windows") { "circe-cli-native.exe" } else { "circe-cli-native" };
    
    // First, try to find it in the same directory as the current executable (for bundled distribution)
    if let Ok(exe_path) = std::env::current_exe() {
        if let Some(exe_dir) = exe_path.parent() {
            let native_path = exe_dir.join(native_binary_name);
            if native_path.exists() {
                return Ok(native_path);
            }
        }
    }
    
    // Next, try the native-binaries directory (for development/packaging)
    if let Ok(current_dir) = std::env::current_dir() {
        // Try linux-x86_64 directory first (most common)
        let native_linux_path = current_dir.join("native-binaries").join("linux-x86_64").join(native_binary_name);
        if native_linux_path.exists() {
            return Ok(native_linux_path);
        }
        
        // Try to find any platform-specific directory
        if let Ok(native_dir) = std::fs::read_dir(current_dir.join("native-binaries")) {
            for entry in native_dir.flatten() {
                if entry.file_type().map(|ft| ft.is_dir()).unwrap_or(false) {
                    let platform_native_path = entry.path().join(native_binary_name);
                    if platform_native_path.exists() {
                        return Ok(platform_native_path);
                    }
                }
            }
        }
    }
    
    // Next, try the OUT_DIR from build (for cargo build/install)
    if let Ok(out_dir) = std::env::var("OUT_DIR") {
        let out_native_path = PathBuf::from(out_dir).join(native_binary_name);
        if out_native_path.exists() {
            return Ok(out_native_path);
        }
    }
    
    // Finally, try PATH (if installed globally)
    if let Ok(output) = Command::new("which").arg(native_binary_name).output() {
        if output.status.success() {
            let path_output = String::from_utf8_lossy(&output.stdout);
            let path_str = path_output.trim();
            if !path_str.is_empty() {
                return Ok(PathBuf::from(path_str));
            }
        }
    }
    
    // Return a default path for error reporting
    Ok(PathBuf::from(native_binary_name))
}

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

/// Initialize the Circe environment using the native executable
pub fn init_jvm() -> Result<(), CirceError> {
    // Check if the native executable exists
    let native_path = get_native_executable_path()?;

    if !native_path.exists() {
        return Err(CirceError::InitializationError(
            format!("circe-cli-native not found at {}. Native library may not be properly installed.", native_path.display())
        ));
    }

    // Test the native executable by running version command
    let output = Command::new(&native_path)
        .arg("version")
        .output()
        .map_err(|e| CirceError::InitializationError(format!("Failed to execute native library: {}", e)))?;

    if !output.status.success() {
        return Err(CirceError::InitializationError(
            "Native library failed to execute properly".to_string()
        ));
    }

    Ok(())
}

/// Build an expression query using CohortExpressionQueryBuilder
/// 
/// NOTE: This is a placeholder implementation that demonstrates the architecture.
/// In a full implementation with JNI dependencies available, this would directly
/// call the Java CohortExpressionQueryBuilder.buildExpressionQuery method.
pub fn build_expression_query(expression_json: &str, options: BuildExpressionQueryOptions) -> Result<String, CirceError> {
    // Use the native executable approach
    let native_path = get_native_executable_path()?;

    if !native_path.exists() {
        return Err(CirceError::InitializationError(
            format!("circe-cli-native not found at {}. Please ensure the native library is properly installed.", native_path.display())
        ));
    }

    // For demonstration, we'll validate the cohort (the closest thing our CLI can do)
    let output = Command::new(&native_path)
        .arg("validate-cohort")
        .arg(expression_json)
        .output()
        .map_err(|e| CirceError::ProcessError(format!("Failed to execute native command: {}", e)))?;

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

/// Validate a cohort definition JSON using the native CLI
pub fn validate_cohort_expression(expression_json: &str) -> Result<String, CirceError> {
    let native_path = get_native_executable_path()?;

    if !native_path.exists() {
        return Err(CirceError::InitializationError(
            format!("circe-cli-native not found at {}. Please ensure the native library is properly installed.", native_path.display())
        ));
    }

    let output = Command::new(&native_path)
        .arg("validate-cohort")
        .arg(expression_json)
        .output()
        .map_err(|e| CirceError::ProcessError(format!("Failed to execute native command: {}", e)))?;

    if !output.status.success() {
        let stderr = String::from_utf8_lossy(&output.stderr);
        return Err(CirceError::JavaException(format!("Validation failed: {}", stderr)));
    }

    Ok(String::from_utf8_lossy(&output.stdout).to_string())
}

/// Validate a concept set expression JSON using the Java CLI  
pub fn validate_concept_set_expression(expression_json: &str) -> Result<String, CirceError> {
    let native_path = get_native_executable_path()?;

    if !native_path.exists() {
        return Err(CirceError::InitializationError(
            format!("circe-cli-native not found at {}. Please ensure the native library is properly installed.", native_path.display())
        ));
    }

    let output = Command::new(&native_path)
        .arg("validate-conceptset")
        .arg(expression_json)
        .output()
        .map_err(|e| CirceError::ProcessError(format!("Failed to execute native command: {}", e)))?;

    if !output.status.success() {
        let stderr = String::from_utf8_lossy(&output.stderr);
        return Err(CirceError::JavaException(format!("Validation failed: {}", stderr)));
    }

    Ok(String::from_utf8_lossy(&output.stdout).to_string())
}