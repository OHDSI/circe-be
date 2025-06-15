package org.ohdsi.circe.cli;

import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;

/**
 * Simple CLI wrapper for Circe library to enable GraalVM native compilation
 */
public class CirceCLI {
    
    public static void main(String[] args) {
        System.out.println("Circe CLI - Java library for OMOP Common Data Model queries");
        System.out.println("Version: 1.12.1-SNAPSHOT");
        
        if (args.length == 0) {
            printUsage();
            return;
        }
        
        String command = args[0];
        
        switch (command) {
            case "validate-cohort":
                if (args.length < 2) {
                    System.err.println("Error: JSON string required for cohort validation");
                    return;
                }
                validateCohort(args[1]);
                break;
            case "validate-conceptset":
                if (args.length < 2) {
                    System.err.println("Error: JSON string required for concept set validation");
                    return;
                }
                validateConceptSet(args[1]);
                break;
            case "version":
                System.out.println("Circe library version: 1.12.1-SNAPSHOT");
                break;
            default:
                System.err.println("Unknown command: " + command);
                printUsage();
        }
    }
    
    private static void printUsage() {
        System.out.println("Usage: circe-cli [command] [options]");
        System.out.println("Commands:");
        System.out.println("  validate-cohort <json>     Validate a cohort definition JSON");
        System.out.println("  validate-conceptset <json> Validate a concept set expression JSON");
        System.out.println("  version                    Show version information");
    }
    
    private static void validateCohort(String json) {
        try {
            CohortExpression cohort = CohortExpression.fromJson(json);
            System.out.println("Cohort definition is valid");
            System.out.println("Title: " + (cohort.title != null ? cohort.title : "Unnamed"));
            System.out.println("Primary Criteria: " + (cohort.primaryCriteria != null ? "Present" : "Missing"));
        } catch (Exception e) {
            System.err.println("Error validating cohort: " + e.getMessage());
        }
    }
    
    private static void validateConceptSet(String json) {
        try {
            ConceptSetExpression conceptSet = ConceptSetExpression.fromJson(json);
            System.out.println("Concept set expression is valid");
            System.out.println("Number of items: " + 
                (conceptSet.items != null ? conceptSet.items.length : 0));
        } catch (Exception e) {
            System.err.println("Error validating concept set: " + e.getMessage());
        }
    }
}