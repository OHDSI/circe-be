package org.ohdsi.circe.jni;

import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.nativeimage.IsolateThread;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.circe.check.CheckResult;
import org.ohdsi.circe.check.Checker;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;

/**
 * Native library interface for Circe functionality
 * This provides C-compatible entry points for use as a shared library
 */
public class CirceNativeLibrary {

    @CEntryPoint(name = "circe_build_expression_query")
    public static CCharPointer buildExpressionQuery(IsolateThread thread, CCharPointer jsonExpression, CCharPointer options) {
        try {
            String expressionJson = CTypeConversion.toJavaString(jsonExpression);
            String optionsJson = CTypeConversion.toJavaString(options);
            
            // Parse the cohort expression
            CohortExpression expression = CohortExpression.fromJson(expressionJson);
            
            // Create query builder
            CohortExpressionQueryBuilder builder = new CohortExpressionQueryBuilder();
            
            // Build the query with default options for now
            // TODO: Parse options from optionsJson
            CohortExpressionQueryBuilder.BuildExpressionQueryOptions queryOptions = 
                new CohortExpressionQueryBuilder.BuildExpressionQueryOptions();
            
            String cohortSql = builder.buildExpressionQuery(expression, queryOptions);
            
            return CTypeConversion.toCString(cohortSql).get();
        } catch (Exception e) {
            String error = "Error: " + e.getMessage();
            return CTypeConversion.toCString(error).get();
        }
    }

    @CEntryPoint(name = "circe_render_and_translate_sql")
    public static CCharPointer renderAndTranslateSql(IsolateThread thread, CCharPointer sql, CCharPointer targetDialect) {
        try {
            String sqlString = CTypeConversion.toJavaString(sql);
            String dialect = CTypeConversion.toJavaString(targetDialect);
            
            // Translate and render SQL
            String translatedSql = SqlTranslate.translateSql(sqlString, dialect);
            String renderedSql = SqlRender.renderSql(translatedSql, null, null);
            
            return CTypeConversion.toCString(renderedSql).get();
        } catch (Exception e) {
            String error = "Error: " + e.getMessage();
            return CTypeConversion.toCString(error).get();
        }
    }

    @CEntryPoint(name = "circe_validate_cohort_expression")
    public static CCharPointer validateCohortExpression(IsolateThread thread, CCharPointer jsonExpression) {
        try {
            String expressionJson = CTypeConversion.toJavaString(jsonExpression);
            
            // Parse and validate the cohort expression
            CohortExpression expression = CohortExpression.fromJson(expressionJson);
            CheckResult result = Checker.checkCohortExpression(expression);
            
            // Convert result to JSON (simplified)
            if (result.getWarnings().isEmpty()) {
                return CTypeConversion.toCString("{\"valid\": true, \"warnings\": []}").get();
            } else {
                String warnings = result.getWarnings().toString();
                return CTypeConversion.toCString("{\"valid\": false, \"warnings\": " + warnings + "}").get();
            }
        } catch (Exception e) {
            String error = "{\"valid\": false, \"error\": \"" + e.getMessage() + "\"}";
            return CTypeConversion.toCString(error).get();
        }
    }

    @CEntryPoint(name = "circe_validate_concept_set_expression")
    public static CCharPointer validateConceptSetExpression(IsolateThread thread, CCharPointer jsonExpression) {
        try {
            String expressionJson = CTypeConversion.toJavaString(jsonExpression);
            
            // Parse and validate the concept set expression
            ConceptSetExpression expression = ConceptSetExpression.fromJson(expressionJson);
            CheckResult result = Checker.checkConceptSetExpression(expression);
            
            // Convert result to JSON (simplified)
            if (result.getWarnings().isEmpty()) {
                return CTypeConversion.toCString("{\"valid\": true, \"warnings\": []}").get();
            } else {
                String warnings = result.getWarnings().toString();
                return CTypeConversion.toCString("{\"valid\": false, \"warnings\": " + warnings + "}").get();
            }
        } catch (Exception e) {
            String error = "{\"valid\": false, \"error\": \"" + e.getMessage() + "\"}";
            return CTypeConversion.toCString(error).get();
        }
    }

    @CEntryPoint(name = "circe_get_version")
    public static CCharPointer getVersion(IsolateThread thread) {
        return CTypeConversion.toCString("1.12.1-SNAPSHOT").get();
    }
}
