use std::env;
use circe::{init_jvm, build_expression_query, render_and_translate_sql, build_and_render_cohort_sql, 
    validate_cohort_expression, validate_concept_set_expression, BuildExpressionQueryOptions};

/**
 * Command-line interface for the Circe Rust library
 * This demonstrates how to use the Rust library that interfaces with Java Circe functionality
 */
fn main() {
    println!("Circe Rust Library Demo");
    println!("=======================");
    
    let args: Vec<String> = env::args().collect();
    
    if args.len() < 2 {
        print_usage(&args[0]);
        std::process::exit(1);
    }
    
    // Initialize environment first
    if let Err(e) = init_jvm() {
        eprintln!("Failed to initialize environment: {:?}", e);
        eprintln!("Make sure you have run 'mvn package' to create the circe-cli.jar file.");
        std::process::exit(1);
    }
    
    let command = &args[1];
    
    let exit_code = match command.as_str() {
        "test-build-query" => {
            println!("Testing buildExpressionQuery with sample data:");
            test_build_expression_query()
        }
        "test-sql-render" => {
            println!("Testing SQL rendering and translation:");
            test_sql_render()
        }
        "test-full-workflow" => {
            println!("Testing full workflow (build + render):");
            test_full_workflow()
        }
        "test-validation" => {
            println!("Testing cohort and concept set validation:");
            test_validation()
        }
        "build-query" => {
            if args.len() < 3 {
                eprintln!("Usage: {} build-query <expression_json> [cdm_schema] [target_dialect]", args[0]);
                std::process::exit(1);
            }
            let expression = &args[2];
            let cdm_schema = args.get(3).map(|s| s.as_str());
            let target_dialect = args.get(4).map_or("postgresql", |v| v.as_str());
            
            build_query_command(expression, cdm_schema, target_dialect)
        }
        "render-sql" => {
            if args.len() < 3 {
                eprintln!("Usage: {} render-sql <sql> [target_dialect]", args[0]);
                std::process::exit(1);
            }
            let sql = &args[2];
            let target_dialect = args.get(3).map_or("postgresql", |v| v.as_str());
            
            render_sql_command(sql, target_dialect)
        }
        "validate-cohort" => {
            if args.len() < 3 {
                eprintln!("Usage: {} validate-cohort <expression_json>", args[0]);
                std::process::exit(1);
            }
            let expression = &args[2];
            validate_cohort_command(expression)
        }
        "validate-conceptset" => {
            if args.len() < 3 {
                eprintln!("Usage: {} validate-conceptset <expression_json>", args[0]);
                std::process::exit(1);
            }
            let expression = &args[2];
            validate_conceptset_command(expression)
        }
        _ => {
            println!("Unknown command: {}", command);
            print_usage(&args[0]);
            1
        }
    };
    
    std::process::exit(exit_code);
}

fn print_usage(program_name: &str) {
    println!("Usage: {} [command] [options]", program_name);
    println!("Commands:");
    println!("  test-build-query          Test buildExpressionQuery with sample data");
    println!("  test-sql-render           Test SQL rendering and translation");
    println!("  test-full-workflow        Test complete workflow");
    println!("  test-validation           Test cohort and concept set validation");
    println!("  build-query <json> [schema] [dialect]  Build cohort query from expression JSON");
    println!("  render-sql <sql> [dialect] Render and translate SQL to target dialect");
    println!("  validate-cohort <json>    Validate cohort definition JSON");
    println!("  validate-conceptset <json> Validate concept set expression JSON");
    println!("");
    println!("Examples:");
    println!("  {} test-build-query", program_name);
    println!("  {} build-query '{{\"title\":\"Test\"}}' cdm postgresql", program_name);
    println!("  {} render-sql 'SELECT * FROM @cdm.person' postgresql", program_name);
    println!("  {} validate-cohort '{{\"title\":\"Test Cohort\"}}'", program_name);
}

fn test_build_expression_query() -> i32 {
    let sample_expression = r#"{
        "title": "Test Cohort",
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

    let options = BuildExpressionQueryOptions {
        cohort_id: Some(1),
        cdm_schema: Some("cdm".to_string()),
        result_schema: Some("results".to_string()),
        generate_stats: false,
        ..Default::default()
    };

    match build_expression_query(sample_expression, options) {
        Ok(sql) => {
            println!("✓ Successfully built expression query");
            println!("Generated SQL length: {} characters", sql.len());
            println!("SQL preview (first 200 chars):");
            println!("{}", &sql[..sql.len().min(200)]);
            if sql.len() > 200 {
                println!("...");
            }
            0
        },
        Err(e) => {
            eprintln!("✗ Error building expression query: {:?}", e);
            1
        }
    }
}

