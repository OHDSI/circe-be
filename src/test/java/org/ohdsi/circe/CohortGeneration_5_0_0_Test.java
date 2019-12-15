package org.ohdsi.circe;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNoException;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.github.mjeanroy.dbunit.core.dataset.DataSetFactory;

import org.apache.commons.lang3.StringUtils;
import org.dbunit.Assertion;
import org.dbunit.DataSourceDatabaseTester;
import org.dbunit.DatabaseUnitException;
import org.dbunit.DefaultPrepAndExpectedTestCase;
import org.dbunit.PrepAndExpectedTestCaseSteps;
import org.dbunit.VerifyTableDefinition;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.circe.cohortdefinition.PrimaryCriteria;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.zonky.test.db.postgres.junit.EmbeddedPostgresRules;
import io.zonky.test.db.postgres.junit.SingleInstancePostgresRule;

import org.springframework.jdbc.core.JdbcTemplate;

// Note: to verify the test results, we must directly query the database
// via createQueryTable(), because loading the result schema tables via
// getTables() fails because the results schema isn't seen by the existing connection.

public class CohortGeneration_5_0_0_Test {

  private final static Logger log = LoggerFactory.getLogger(CohortGeneration_5_0_0_Test.class);
  private static final String CDM_DDL_PATH = "/cohortgeneration/cdm_v5.0.sql";
  private static final String RESULTS_DDL_PATH = "/cohortgeneration/resultsSchema.sql";
  private static JdbcTemplate jdbcTemplate;

  @ClassRule
  public static SingleInstancePostgresRule pg = EmbeddedPostgresRules.singleInstance();

  private static DataSource getDataSource() {
    return pg.getEmbeddedPostgres().getPostgresDatabase();
  }

  private static IDatabaseConnection getConnection() throws SQLException {
    IDatabaseConnection con = new DatabaseDataSourceConnection(getDataSource());
    con.getConfig().setProperty(DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES, true);
    con.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
    return con;
  }

  private static void prepareSchema(String schemaName, String schemaPath) {
    String sql = StringUtils.replace(ResourceHelper.GetResourceAsString(schemaPath), "@schemaName", schemaName);
    jdbcTemplate.execute(String.format("DROP SCHEMA IF EXISTS %s CASCADE", schemaName));
    jdbcTemplate.execute(String.format("CREATE SCHEMA %s", schemaName));
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(sql));    
  }

  private static CohortExpressionQueryBuilder.BuildExpressionQueryOptions buildExpressionQueryOptions(int cohortId, String resultsSchema) {
    CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = new CohortExpressionQueryBuilder.BuildExpressionQueryOptions();
    options.cdmSchema = "cdm";
    options.cohortId = 1;
    options.generateStats = true;
    options.resultSchema = resultsSchema;
    options.targetTable = options.resultSchema + ".cohort";

    return options;
  }

  private static String buildExpressionSql(CohortExpression expression, CohortExpressionQueryBuilder.BuildExpressionQueryOptions options)
  {
    // build SQL
    CohortExpressionQueryBuilder builder = new CohortExpressionQueryBuilder();
    String cohortSql = builder.buildExpressionQuery(expression, options);

    // translate to PG
    cohortSql = SqlRender.renderSql(SqlTranslate.translateSql(cohortSql,"postgresql"),null,null);

    return cohortSql;
  }
  
  @BeforeClass
  public static void beforeClass() {
    jdbcTemplate = new JdbcTemplate(getDataSource());
    prepareSchema("cdm", CDM_DDL_PATH);
  }

  @Test
  public void cdmSchemaWasCreated() {
    jdbcTemplate.queryForList("select count(*) as c from cdm.PERSON");
  }

  @Test
  public void loadTestData() throws SQLException, DataSetException {
    final String[] testDataSetsPaths = new String[] {"/datasets/vocabulary.json"};

    IDatabaseConnection dbUnitCon = getConnection();
    IDataSet ds = DataSetFactory.createDataSet(testDataSetsPaths);

    assertNotNull("No dataset found", ds);

    try {
      DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, ds); // clean load of the DB. Careful, clean means "delete the old stuff"

    } catch (DatabaseUnitException e) {
      log.error("Failed to load the data: {}", e);
      fail(e.getMessage());
    } finally {
      dbUnitCon.close();
    }
  }

  @Test
  public void allCriteriaTest() throws SQLException {
    
    CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = buildExpressionQueryOptions(1, "allCriteriaTest");

    // prepare results schema
    prepareSchema(options.resultSchema, RESULTS_DDL_PATH);
    
    // load 'all' criteria json
    CohortExpression expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/cohortgeneration/allCriteria/allCriteriaExpression.json"));

    // build Sql
    String cohortSql = buildExpressionSql(expression, options);

    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));    
  }

  @Test
  public void firstOccurrenceTest() throws Exception {
    final String[] testDataSetsPrep = new String[] {"/datasets/vocabulary.json", "/cohortgeneration/firstOccurrence/firstOccurrenceTest_PREP.json"};
    final String[] testDataSetsVerify = new String[] {"/cohortgeneration/firstOccurrence/firstOccurrenceTest_VERIFY.json"};
    IDatabaseConnection dbUnitCon = getConnection();
    
    // define options for result schema
    CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = buildExpressionQueryOptions(1, "firstOccurrenceTest");

    // prepare results schema for the specified options.resultSchema
    prepareSchema(options.resultSchema, RESULTS_DDL_PATH);

    // load test data into DB.
    IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    // load test case expresion json
    CohortExpression expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/cohortgeneration/firstOccurrence/firstOccurrenceTestExpression.json"));
    String cohortSql = buildExpressionSql(expression, options);
  
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));

    ITable actualTable = dbUnitCon.createQueryTable(options.targetTable, String.format("SELECT * from %s", options.targetTable));

    // Load expected data from an XML dataset
    IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);
    ITable expectedTable = expectedDataSet.getTable(options.targetTable);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedTable, actualTable);    
  }
}
