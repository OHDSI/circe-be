package org.ohdsi.circe;

import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.check.Checker;
import org.ohdsi.circe.check.Warning;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.UnmanagedMemory;

/**
 * Native library interface for Circe functionality
 * This provides C-compatible entry points for use as a shared library
 */
public class CirceNativeLibrary {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * C-compatible entry point for building cohort SQL
     */
    @CEntryPoint(name = "circe_build_cohort_sql")
    public static CCharPointer buildCohortSqlNative(IsolateThread thread, CCharPointer jsonExpression, CCharPointer options) {
        try {
            String expressionStr = CTypeConversion.toJavaString(jsonExpression);
            String optionsStr = options.isNull() ? null : CTypeConversion.toJavaString(options);
            String result = buildCohortSql(expressionStr, optionsStr);
            return CTypeConversion.toCString(result).get();
        } catch (Exception e) {
            String error = "Error in native function: " + e.getMessage();
            return CTypeConversion.toCString(error).get();
        }
    }
    
    /**
     * C-compatible entry point for checking cohort expression
     */
    @CEntryPoint(name = "circe_check_cohort_expression")
    public static CCharPointer checkCohortExpressionNative(IsolateThread thread, CCharPointer jsonExpression) {
        try {
            String expressionStr = CTypeConversion.toJavaString(jsonExpression);
            String result = checkCohortExpression(expressionStr);
            return CTypeConversion.toCString(result).get();
        } catch (Exception e) {
            String error = "Error in native function: " + e.getMessage();
            return CTypeConversion.toCString(error).get();
        }
    }
    
    /**
     * C-compatible entry point for freeing strings
     */
    @CEntryPoint(name = "circe_free_string")
    public static void freeString(IsolateThread thread, CCharPointer ptr) {
        // In GraalVM, CTypeConversion.toCString uses malloc, so we must free it.
        UnmanagedMemory.free(ptr);
    }
    
    /**
     * Build cohort SQL from JSON expression
     * 
     * @param jsonExpression JSON string of cohort expression
     * @param options Query options JSON string (nullable)
     * @return Generated SQL string or error message
     */
    public static String buildCohortSql(String jsonExpression, String options) {
        try {
            CohortExpression cohortExpression = objectMapper.readValue(jsonExpression, CohortExpression.class);
            CohortExpressionQueryBuilder.BuildExpressionQueryOptions generateOptions = 
                new CohortExpressionQueryBuilder.BuildExpressionQueryOptions();
            
            if (options != null && !options.trim().isEmpty()) {
                // Parse options if provided
                generateOptions = objectMapper.readValue(options, CohortExpressionQueryBuilder.BuildExpressionQueryOptions.class);
            }
            
            CohortExpressionQueryBuilder queryBuilder = new CohortExpressionQueryBuilder();
            String sql = queryBuilder.buildExpressionQuery(cohortExpression, generateOptions);
            
            return sql;
        } catch (Exception e) {
            return "Error building cohort SQL: " + e.getMessage();
        }
    }

    /**
     * Check/validate cohort expression JSON
     * 
     * @param jsonExpression JSON string of cohort expression
     * @return Validation result JSON string or error message
     */
    public static String checkCohortExpression(String jsonExpression) {
        try {
            CohortExpression cohortExpression = objectMapper.readValue(jsonExpression, CohortExpression.class);
            Checker checker = new Checker();
            List<Warning> warnings = checker.check(cohortExpression);
            
            return objectMapper.writeValueAsString(warnings);
        } catch (Exception e) {
            return "Error validating cohort expression: " + e.getMessage();
        }
    }

    /**
     * Main method for GraalVM shared library entry point
     */
    public static void main(String[] args) {
        System.out.println("Circe Native Library - Version 1.12.1-SNAPSHOT");
        System.out.println("Available functions:");
        System.out.println("- buildCohortSql(jsonExpression, options)");
        System.out.println("- checkCohortExpression(jsonExpression)");
    }
}
