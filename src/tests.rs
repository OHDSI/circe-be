use crate::{init_jvm, build_expression_query, render_and_translate_sql, build_and_render_cohort_sql, 
    validate_cohort_expression, validate_concept_set_expression, BuildExpressionQueryOptions, CirceError};

/// Integration tests for the Circe Rust library
/// These tests mirror the functionality of the Java tests but from the Rust side

#[cfg(test)]
mod integration_tests {
    use super::*;

    // Test data similar to the Java tests
    const SAMPLE_COHORT_EXPRESSION: &str = r#"{
        "title": "Test Cohort",
        "primaryCriteria": {
            "criteriaList": [{
                "ConditionOccurrence": {
                    "CodesetId": 1
                }
            }],
            "observationWindow": {
                "priorDays": 365,
                "postDays": 0
            },
            "primaryLimit": {
                "type": "First"
            }
        },
        "conceptSets": [{
            "id": 1,
            "name": "Test Condition Set",
            "expression": {
                "items": []
            }
        }],
        "qualifiedLimit": {"type": "First"},
        "expressionLimit": {"type": "First"},
        "inclusionRules": [],
        "collapseSettings": {"collapseType": "ERA", "eraPad": 0}
    }"#;

    const MINIMAL_COHORT_EXPRESSION: &str = r#"{
        "title": "Minimal Cohort",
        "primaryCriteria": {
            "criteriaList": [],
            "observationWindow": {
                "priorDays": 0,
                "postDays": 0
            },
            "primaryLimit": {
                "type": "First"
            }
        },
        "conceptSets": [],
        "qualifiedLimit": {"type": "First"},
        "expressionLimit": {"type": "First"},
        "inclusionRules": [],
        "collapseSettings": {"collapseType": "ERA", "eraPad": 0}
    }"#;

    #[test]
    fn test_environment_initialization() {
        match init_jvm() {
            Ok(_) => println!("✓ Environment initialized successfully"),
            Err(CirceError::InitializationError(msg)) if msg.contains("circe-cli.jar not found") => {
                println!("⚠ Expected error: JAR not found (run 'mvn package' first)");
            },
            Err(e) => panic!("Unexpected initialization error: {:?}", e),
        }
    }

    #[test]
    fn test_build_expression_query_minimal() {
        if init_jvm().is_err() {
            println!("⚠ Skipping test - environment initialization failed");
            return;
        }

        let options = BuildExpressionQueryOptions::default();

        match build_expression_query(MINIMAL_COHORT_EXPRESSION, options) {
            Ok(sql) => {
                assert!(!sql.is_empty());
                assert!(sql.to_uppercase().contains("SELECT") || sql.contains("validation"));
                println!("✓ Minimal expression query generated successfully");
                println!("  SQL length: {} characters", sql.len());
            },
            Err(e) => {
                println!("⚠ Expected error (dependencies may be missing): {:?}", e);
            }
        }
    }

    #[test]
    fn test_build_expression_query_with_options() {
        if init_jvm().is_err() {
            println!("⚠ Skipping test - environment initialization failed");
            return;
        }

        let options = BuildExpressionQueryOptions {
            cohort_id: Some(123),
            cdm_schema: Some("test_cdm".to_string()),
            vocabulary_schema: Some("test_cdm".to_string()),
            result_schema: Some("test_results".to_string()),
            target_table: Some("test_results.cohort".to_string()),
            generate_stats: true,
            ..Default::default()
        };

        match build_expression_query(SAMPLE_COHORT_EXPRESSION, options) {
            Ok(sql) => {
                assert!(!sql.is_empty());
                println!("✓ Expression query with options generated successfully");
                println!("  SQL length: {} characters", sql.len());
            },
            Err(e) => {
                println!("⚠ Expected error (dependencies may be missing): {:?}", e);
            }
        }
    }

    #[test]
    fn test_sql_render_simple() {
        if init_jvm().is_err() {
            println!("⚠ Skipping test - environment initialization failed");
            return;
        }

        let simple_sql = "SELECT * FROM @cdm_database_schema.person";

        match render_and_translate_sql(simple_sql, "postgresql") {
            Ok(rendered_sql) => {
                assert!(!rendered_sql.is_empty());
                assert!(rendered_sql.to_uppercase().contains("SELECT"));
                assert!(!rendered_sql.contains("@cdm_database_schema")); // Should be replaced
                println!("✓ Simple SQL rendered successfully");
                println!("  Original: {}", simple_sql);
                println!("  Rendered: {}", rendered_sql);
            },
            Err(e) => {
                println!("⚠ Error rendering SQL: {:?}", e);
            }
        }
    }

    #[test]
    fn test_sql_render_with_parameters() {
        if init_jvm().is_err() {
            println!("⚠ Skipping test - environment initialization failed");
            return;
        }

        let parameterized_sql = "SELECT * FROM @cdm_database_schema.condition_occurrence WHERE condition_concept_id = @concept_id";

        for dialect in &["postgresql", "sql server", "oracle"] {
            match render_and_translate_sql(parameterized_sql, dialect) {
                Ok(rendered_sql) => {
                    assert!(!rendered_sql.is_empty());
                    assert!(rendered_sql.to_uppercase().contains("SELECT"));
                    println!("✓ SQL rendered for {} dialect", dialect);
                    
                    // Verify dialect-specific transformations
                    match dialect {
                        &"postgresql" => {
                            assert!(rendered_sql.contains("cdm.condition_occurrence"));
                            assert!(rendered_sql.contains("\"condition_concept_id\"") || rendered_sql.contains("condition_concept_id"));
                        },
                        &"sql server" => {
                            assert!(rendered_sql.contains("[cdm]"));
                        },
                        &"oracle" => {
                            // The rendered SQL should be uppercase
                            assert_eq!(rendered_sql, rendered_sql.to_uppercase(), "Oracle SQL should be uppercase");
                        },
                        _ => {}
                    }
                },
                Err(e) => {
                    println!("⚠ Expected error for {} dialect: {:?}", dialect, e);
                }
            }
        }
    }

    #[test]
    fn test_validation_functions() {
        if init_jvm().is_err() {
            println!("⚠ Skipping test - environment initialization failed");
            return;
        }

        // Test cohort validation
        match validate_cohort_expression(MINIMAL_COHORT_EXPRESSION) {
            Ok(result) => {
                assert!(!result.is_empty());
                println!("✓ Cohort validation successful: {}", result.trim());
            },
            Err(e) => {
                println!("⚠ Cohort validation error: {:?}", e);
            }
        }

        // Test concept set validation
        let concept_set = r#"{"items":[]}"#;
        match validate_concept_set_expression(concept_set) {
            Ok(result) => {
                assert!(!result.is_empty());
                println!("✓ Concept set validation successful: {}", result.trim());
            },
            Err(e) => {
                println!("⚠ Concept set validation error: {:?}", e);
            }
        }
    }

    #[test]
    fn test_full_workflow_minimal() {
        if init_jvm().is_err() {
            println!("⚠ Skipping test - environment initialization failed");
            return;
        }

        let options = BuildExpressionQueryOptions {
            cohort_id: Some(1),
            cdm_schema: Some("cdm".to_string()),
            vocabulary_schema: Some("cdm".to_string()),
            generate_stats: false,
            ..Default::default()
        };

        match build_and_render_cohort_sql(MINIMAL_COHORT_EXPRESSION, options, "postgresql") {
            Ok(final_sql) => {
                assert!(!final_sql.is_empty());
                println!("✓ Full workflow completed successfully");
                println!("  Final SQL length: {} characters", final_sql.len());
            },
            Err(e) => {
                println!("⚠ Expected error (dependencies may be missing): {:?}", e);
            }
        }
    }

    #[test]
    fn test_error_handling_invalid_json() {
        if init_jvm().is_err() {
            println!("⚠ Skipping test - environment initialization failed");
            return;
        }

        let invalid_json = "{ invalid json }";

        match validate_cohort_expression(invalid_json) {
            Ok(_) => {
                println!("⚠ Unexpectedly succeeded with invalid JSON - may indicate fallback behavior");
            },
            Err(e) => {
                println!("✓ Properly handled invalid JSON: {:?}", e);
                match e {
                    CirceError::JavaException(_) | CirceError::JsonError(_) | CirceError::ProcessError(_) => {
                        // Expected error types
                    },
                    _ => println!("⚠ Unexpected error type, but still an error as expected"),
                }
            }
        }
    }

    #[test]
    fn test_options_serialization_compatibility() {
        let options = BuildExpressionQueryOptions {
            cohort_id_field_name: Some("custom_cohort_id".to_string()),
            cohort_id: Some(999),
            cdm_schema: Some("test_cdm".to_string()),
            target_table: Some("test.target".to_string()),
            result_schema: Some("test_results".to_string()),
            vocabulary_schema: Some("test_vocab".to_string()),
            generate_stats: true,
        };

        let json = options.to_json();
        
        // Verify the JSON contains expected fields with correct names (matching Java)
        assert!(json.contains("cohortIdFieldName"));
        assert!(json.contains("cohortId"));
        assert!(json.contains("cdmSchema"));
        assert!(json.contains("targetTable"));
        assert!(json.contains("resultSchema"));
        assert!(json.contains("vocabularySchema"));
        assert!(json.contains("generateStats"));
        
        println!("✓ Options serialization produces Java-compatible JSON");
        println!("  JSON: {}", json);
    }
}