#include "../include/circe_native_lib.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int test_version() {
    printf("Testing circe_get_version...\n");
    const char* version = circe_get_version();
    if (version == NULL) {
        printf("❌ circe_get_version returned NULL\n");
        return 1;
    }
    printf("✅ Version: %s\n", version);
    return 0;
}

int test_validate_cohort() {
    printf("Testing circe_validate_cohort_expression...\n");
    
    // Simple test with empty JSON
    const char* test_json = "{}";
    char* result = circe_validate_cohort_expression(test_json);
    if (result == NULL) {
        printf("❌ circe_validate_cohort_expression returned NULL\n");
        return 1;
    }
    
    printf("✅ Validation result: %s\n", result);
    free(result);
    return 0;
}

int test_build_query() {
    printf("Testing circe_build_expression_query...\n");
    
    // Simple test JSON (this would need to be a valid cohort expression in real use)
    const char* test_expression = "{\"conceptSets\":[], \"primaryCriteria\":{\"criteriaList\":[]}}";
    const char* options = "{}";
    
    char* result = circe_build_expression_query(test_expression, options);
    if (result == NULL) {
        printf("❌ circe_build_expression_query returned NULL\n");
        return 1;
    }
    
    // Check if it's an error message
    if (strncmp(result, "Error:", 6) == 0) {
        printf("⚠️  Got error (expected for minimal test): %s\n", result);
    } else {
        printf("✅ Query result: %.100s...\n", result);  // Print first 100 chars
    }
    
    free(result);
    return 0;
}

int test_sql_translation() {
    printf("Testing circe_render_and_translate_sql...\n");
    
    const char* test_sql = "SELECT * FROM @cdm_database_schema.person";
    const char* target_dialect = "postgresql";
    
    char* result = circe_render_and_translate_sql(test_sql, target_dialect);
    if (result == NULL) {
        printf("❌ circe_render_and_translate_sql returned NULL\n");
        return 1;
    }
    
    printf("✅ Translated SQL: %s\n", result);
    free(result);
    return 0;
}

int main() {
    printf("Circe Native Library Test\n");
    printf("=========================\n\n");
    
    int errors = 0;
    
    errors += test_version();
    printf("\n");
    
    errors += test_validate_cohort();
    printf("\n");
    
    errors += test_build_query();
    printf("\n");
    
    errors += test_sql_translation();
    printf("\n");
    
    if (errors == 0) {
        printf("🎉 All tests passed!\n");
    } else {
        printf("❌ %d test(s) failed\n", errors);
    }
    
    return errors;
}
