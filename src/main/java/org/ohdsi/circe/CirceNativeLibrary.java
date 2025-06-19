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
    
    // Stack overflow protection - track recursion depth
    private static final ThreadLocal<Integer> recursionDepth = ThreadLocal.withInitial(() -> 0);
    private static final int MAX_RECURSION_DEPTH = 100;
    
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
            // Stack overflow protection
            int depth = recursionDepth.get();
            if (depth > MAX_RECURSION_DEPTH) {
                return "Error building cohort SQL: Maximum recursion depth exceeded";
            }
            recursionDepth.set(depth + 1);
            
            // Add debug logging
            System.err.println("DEBUG: Building cohort SQL");
            System.err.println("DEBUG: Expression length: " + (jsonExpression != null ? jsonExpression.length() : "null"));
            System.err.println("DEBUG: Options: " + options);
            
            if (jsonExpression == null || jsonExpression.trim().isEmpty()) {
                return "Error building cohort SQL: Expression is null or empty";
            }
            
            CohortExpression cohortExpression = objectMapper.readValue(jsonExpression, CohortExpression.class);
            System.err.println("DEBUG: Parsed cohort expression successfully");
            
            CohortExpressionQueryBuilder.BuildExpressionQueryOptions generateOptions = 
                new CohortExpressionQueryBuilder.BuildExpressionQueryOptions();
            
            if (options != null && !options.trim().isEmpty()) {
                // Parse options if provided
                generateOptions = objectMapper.readValue(options, CohortExpressionQueryBuilder.BuildExpressionQueryOptions.class);
                System.err.println("DEBUG: Parsed options successfully");
            }
            
            CohortExpressionQueryBuilder queryBuilder = new CohortExpressionQueryBuilder();
            System.err.println("DEBUG: Created query builder");
            
            String sql = queryBuilder.buildExpressionQuery(cohortExpression, generateOptions);
            System.err.println("DEBUG: Generated SQL, length: " + (sql != null ? sql.length() : "null"));
            
            if (sql == null) {
                return "Error building cohort SQL: Generated SQL is null";
            }
            
            return sql;
        } catch (Exception e) {
            System.err.println("DEBUG: Exception in buildCohortSql: " + e.getClass().getSimpleName());
            System.err.println("DEBUG: Exception message: " + e.getMessage());
            e.printStackTrace();
            
            String errorMsg = e.getMessage();
            if (errorMsg == null) {
                errorMsg = e.getClass().getSimpleName() + " (no message)";
            }
            return "Error building cohort SQL: " + errorMsg;
        } finally {
            // Reset recursion depth
            int depth = recursionDepth.get();
            recursionDepth.set(Math.max(0, depth - 1));
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
            System.err.println("DEBUG: Checking cohort expression");
            System.err.println("DEBUG: Expression length: " + (jsonExpression != null ? jsonExpression.length() : "null"));
            
            if (jsonExpression == null || jsonExpression.trim().isEmpty()) {
                return "Error validating cohort expression: Expression is null or empty";
            }
            
            CohortExpression cohortExpression = objectMapper.readValue(jsonExpression, CohortExpression.class);
            System.err.println("DEBUG: Parsed cohort expression for validation");
            
            Checker checker = new Checker();
            System.err.println("DEBUG: Created checker");
            
            List<Warning> warnings = checker.check(cohortExpression);
            System.err.println("DEBUG: Check completed, warnings count: " + (warnings != null ? warnings.size() : "null"));
            
            String result = objectMapper.writeValueAsString(warnings);
            System.err.println("DEBUG: Serialized warnings to JSON, length: " + (result != null ? result.length() : "null"));
            
            return result;
        } catch (Exception e) {
            System.err.println("DEBUG: Exception in checkCohortExpression: " + e.getClass().getSimpleName());
            System.err.println("DEBUG: Exception message: " + e.getMessage());
            e.printStackTrace();
            
            String errorMsg = e.getMessage();
            if (errorMsg == null) {
                errorMsg = e.getClass().getSimpleName() + " (no message)";
            }
            return "Error validating cohort expression: " + errorMsg;
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
