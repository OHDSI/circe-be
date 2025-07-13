use std::ffi::{CStr, CString};
use std::os::raw::c_char;
use std::sync::Mutex;

// Global mutex to serialize access to the native library
static NATIVE_CALL_MUTEX: Mutex<()> = Mutex::new(());

#[cfg(test)]
mod tests;

#[cfg(test)]
mod build_test;

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

#[repr(C)]
pub struct IsolateThread {
    _private: [u8; 0],
}

// Wrapper to make IsolateThread pointer Send + Sync
struct IsolateThreadPtr(*mut IsolateThread);
unsafe impl Send for IsolateThreadPtr {}
unsafe impl Sync for IsolateThreadPtr {}

// External functions from GraalVM native library
extern "C" {
    fn graal_create_isolate(params: *const std::os::raw::c_void, isolate: *mut *mut std::os::raw::c_void, thread: *mut *mut IsolateThread) -> std::os::raw::c_int;
    fn graal_tear_down_isolate(thread: *mut IsolateThread) -> std::os::raw::c_int;
    fn circe_build_cohort_sql(thread: *mut IsolateThread, json_expression: *const c_char, options: *const c_char) -> *mut c_char;
    fn circe_check_cohort_expression(thread: *mut IsolateThread, json_expression: *const c_char) -> *mut c_char;
}

// Global isolate thread - this should be initialized once
static ISOLATE_THREAD: Mutex<Option<IsolateThreadPtr>> = Mutex::new(None);

/// Initialize the GraalVM isolate
fn ensure_isolate_initialized() -> Result<*mut IsolateThread, CirceError> {
    let mut isolate_guard = ISOLATE_THREAD.lock()
        .map_err(|_| CirceError::InitializationError("Failed to acquire isolate mutex".to_string()))?;
    
    if let Some(ref thread) = *isolate_guard {
        return Ok(thread.0);
    }
    
    unsafe {
        let mut isolate: *mut std::os::raw::c_void = std::ptr::null_mut();
        let mut thread: *mut IsolateThread = std::ptr::null_mut();
        
        let result = graal_create_isolate(std::ptr::null(), &mut isolate, &mut thread);
        if result != 0 {
            return Err(CirceError::InitializationError(format!("Failed to create GraalVM isolate: {}", result)));
        }
        
        *isolate_guard = Some(IsolateThreadPtr(thread));
        Ok(thread)
    }
}

/// Reset the GraalVM isolate (useful for test cleanup)
fn reset_isolate() -> Result<(), CirceError> {
    let mut isolate_guard = ISOLATE_THREAD.lock()
        .map_err(|_| CirceError::InitializationError("Failed to acquire isolate mutex".to_string()))?;
    
    // Clean up existing isolate if it exists
    if let Some(thread_ptr) = isolate_guard.take() {
        unsafe {
            let result = graal_tear_down_isolate(thread_ptr.0);
            if result != 0 {
                eprintln!("Warning: Failed to tear down isolate: {}", result);
            }
        }
    }
    
    // Create a new isolate
    unsafe {
        let mut isolate: *mut std::os::raw::c_void = std::ptr::null_mut();
        let mut thread: *mut IsolateThread = std::ptr::null_mut();
        
        let result = graal_create_isolate(std::ptr::null(), &mut isolate, &mut thread);
        if result != 0 {
            return Err(CirceError::InitializationError(format!("Failed to create new GraalVM isolate: {}", result)));
        }
        
        *isolate_guard = Some(IsolateThreadPtr(thread));
        Ok(())
    }
}

/// Call GraalVM native library to build cohort SQL
fn call_java_build_cohort_sql(expression: &str, options: &str) -> Result<String, CirceError> {
    let _guard = NATIVE_CALL_MUTEX.lock()
        .map_err(|_| CirceError::ProcessError("Failed to acquire native call mutex".to_string()))?;
    
    let thread = ensure_isolate_initialized()?;
    
    let c_expression = CString::new(expression)
        .map_err(|e| CirceError::ProcessError(format!("Invalid expression: {}", e)))?;
    
    let c_options = CString::new(options)
        .map_err(|e| CirceError::ProcessError(format!("Invalid options: {}", e)))?;

    unsafe {
        let result_ptr = circe_build_cohort_sql(thread, c_expression.as_ptr(), c_options.as_ptr());
        
        if result_ptr.is_null() {
            return Err(CirceError::ProcessError("Native function returned null".to_string()));
        }
        
        let result_cstr = CStr::from_ptr(result_ptr);
        let result = result_cstr.to_str()
            .map_err(|e| CirceError::ProcessError(format!("Invalid UTF-8 from native library: {}", e)))?
            .to_string();
        
        // Note: GraalVM manages its own memory, so we skip manual free to avoid double-free issues
        // graal_free_string(thread, result_ptr);
        
        Ok(result)
    }
}

/// Call GraalVM native library to check cohort expression
fn call_java_check_cohort_expression(expression: &str) -> Result<String, CirceError> {
    let _guard = NATIVE_CALL_MUTEX.lock()
        .map_err(|_| CirceError::ProcessError("Failed to acquire native call mutex".to_string()))?;
    
    let thread = ensure_isolate_initialized()?;
    
    let c_expression = CString::new(expression)
        .map_err(|e| CirceError::ProcessError(format!("Invalid expression: {}", e)))?;

    unsafe {
        let result_ptr = circe_check_cohort_expression(thread, c_expression.as_ptr());
        
        if result_ptr.is_null() {
            return Err(CirceError::ProcessError("Native function returned null".to_string()));
        }
        
        let result_cstr = CStr::from_ptr(result_ptr);
        let result = result_cstr.to_str()
            .map_err(|e| CirceError::ProcessError(format!("Invalid UTF-8 from native library: {}", e)))?
            .to_string();
        
        // Note: GraalVM manages its own memory, so we skip manual free to avoid double-free issues
        // graal_free_string(thread, result_ptr);
        
        Ok(result)
    }
}

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

/// Initialize the Circe environment
pub fn init_jvm() -> Result<(), CirceError> {
    // For GraalVM native library mode, ensure isolate is initialized
    ensure_isolate_initialized()?;
    Ok(())
}

/// Build an expression query from a cohort definition JSON
pub fn build_expression_query(
    expression_json: &str,
    options: Option<&BuildExpressionQueryOptions>,
) -> Result<String, CirceError> {
    let options_json = options.map(|o| o.to_json()).unwrap_or_else(|| "{}".to_string());
    call_java_build_cohort_sql(expression_json, &options_json)
}

/// Render and translate SQL to target database dialect
pub fn render_and_translate_sql(sql: &str, target_dialect: &str) -> Result<String, CirceError> {
    // For now, just return the SQL with a comment about the dialect
    // In a full implementation, this would use SqlRender
    Ok(format!("-- Rendered for {} dialect\n{}", target_dialect, sql))
}

/// Validate a cohort expression JSON
pub fn validate_cohort_expression(expression_json: &str) -> Result<String, CirceError> {
    call_java_check_cohort_expression(expression_json)
}

/// Validate a concept set expression JSON
pub fn validate_concept_set_expression(expression_json: &str) -> Result<String, CirceError> {
    // For now, use the same validation as cohort expression
    // In a full implementation, this would call a specific concept set validation function
    call_java_check_cohort_expression(expression_json)
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
    Ok("1.12.1-SNAPSHOT".to_string())
}

/// Reset the GraalVM isolate (for test cleanup)
pub fn reset_native_library() -> Result<(), CirceError> {
    reset_isolate()
}
