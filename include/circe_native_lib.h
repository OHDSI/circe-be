#ifndef CIRCE_NATIVE_LIB_H
#define CIRCE_NATIVE_LIB_H

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Circe Native Library - C Interface
 * 
 * This header provides C function declarations for the Circe native library
 * built with GraalVM. The library exposes key functionality from the Java
 * Circe library for building OMOP CDM cohort queries.
 */

// Library version information
const char* circe_get_version();

/**
 * Build an expression query from a cohort definition JSON
 * 
 * @param json_expression - JSON string containing the cohort expression
 * @param options - JSON string containing build options (can be NULL for defaults)
 * @return SQL query string (caller must free the returned string)
 */
char* circe_build_expression_query(const char* json_expression, const char* options);

/**
 * Render and translate SQL to target database dialect
 * 
 * @param sql - SQL query string to translate
 * @param target_dialect - Target database dialect ("postgresql", "sql server", etc.)
 * @return Translated SQL string (caller must free the returned string)
 */
char* circe_render_and_translate_sql(const char* sql, const char* target_dialect);

/**
 * Validate a cohort expression JSON
 * 
 * @param json_expression - JSON string containing the cohort expression
 * @return JSON string with validation results (caller must free the returned string)
 */
char* circe_validate_cohort_expression(const char* json_expression);

/**
 * Validate a concept set expression JSON
 * 
 * @param json_expression - JSON string containing the concept set expression
 * @return JSON string with validation results (caller must free the returned string)
 */
char* circe_validate_concept_set_expression(const char* json_expression);

/**
 * Free memory allocated by the library
 * Note: For GraalVM native libraries, strings returned by functions should
 * be freed by the caller using standard free() function
 * 
 * @param ptr - Pointer to memory to free
 */
void circe_free_string(char* ptr);

#ifdef __cplusplus
}
#endif

#endif // CIRCE_NATIVE_LIB_H
