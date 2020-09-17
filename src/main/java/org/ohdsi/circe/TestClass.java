package org.ohdsi.circe;

import java.io.*;
import java.nio.charset.*;
import org.apache.commons.io.*;

import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.sql.SqlRender;

public class TestClass {

    private static CohortExpressionQueryBuilder.BuildExpressionQueryOptions buildExpressionQueryOptions(
        final int cohortId, final String cdmSchema, final String resultSchema, final String targetTable, final String codelistDataset
    ) {
      final CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = new CohortExpressionQueryBuilder.BuildExpressionQueryOptions();
      options.cdmSchema = cdmSchema;
      options.cohortId = cohortId;
      options.generateStats = false;
      options.resultSchema = resultSchema;
      options.targetTable = targetTable;
      options.codelistDataset = codelistDataset;
      return options;
    }
    
    private static String buildExpressionSql(final CohortExpression expression,
        final CohortExpressionQueryBuilder.BuildExpressionQueryOptions options) {
      // build SQL
      final CohortExpressionQueryBuilder builder = new CohortExpressionQueryBuilder();
      String cohortSql = builder.buildExpressionQuery(expression, options);
      return cohortSql;
    }

    private static String readFile(String path) throws IOException{
        File file = new File(path);
        return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
    }

    public static String createSql(
        int cohortId, String cdmSchema, String resultSchema, 
        String targetTable, String codelistDataset, String jsonString
    ) throws IOException {

        System.out.println("Parsing Json to SQL");

        final CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = buildExpressionQueryOptions(
            cohortId,
            cdmSchema,
            resultSchema,
            targetTable,
            codelistDataset
        );
    
        final CohortExpression expression = CohortExpression.fromJson(jsonString);
    
        String cohortSql = buildExpressionSql(expression, options);
        cohortSql = SqlRender.renderSql(cohortSql, null, null);

        return(cohortSql);
    }

    public static void main(String [] args) throws IOException {

        final int cohortId = 1;
        final String cdmSchema = "/UNITE/Safe Harbor/transform";
        final String resultSchema = "";
        final String targetTable =  "/UNITE/Palantir/OHDSI/testing";
        final String codelistDataset = "`/UNITE/Palantir/Applications/Development/Code Set Builder/hubble_base/concept_set_to_concept_id_edited`";

        final String jsonFilePath = "/Users/bamor/Documents/active_projects/circe-be/test.json";
        final String outputSqlFilePath = "/Users/bamor/Documents/active_projects/circe-be/test.sql";

        final String jsonString = readFile(jsonFilePath);
    
        final String cohortSql = createSql(cohortId, cdmSchema, resultSchema, targetTable, codelistDataset, jsonString);
        FileUtils.writeStringToFile(new File(outputSqlFilePath), cohortSql, StandardCharsets.UTF_8);
    }

}