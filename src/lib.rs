use std::ffi::{CStr, CString};
use std::os::raw::c_char;
use std::ptr;
use std::panic;
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

/// Helper function to safely convert C string to Rust string
unsafe fn c_str_to_string(c_str: *const c_char) -> Result<String, CirceError> {
    if c_str.is_null() {
        return Err(CirceError::NullPointer);
    }
    CStr::from_ptr(c_str)
        .to_str()
        .map(|s| s.to_string())
        .map_err(|e| CirceError::JsonError(format!("UTF-8 error: {}", e)))
}

/// Helper function to safely convert Rust string to C string
fn string_to_c_str(s: String) -> *mut c_char {
    match CString::new(s) {
        Ok(c_string) => c_string.into_raw(),
        Err(_) => ptr::null_mut(),
    }
}

/// Exported C function: Get library version
#[no_mangle]
pub extern "C" fn circe_get_version() -> *mut c_char {
    string_to_c_str("1.12.1-SNAPSHOT".to_string())
}

/// Exported C function: Build expression query from JSON
#[no_mangle]
pub extern "C" fn circe_build_expression_query(
    json_expression: *const c_char, 
    options: *const c_char
) -> *mut c_char {
    let result = panic::catch_unwind(|| {
        unsafe {
            let expression = match c_str_to_string(json_expression) {
                Ok(s) => s,
                Err(e) => return format!("Error reading expression: {}", e),
            };
            
            let options_str = if options.is_null() {
                String::new()
            } else {
                match c_str_to_string(options) {
                    Ok(s) => s,
                    Err(e) => return format!("Error reading options: {}", e),
                }
            };
            
            // Call Java-based functionality through JNI
            match call_java_build_cohort_sql(&expression, &options_str) {
                Ok(sql) => sql,
                Err(e) => format!("Error: {}", e),
            }
        }
    });
    
    match result {
        Ok(sql) => string_to_c_str(sql),
        Err(_) => string_to_c_str("Error: panic occurred in circe_build_expression_query".to_string()),
    }
}

/// Exported C function: Validate cohort expression
#[no_mangle]
pub extern "C" fn circe_validate_cohort_expression(json_expression: *const c_char) -> *mut c_char {
    let result = panic::catch_unwind(|| {
        unsafe {
            let expression = match c_str_to_string(json_expression) {
                Ok(s) => s,
                Err(e) => return format!("Error reading expression: {}", e),
            };
            
            // Call Java-based validation through JNI
            match call_java_check_cohort_expression(&expression) {
                Ok(result) => result,
                Err(e) => format!("Error: {}", e),
            }
        }
    });
    
    match result {
        Ok(result) => string_to_c_str(result),
        Err(_) => string_to_c_str("Error: panic occurred in circe_validate_cohort_expression".to_string()),
    }
}

/// Exported C function: Render and translate SQL (placeholder)
#[no_mangle]
pub extern "C" fn circe_render_and_translate_sql(
    sql: *const c_char,
    target_dialect: *const c_char
) -> *mut c_char {
    let result = panic::catch_unwind(|| {
        unsafe {
            let sql_str = match c_str_to_string(sql) {
                Ok(s) => s,
                Err(e) => return format!("Error reading SQL: {}", e),
            };
            
            let dialect = if target_dialect.is_null() {
                "sql server".to_string()
            } else {
                match c_str_to_string(target_dialect) {
                    Ok(s) => s,
                    Err(e) => return format!("Error reading dialect: {}", e),
                }
            };
            
            // For now, just return the original SQL with a comment
            format!("-- Translated for {}\n{}", dialect, sql_str)
        }
    });
    
    match result {
        Ok(sql) => string_to_c_str(sql),
        Err(_) => string_to_c_str("Error: panic occurred in circe_render_and_translate_sql".to_string()),
    }
}

/// Free memory allocated by the library
#[no_mangle]
pub extern "C" fn circe_free_string(ptr: *mut c_char) {
    if !ptr.is_null() {
        // This function is now a no-op, as memory is managed by the GraalVM library
    }
}

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
    #[link_name = "circe_free_string"]
    fn graal_free_string(thread: *mut IsolateThread, ptr: *mut c_char);
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
        
        // Free the memory allocated by the native library
        graal_free_string(thread, result_ptr);
        
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
        
        // Free the memory allocated by the native library
        graal_free_string(thread, result_ptr);
        
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
    // For GraalVM native library mode, no JVM initialization needed
    // The native library functions will be called directly via FFI
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
    // For now, use the same validation as cohort expression
    // In a full implementation, this would call a specific concept set validation function
    let c_expression = CString::new(expression_json)
        .map_err(|e| CirceError::JsonError(format!("Invalid expression JSON: {}", e)))?;
    
    unsafe {
        let result_ptr = circe_validate_cohort_expression(c_expression.as_ptr());
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

/// Reset the GraalVM isolate (for test cleanup)
pub fn reset_native_library() -> Result<(), CirceError> {
    reset_isolate()
}
