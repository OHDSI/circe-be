use std::ffi::{CStr, CString};
use std::os::raw::c_char;
use std::path::PathBuf;

#[cfg(test)]
mod tests;

#[cfg(test)]
mod build_test;

// Link to the native shared library functions
extern "C" {
    fn circe_build_expression_query(json_expression: *const c_char, options: *const c_char) -> *const c_char;
    fn circe_render_and_translate_sql(sql: *const c_char, target_dialect: *const c_char) -> *const c_char;
    fn circe_validate_cohort_expression(json_expression: *const c_char) -> *const c_char;
    fn circe_validate_concept_set_expression(json_expression: *const c_char) -> *const c_char;
    fn circe_get_version() -> *const c_char;
}

/// Get the path to the shared library
fn get_shared_library_path() -> Result<PathBuf, CirceError> {
    let library_name = if cfg!(target_os = "windows") {
        "libcirce-native.dll"
    } else if cfg!(target_os = "macos") {
        "libcirce-native.dylib"
    } else {
        "libcirce-native.so"
    };
    
    // Try various locations for the shared library
    let search_paths = vec![
        std::env::current_dir().ok().map(|d| d.join("target").join(library_name)),
        std::env::current_dir().ok().map(|d| d.join("native-binaries").join("linux-x86_64").join(library_name)),
        std::env::current_exe().ok().and_then(|e| e.parent().map(|p| p.join(library_name))),
    ];
    
    for path in search_paths.into_iter().flatten() {
        if path.exists() {
            return Ok(path);
        }
    }
    
    Err(CirceError::InitializationError(
        format!("Shared library {} not found", library_name)
    ))
}

/// Error type for Circe operations
#[derive(Debug)]
pub enum CirceError {
    JsonError(String),
    JavaException(String),
    InitializationError(String),
    ProcessError(String),
    NullPointer,
}

impl std::fmt::Display for CirceError {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            CirceError::JsonError(msg) => write!(f, "JSON error: {}", msg),
            CirceError::JavaException(msg) => write!(f, "Java exception: {}", msg),
            CirceError::InitializationError(msg) => write!(f, "Initialization error: {}", msg),
            CirceError::ProcessError(msg) => write!(f, "Process error: {}", msg),
            CirceError::NullPointer => write!(f, "Received null pointer from native library"),
        }
    }
}

impl std::error::Error for CirceError {}

/// Options for building expression queries
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

/// Helper function to convert C string to Rust string
unsafe fn c_str_to_rust_string(c_str: *const c_char) -> Result<String, CirceError> {
    if c_str.is_null() {
        return Err(CirceError::NullPointer);
    }
    
    let cstr = CStr::from_ptr(c_str);
    cstr.to_str()
        .map_err(|e| CirceError::ProcessError(format!("Invalid UTF-8: {}", e)))
        .map(|s| s.to_string())
}

/// Initialize the Circe environment
pub fn init_jvm() -> Result<(), CirceError> {
    // For shared library, just verify it exists and can be loaded
    let _lib_path = get_shared_library_path()?;
    // TODO: Actually load the library and verify symbols
    Ok(())
}

/// Build an expression query from a cohort definition JSON
pub fn build_expression_query(
    expression_json: &str,
    options: Option<&BuildExpressionQueryOptions>,
) -> Result<String, CirceError> {
    build_expression_query_shared_lib(expression_json, options)
}

/// Build expression query using shared library
fn build_expression_query_shared_lib(
    expression_json: &str,
    options: Option<&BuildExpressionQueryOptions>,
) -> Result<String, CirceError> {
    let c_expression = CString::new(expression_json)
        .map_err(|e| CirceError::JsonError(format!("Invalid expression JSON: {}", e)))?;
    
    let options_json = options.map(|o| o.to_json()).unwrap_or_else(|| "{}".to_string());
    let c_options = CString::new(options_json)
        .map_err(|e| CirceError::JsonError(format!("Invalid options JSON: {}", e)))?;
    
    unsafe {
        let result_ptr = circe_build_expression_query(c_expression.as_ptr(), c_options.as_ptr());
        c_str_to_rust_string(result_ptr)
    }
}

/// Render and translate SQL to target database dialect
pub fn render_and_translate_sql(sql: &str, target_dialect: &str) -> Result<String, CirceError> {
    let c_sql = CString::new(sql)
        .map_err(|e| CirceError::ProcessError(format!("Invalid SQL: {}", e)))?;
    let c_dialect = CString::new(target_dialect)
        .map_err(|e| CirceError::ProcessError(format!("Invalid dialect: {}", e)))?;
    
    unsafe {
        let result_ptr = circe_render_and_translate_sql(c_sql.as_ptr(), c_dialect.as_ptr());
        c_str_to_rust_string(result_ptr)
    }
}

/// Validate a cohort expression JSON
pub fn validate_cohort_expression(expression_json: &str) -> Result<String, CirceError> {
    let c_expression = CString::new(expression_json)
        .map_err(|e| CirceError::JsonError(format!("Invalid expression JSON: {}", e)))?;
    
    unsafe {
        let result_ptr = circe_validate_cohort_expression(c_expression.as_ptr());
        c_str_to_rust_string(result_ptr)
    }
}

/// Validate a concept set expression JSON
pub fn validate_concept_set_expression(expression_json: &str) -> Result<String, CirceError> {
    let c_expression = CString::new(expression_json)
        .map_err(|e| CirceError::JsonError(format!("Invalid expression JSON: {}", e)))?;
    
    unsafe {
        let result_ptr = circe_validate_concept_set_expression(c_expression.as_ptr());
        c_str_to_rust_string(result_ptr)
    }
}

/// Build and render cohort SQL in one call (convenience function)
pub fn build_and_render_cohort_sql(
    expression_json: &str,
    target_dialect: &str,
    options: Option<&BuildExpressionQueryOptions>,
) -> Result<String, CirceError> {
    let cohort_sql = build_expression_query(expression_json, options)?;
    render_and_translate_sql(&cohort_sql, target_dialect)
}

/// Get library version
pub fn get_version() -> Result<String, CirceError> {
    unsafe {
        let result_ptr = circe_get_version();
        c_str_to_rust_string(result_ptr)
    }
}
