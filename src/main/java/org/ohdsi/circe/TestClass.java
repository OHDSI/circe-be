package org.ohdsi.circe;

import java.io.*;
import java.nio.charset.*;
import org.apache.commons.io.*;

import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;

public class TestClass {

    private static CohortExpressionQueryBuilder.BuildExpressionQueryOptions buildExpressionQueryOptions(
        final int cohortId, final String resultsSchema) {
      final CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = new CohortExpressionQueryBuilder.BuildExpressionQueryOptions();
      options.cdmSchema = "/UNITE/Safe Harbor - TUFTS/transform";
      options.cohortId = cohortId;
      options.generateStats = false;
      options.resultSchema = resultsSchema;
      options.targetTable = "/UNITE/Palantir/OMOP FeatureExtraction/covid_serious_outcomes_plp/cohort";
      options.codelistDataset = "`/UNITE/Palantir/OMOP FeatureExtraction/code_lists/serious_covid_outcomes/mappedConcepts`";
      return options;
    }
    
    private static String buildExpressionSql(final CohortExpression expression,
        final CohortExpressionQueryBuilder.BuildExpressionQueryOptions options) {
      // build SQL
      final CohortExpressionQueryBuilder builder = new CohortExpressionQueryBuilder();
      String cohortSql = builder.buildExpressionQuery(expression, options);

      // translate to PG
      //cohortSql = SqlRender.renderSql(SqlTranslate.translateSql(cohortSql, "postgresql"), null, null);
      return cohortSql;
    }

    private static String readFile(String path) throws IOException{
        File file = new File(path);
        return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
    }
    public static void main(String[] args) throws IOException {
        System.out.println("Hello");
        final CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = buildExpressionQueryOptions(1,"allCriteriaTest");
    
        // load 'all' criteria json
        final CohortExpression expression = CohortExpression
            .fromJson(readFile("/Users/bamor/Documents/active_projects/circe-be/cohort_O.json"));
    
        // build Sql
        String cohortSql = buildExpressionSql(expression, options);
        cohortSql = SqlRender.renderSql(cohortSql, null, null);
        FileUtils.writeStringToFile(new File("/Users/bamor/Documents/active_projects/circe-be/cohort_O.sql"), cohortSql, StandardCharsets.UTF_8);
    }

}