fn test_sql_render() -> i32 {
    let sample_sql = "SELECT * FROM @cdm_database_schema.person WHERE person_id = @person_id";
    
    match render_and_translate_sql(sample_sql, "postgresql") {
        Ok(rendered_sql) => {
            println!("✓ Successfully rendered and translated SQL");
            println!("Original SQL: {}", sample_sql);
            println!("Rendered SQL: {}", rendered_sql);
            0
        },
        Err(e) => {
            eprintln!("✗ Error rendering SQL: {:?}", e);
            1
        }
    }
}

fn test_full_workflow() -> i32 {
    let sample_expression = r#"{
        "title": "Test Cohort with Condition",
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
                "type": "All"
            }
        },
        "conceptSets": [{
            "id": 1,
            "name": "Test Condition",
            "expression": {
                "items": []
            }
        }],
        "qualifiedLimit": {"type": "First"},
        "expressionLimit": {"type": "All"},
        "inclusionRules": [],
        "collapseSettings": {"collapseType": "ERA", "eraPad": 0}
    }"#;

    let options = BuildExpressionQueryOptions {
        cohort_id: Some(123),
        cdm_schema: Some("my_cdm".to_string()),
        vocabulary_schema: Some("my_cdm".to_string()),
        result_schema: Some("results".to_string()),
        target_table: Some("results.cohort".to_string()),
        generate_stats: true,
        ..Default::default()
    };

    match build_and_render_cohort_sql(sample_expression, options, "postgresql") {
        Ok(final_sql) => {
            println!("✓ Successfully completed full workflow");
            println!("Final SQL length: {} characters", final_sql.len());
            println!("SQL preview (first 300 chars):");
            println!("{}", &final_sql[..final_sql.len().min(300)]);
            if final_sql.len() > 300 {
                println!("...");
            }
            0
        },
        Err(e) => {
            eprintln!("✗ Error in full workflow: {:?}", e);
            1
        }
    }
}

fn test_validation() -> i32 {
    println!("Testing cohort validation:");
    let cohort_expression = r#"{"title":"Test Cohort","primaryCriteria":{"criteriaList":[],"observationWindow":{"priorDays":0,"postDays":0},"primaryLimit":{"type":"First"}},"conceptSets":[],"qualifiedLimit":{"type":"First"},"expressionLimit":{"type":"First"},"inclusionRules":[],"collapseSettings":{"collapseType":"ERA","eraPad":0}}"#;
    
    match validate_cohort_expression(cohort_expression) {
        Ok(result) => {
            println!("✓ Cohort validation successful:");
            println!("{}", result.trim());
        },
        Err(e) => {
            eprintln!("✗ Cohort validation error: {:?}", e);
            return 1;
        }
    }

    println!("\nTesting concept set validation:");
    let concept_set_expression = r#"{"items":[]}"#;
    
    match validate_concept_set_expression(concept_set_expression) {
        Ok(result) => {
            println!("✓ Concept set validation successful:");
            println!("{}", result.trim());
            0
        },
        Err(e) => {
            eprintln!("✗ Concept set validation error: {:?}", e);
            1
        }
    }
}

fn build_query_command(expression_json: &str, cdm_schema: Option<&str>, target_dialect: &str) -> i32 {
    let options = BuildExpressionQueryOptions {
        cohort_id: Some(1),
        cdm_schema: cdm_schema.map(String::from),
        vocabulary_schema: cdm_schema.map(String::from),
        generate_stats: false,
        ..Default::default()
    };

    match build_and_render_cohort_sql(expression_json, options, target_dialect) {
        Ok(sql) => {
            println!("{}", sql);
            0
        },
        Err(e) => {
            eprintln!("Error: {:?}", e);
            1
        }
    }
}

fn render_sql_command(sql: &str, target_dialect: &str) -> i32 {
    match render_and_translate_sql(sql, target_dialect) {
        Ok(rendered_sql) => {
            println!("{}", rendered_sql);
            0
        },
        Err(e) => {
            eprintln!("Error: {:?}", e);
            1
        }
    }
}

fn validate_cohort_command(expression_json: &str) -> i32 {
    match validate_cohort_expression(expression_json) {
        Ok(result) => {
            println!("{}", result);
            0
        },
        Err(e) => {
            eprintln!("Error: {:?}", e);
            1
        }
    }
}

fn validate_conceptset_command(expression_json: &str) -> i32 {
    match validate_concept_set_expression(expression_json) {
        Ok(result) => {
            println!("{}", result);
            0
        },
        Err(e) => {
            eprintln!("Error: {:?}", e);
            1
        }
    }
}