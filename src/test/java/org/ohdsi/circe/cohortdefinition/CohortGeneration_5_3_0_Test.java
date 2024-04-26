package org.ohdsi.circe.cohortdefinition;

import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.ohdsi.circe.AbstractDatabaseTest;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class CohortGeneration_5_3_0_Test extends AbstractDatabaseTest {
    private final static Logger log = LoggerFactory.getLogger(CohortGeneration_5_3_0_Test.class);
    private static final String CDM_DDL_PATH = "/ddl/cdm_v5.3.sql";
    private static final String RESULTS_DDL_PATH = "/ddl/resultsSchema.sql";
    
    private static CohortExpressionQueryBuilder.BuildExpressionQueryOptions buildExpressionQueryOptions(
            final int cohortId, final String resultsSchema) {
        final CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = new CohortExpressionQueryBuilder.BuildExpressionQueryOptions();
        options.cdmSchema = "cdm";
        options.cohortId = cohortId;
        options.generateStats = true;
        options.resultSchema = resultsSchema;
        options.targetTable = resultsSchema + ".cohort";
        
        return options;
    }
    
    private static String buildExpressionSql(final CohortExpression expression,
            final CohortExpressionQueryBuilder.BuildExpressionQueryOptions options) {
        // build SQL
        final CohortExpressionQueryBuilder builder = new CohortExpressionQueryBuilder();
        String cohortSql = builder.buildExpressionQuery(expression, options);
        
        // translate to PG
        cohortSql = SqlRender.renderSql(SqlTranslate.translateSql(cohortSql, "postgresql"), null, null);
        
        return cohortSql;
    }
    
    @BeforeClass
    public static void beforeClass() {
        jdbcTemplate = new JdbcTemplate(getDataSource());
        prepareSchema("cdm", CDM_DDL_PATH);
    }
    
    @Test
    public void allCriteriaTest() throws SQLException {
        
        final CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = buildExpressionQueryOptions(1,
                "allCriteriaTest");
        
        // prepare results schema
        prepareSchema(options.resultSchema, RESULTS_DDL_PATH);
        
        // load 'all' criteria json
        final CohortExpression expression = CohortExpression.fromJson(
                ResourceHelper.GetResourceAsString("/cohortgeneration/allCriteria/criteriaWithDateTime.json"));
        
        // build Sql
        final String cohortSql = buildExpressionSql(expression, options);
        
        // execute on database, expect no errors
        jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));
    }
    
    @Test
    public void allCriteriaTestWithRetainingCohortCovariates() throws SQLException {
        
        final CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = buildExpressionQueryOptions(1,
                "allCriteriaTest");
        options.retainCohortCovariates = true;
        
        // prepare results schema
        prepareSchema(options.resultSchema, RESULTS_DDL_PATH);
        
        // load 'all' criteria json
        final CohortExpression expression = CohortExpression.fromJson(
                ResourceHelper.GetResourceAsString("/cohortgeneration/allCriteria/criteriaWithDateTime.json"));
        
        // build Sql
        final String cohortSql = buildExpressionSql(expression, options);
        
        // execute on database, expect no errors
        jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));
    }
    
    @Test
    public void allCriteriaTestWithTimeUnitInterval() throws SQLException {
        
        final CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = buildExpressionQueryOptions(1,
                "allCriteriaTest");
        
        // prepare results schema
        prepareSchema(options.resultSchema, RESULTS_DDL_PATH);
        
        // load 'all' criteria json
        final CohortExpression expression = CohortExpression.fromJson(ResourceHelper
                .GetResourceAsString("/cohortgeneration/allCriteria/criteriaWithDateTime.json"));
        
        // build Sql
        final String cohortSql = buildExpressionSql(expression, options);
        
        // execute on database, expect no errors
        jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));
    }
}
