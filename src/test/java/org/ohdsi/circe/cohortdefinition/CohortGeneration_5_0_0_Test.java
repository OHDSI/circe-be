package org.ohdsi.circe.cohortdefinition;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.sql.SQLException;
import com.github.mjeanroy.dbunit.core.dataset.DataSetFactory;
import org.apache.commons.lang3.StringUtils;
import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.operation.DatabaseOperation;
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

// Note: to verify the test results, we must directly query the database
// via createQueryTable(), because loading the result schema tables via
// getTables() fails because the results schema isn't seen by the existing connection.

public class CohortGeneration_5_0_0_Test extends AbstractDatabaseTest {

  private final static Logger log = LoggerFactory.getLogger(CohortGeneration_5_0_0_Test.class);
  private static final String CDM_DDL_PATH = "/ddl/cdm_v5.0.sql";
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

  /* Scema init and load tests */

  @Test
  public void cdmSchemaWasCreated() {
    jdbcTemplate.queryForList("select count(*) as c from cdm.PERSON");
  }

  @Test
  public void loadTestData() throws SQLException, DataSetException {
    final String[] testDataSetsPaths = new String[] { "/datasets/vocabulary.json" };

    final IDatabaseConnection dbUnitCon = getConnection();
    final IDataSet ds = DataSetFactory.createDataSet(testDataSetsPaths);

    assertNotNull("No dataset found", ds);

    try {
      DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, ds); // clean load of the DB. Careful, clean means "delete the old stuff"

    } catch (final DatabaseUnitException e) {
      log.error("Failed to load the data: {}", e);
      fail(e.getMessage());
    } finally {
      dbUnitCon.close();
    }
  }



  @Test
  public void rawJsonTest() throws SQLException {

    final CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = buildExpressionQueryOptions(1,"allCriteriaTest");

    // prepare results schema
    prepareSchema(options.resultSchema, RESULTS_DDL_PATH);

    // load 'all' criteria json
    final String expression = ResourceHelper.GetResourceAsString("/cohortgeneration/allCriteria/allCriteriaExpression.json");

    // build Sql
    final CohortExpressionQueryBuilder builder = new CohortExpressionQueryBuilder();
    String cohortSql = builder.buildExpressionQuery(expression, options);
    cohortSql = SqlRender.renderSql(SqlTranslate.translateSql(cohortSql, "postgresql"), null, null);

    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));
  }

  /* Cohort Expression Tests */
  
  @Test
  public void allCriteriaTest() throws SQLException {

    final CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = buildExpressionQueryOptions(1,"allCriteriaTest");

    // prepare results schema
    prepareSchema(options.resultSchema, RESULTS_DDL_PATH);

    // load 'all' criteria json
    final CohortExpression expression = CohortExpression
        .fromJson(ResourceHelper.GetResourceAsString("/cohortgeneration/allCriteria/allCriteriaExpression.json"));

    // build Sql
    final String cohortSql = buildExpressionSql(expression, options);

    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));
  }

  /* first occurrence tests */
  private void setFirstOccurrenceCriteria(final Criteria c) throws Exception {
    final Field first = c.getClass().getDeclaredField("first");
    first.set(c, Boolean.valueOf(true));
  }

  @Test
  public void firstOccurrenceTest() throws Exception {
    final String RESULTS_SCHEMA = "firstOccurrenceTest";
    final String[] testDataSetsPrep = new String[] { 
      "/datasets/vocabulary.json",
      "/cohortgeneration/firstOccurrence/firstOccurrenceTest_PREP.json" 
    };
    final String[] testDataSetsVerify = new String[] {"/cohortgeneration/firstOccurrence/firstOccurrenceTest_VERIFY.json"};
    final IDatabaseConnection dbUnitCon = getConnection();

    // prepare results schema for the specified options.resultSchema
    prepareSchema(RESULTS_SCHEMA, RESULTS_DDL_PATH);

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    // load test case expresion json: empty primary criteria, but specifies 90d of
    // prior continuous observation, and a simple concept set
    final CohortExpression expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/cohortgeneration/firstOccurrence/firstOccurrenceTestExpression.json"));

    String cohortSql;
    CohortExpressionQueryBuilder.BuildExpressionQueryOptions options;
    final Criteria[] testCriteria = new Criteria[] { 
      new ConditionEra(), 
      new ConditionOccurrence(),
      new DeviceExposure(),
      new DrugEra(),
      new DrugExposure(),
      new Measurement(),
      new Observation(),
      new ProcedureOccurrence(),
      new Specimen(),
      new VisitOccurrence(),
      new PayerPlanPeriod()
    };

    // create and execute cohort for each test criteria
    for (int i = 0; i < testCriteria.length; i++) {
      final Criteria c = testCriteria[i];
      options = buildExpressionQueryOptions(i + 1, RESULTS_SCHEMA);
      setFirstOccurrenceCriteria(c);
      expression.primaryCriteria.criteriaList = new Criteria[] { c };
      cohortSql = buildExpressionSql(expression, options);
      // execute on database, expect no errors
      jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));
    }

    // Validate results
    // Load actual records from cohort table
    final ITable actualTable = dbUnitCon.createQueryTable(RESULTS_SCHEMA + ".cohort", String.format("SELECT * from %s ORDER BY cohort_definition_id, subject_id, cohort_start_date", RESULTS_SCHEMA + ".cohort"));
    // Load expected data from an XML dataset
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);
    final ITable expectedTable = expectedDataSet.getTable(RESULTS_SCHEMA + ".cohort");

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedTable, actualTable);    
  }

  /**
   *  Check correlated criteria
   */  
  @Test
  public void testCorrelatedCriteriaCounts() throws Exception  {
    final String RESULTS_SCHEMA = "correlatedCriteriaCountsTest";
    final String[] testDataSetsPrep = new String[] { 
      "/datasets/vocabulary.json",
      "/cohortgeneration/correlatedCriteria/countsTest_PREP.json" 
    };
    final String[] testDataSetsVerify = new String[] {"/cohortgeneration/correlatedCriteria/countsTest_VERIFY.json"};
    final IDatabaseConnection dbUnitCon = getConnection();

    // prepare results schema for the specified options.resultSchema
    prepareSchema(RESULTS_SCHEMA, RESULTS_DDL_PATH);

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    CohortExpressionQueryBuilder.BuildExpressionQueryOptions options;
    CohortExpression expression;   
    String cohortSql;
    
    // load the default expression, which looks for the event with exactly 0 conceptSet = 1 between all days beore and 0 days before index
    // cohort 1 will use the default expression from JSON.
    expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/cohortgeneration/correlatedCriteria/countsExpression.json"));
    options = buildExpressionQueryOptions(1, RESULTS_SCHEMA);
    cohortSql = buildExpressionSql(expression, options);
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));


    // cohort 2 will set the correlated criteria to be 'at least 1' so the cohort will begin at the second event
    options.cohortId = 2;
    expression.additionalCriteria.criteriaList[0].occurrence.type = 2;
    expression.additionalCriteria.criteriaList[0].occurrence.count = 1;
    cohortSql = buildExpressionSql(expression, options);
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));

    // cohort 3 will set the correlated criteria to be 'at most 0' which results in same as cohort 1
    options.cohortId = 3;
    expression.additionalCriteria.criteriaList[0].occurrence.type = 1;
    expression.additionalCriteria.criteriaList[0].occurrence.count = 0;
    cohortSql = buildExpressionSql(expression, options);
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));

    // cohort 4 will look for the event that has a visit that started 1 day prior and ended 1 day after the condition event start date
    // only person 1 has this on their second condition occurrence event
    expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/cohortgeneration/correlatedCriteria/visitExpression.json"));
    options.cohortId = 4;
    cohortSql = buildExpressionSql(expression, options);
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));

    // Set the correlated to an invalid type (-99) which results in an exception
    boolean exceptionThrown = false;
    try {
      expression.additionalCriteria.criteriaList[0].occurrence.type = -99;
      cohortSql = buildExpressionSql(expression, options); // this should throw an exception
    }
    catch (RuntimeException re) {
      exceptionThrown = true;
    }

    assertTrue("Invalid cohort expression occurrence.type should have thrown an exception", exceptionThrown);

    // Validate results
    // Load actual records from cohort table
    final ITable actualTable = dbUnitCon.createQueryTable(RESULTS_SCHEMA + ".cohort", String.format("SELECT * from %s ORDER BY cohort_definition_id, subject_id, cohort_start_date", RESULTS_SCHEMA + ".cohort"));
    // Load expected data from an XML dataset
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);
    final ITable expectedTable = expectedDataSet.getTable(RESULTS_SCHEMA + ".cohort");

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedTable, actualTable);    

  }

  @Test
  public void testGroups() throws Exception  {
    final String RESULTS_SCHEMA = "groupsTest";
    final String[] testDataSetsPrep = new String[] { 
      "/datasets/vocabulary.json",
      "/cohortgeneration/correlatedCriteria/groupTest_PREP.json" 
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // prepare results schema for the specified options.resultSchema
    prepareSchema(RESULTS_SCHEMA, RESULTS_DDL_PATH);

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    CohortExpressionQueryBuilder.BuildExpressionQueryOptions options;
    CohortExpression expression;   
    String cohortSql;

    // this expression has groups defined as: (A OR B OR (C AND D))
    // Cohort 1: Persons 1,2,3 satisfy these criteria through different branches
    expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/cohortgeneration/correlatedCriteria/groupExpression.json"));
    options = buildExpressionQueryOptions(1, RESULTS_SCHEMA);
    cohortSql = buildExpressionSql(expression, options);
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));

    // Cohort 2: Change outer group to 'at least 2'
    // person 1 satisfies A, B and (C AND D)
    expression.additionalCriteria.type = "AT_LEAST";
    expression.additionalCriteria.count = 2;
    options.cohortId = 2;
    cohortSql = buildExpressionSql(expression, options);
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));

    // cohort 3: change outer group to 'at most 1'
    // person 2 and 3 only have 1: 2 has A and 3 has (C AND D)
    expression.additionalCriteria.type = "AT_MOST";
    expression.additionalCriteria.count = 2;
    options.cohortId = 3;
    cohortSql = buildExpressionSql(expression, options);
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));


    // cohort 4: outer group 'ALL', change inner group to 'at most 0'
    // No one passes, since that arrangement is a contradiction
    expression.additionalCriteria.type = "ALL";
    expression.additionalCriteria.groups[0].type = "AT_MOST";
    expression.additionalCriteria.groups[0].count = 0;
    options.cohortId = 4;
    cohortSql = buildExpressionSql(expression, options);
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));

    // cohort 5: remove inner group, an empty group should have no impact
    // Person 1 has both A and B
    expression.additionalCriteria.groups = new CriteriaGroup[0];
    options.cohortId = 5;
    cohortSql = buildExpressionSql(expression, options);
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));

    // Validate results
    // Load actual records from cohort table
    final ITable actualTable = dbUnitCon.createQueryTable(RESULTS_SCHEMA + ".cohort", String.format("SELECT * from %s ORDER BY cohort_definition_id, subject_id, cohort_start_date", RESULTS_SCHEMA + ".cohort"));
    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[] {"/cohortgeneration/correlatedCriteria/groupTest_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);
    final ITable expectedTable = expectedDataSet.getTable(RESULTS_SCHEMA + ".cohort");

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedTable, actualTable);    
  }
  

  /**
   * Limit tests
   */
  @Test
  public void testLimits() throws Exception {
    final String RESULTS_SCHEMA = "limitTest";
    final String[] testDataSetsPrep = new String[] { 
      "/datasets/vocabulary.json",
      "/cohortgeneration/limits/limitTest_PREP.json" 
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // prepare results schema for the specified options.resultSchema
    prepareSchema(RESULTS_SCHEMA, RESULTS_DDL_PATH);

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    CohortExpressionQueryBuilder.BuildExpressionQueryOptions options;
    CohortExpression expression;   
    String cohortSql;

    // load the default expression: 
    // all events of CO, restrited to events with a prior CO, initial all events, qualifying all events.  
    // fixed duration of event: 1 day after start date
    // gap window 60 days

    // cohort 1 will use the default expression from JSON.
    expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/cohortgeneration/limits/limitExpression.json"));
    options = buildExpressionQueryOptions(1, RESULTS_SCHEMA);
    cohortSql = buildExpressionSql(expression, options);
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));

    // cohort 2 will limit the final events to earliest.
    expression.expressionLimit.type = "first";
    options.cohortId = 2;
    cohortSql = buildExpressionSql(expression, options);
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));

    // cohort 3 will limit the qualified events to earliest.
    expression.qualifiedLimit.type = "first";
    options.cohortId = 3;
    cohortSql = buildExpressionSql(expression, options);
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));

    // cohort 4 will limit the initial events to earliest.
    expression.primaryCriteria.primaryLimit.type = "first";
    options.cohortId = 4;
    cohortSql = buildExpressionSql(expression, options);
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));

    // cohort 5 will use the last event of the initial events
    expression.primaryCriteria.primaryLimit.type = "last";
    options.cohortId = 5;
    cohortSql = buildExpressionSql(expression, options);
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));

    // Validate results
    // Load actual records from cohort table
    final ITable actualTable = dbUnitCon.createQueryTable(RESULTS_SCHEMA + ".cohort", String.format("SELECT * from %s ORDER BY cohort_definition_id, subject_id, cohort_start_date", RESULTS_SCHEMA + ".cohort"));
    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[] {"/cohortgeneration/limits/limitTest_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);
    final ITable expectedTable = expectedDataSet.getTable(RESULTS_SCHEMA + ".cohort");

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedTable, actualTable);    

  }

  /**
   * Inclusion rule tests
   */

  @Test
  public void testSimpleInclusionRule() throws Exception  {
    final String RESULTS_SCHEMA = "simpleInclusionRule";
    final String[] testDataSetsPrep = new String[] { 
      "/datasets/vocabulary.json",
      "/cohortgeneration/inclusionRules/simpleInclusionRule_PREP.json" 
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // prepare results schema for the specified options.resultSchema
    prepareSchema(RESULTS_SCHEMA, RESULTS_DDL_PATH);

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    CohortExpressionQueryBuilder.BuildExpressionQueryOptions options;
    CohortExpression expression;   
    String cohortSql;
    
    // load the default expression, which looks for the event with exactly 0 conceptSet = 1 between all days beore and 0 days before index
    // cohort 1 will use the default expression from JSON.
    expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/cohortgeneration/inclusionRules/simpleInclusionRule.json"));
    options = buildExpressionQueryOptions(1, RESULTS_SCHEMA);
    cohortSql = buildExpressionSql(expression, options);
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));

    // Validate results
    // Load actual records from cohort table
    final ITable cohortTable = dbUnitCon.createQueryTable(RESULTS_SCHEMA + ".cohort", String.format("SELECT * from %s ORDER BY cohort_definition_id, subject_id, cohort_start_date", RESULTS_SCHEMA + ".cohort"));
    final ITable censorStatsTable = dbUnitCon.createQueryTable(RESULTS_SCHEMA + ".cohort_inclusion_result", String.format("SELECT * from %s ORDER BY cohort_definition_id, mode_id, inclusion_rule_mask", RESULTS_SCHEMA + ".cohort_inclusion_result"));
    final IDataSet actualDataSet = new CompositeDataSet(new ITable[] {cohortTable, censorStatsTable});

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[] {"/cohortgeneration/inclusionRules/simpleInclusionRule_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedDataSet, actualDataSet);     

  }

  /**
   *  Exit strategies and censoring events
   */
  @Test
  public void testContinuousExposure() throws Exception{
    final String RESULTS_SCHEMA = "continuousExposure";
    final String[] testDataSetsPrep = new String[] { 
      "/datasets/vocabulary.json",
      "/cohortgeneration/exits/continuousExposure_PREP.json" 
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // prepare results schema for the specified options.resultSchema
    prepareSchema(RESULTS_SCHEMA, RESULTS_DDL_PATH);

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    CohortExpressionQueryBuilder.BuildExpressionQueryOptions options;
    CohortExpression expression;   
    String cohortSql;

    // load the default expression: 
    // all drug exposure events, exit with end of continuous exposure with a persistence window of 60 days.

    // cohort 1 will use the default expression from JSON.
    expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/cohortgeneration/exits/continuousExposureExpression.json"));
    options = buildExpressionQueryOptions(1, RESULTS_SCHEMA);
    cohortSql = buildExpressionSql(expression, options);
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));

    // cohort 2 will limit to earliest event
    expression.primaryCriteria.primaryLimit.type="First";
    options.cohortId = 2;
    cohortSql = buildExpressionSql(expression, options);
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));

    // check for validation error by removing the codesetID from exit strategy
    boolean exceptionThrown = false;
    CustomEraStrategy strategy = (CustomEraStrategy)expression.endStrategy;
    strategy.drugCodesetId = null;
    try {
      options.cohortId = 3;
      cohortSql = buildExpressionSql(expression, options);
      // execute on database, expect no errors
      jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));
  
    } catch (RuntimeException re)
    {
      exceptionThrown = true;
    }
    assertTrue("A NULL continuous exposure concept ID results in an exception.", exceptionThrown);


    // Validate results
    // Load actual records from cohort table
    final ITable actualTable = dbUnitCon.createQueryTable(RESULTS_SCHEMA + ".cohort", String.format("SELECT * from %s ORDER BY cohort_definition_id, subject_id, cohort_start_date", RESULTS_SCHEMA + ".cohort"));
    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[] {"/cohortgeneration/exits/continuousExposure_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);
    final ITable expectedTable = expectedDataSet.getTable(RESULTS_SCHEMA + ".cohort");

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedTable, actualTable);        

  }
  @Test
  public void testContinuousExposureCensor() throws Exception{
    final String RESULTS_SCHEMA = "continuousExposureCensor";
    final String[] testDataSetsPrep = new String[] { 
      "/datasets/vocabulary.json",
      "/cohortgeneration/exits/continuousExposureCensor_PREP.json" 
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // prepare results schema for the specified options.resultSchema
    prepareSchema(RESULTS_SCHEMA, RESULTS_DDL_PATH);

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    CohortExpressionQueryBuilder.BuildExpressionQueryOptions options;
    CohortExpression expression;   
    String cohortSql;

    // load the default expression: 
    // all drug exposure events, exit with end of continuous exposure with a persistence window of 60 days.

    // cohort 1 will use the default expression from JSON.
    expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/cohortgeneration/exits/continuousExposureCensorExpression.json"));
    options = buildExpressionQueryOptions(1, RESULTS_SCHEMA);
    cohortSql = buildExpressionSql(expression, options);
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));

    // Validate results
    // Load actual records from cohort table
    final ITable actualTable = dbUnitCon.createQueryTable(RESULTS_SCHEMA + ".cohort", String.format("SELECT * from %s ORDER BY cohort_definition_id, subject_id, cohort_start_date", RESULTS_SCHEMA + ".cohort"));
    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[] {"/cohortgeneration/exits/continuousExposureCensor_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);
    final ITable expectedTable = expectedDataSet.getTable(RESULTS_SCHEMA + ".cohort");

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedTable, actualTable);        

  }

  @Test
  public void testFixedOffset() throws Exception {
    final String RESULTS_SCHEMA = "fixedOffset";
    final String[] testDataSetsPrep = new String[] { 
      "/datasets/vocabulary.json",
      "/cohortgeneration/exits/fixedOffset_PREP.json" 
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // prepare results schema for the specified options.resultSchema
    prepareSchema(RESULTS_SCHEMA, RESULTS_DDL_PATH);

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    CohortExpressionQueryBuilder.BuildExpressionQueryOptions options;
    CohortExpression expression;   
    String cohortSql;

    // load the default expression: 
    // all drug exposure events, fixed offset of endDate + 31 days.

    // cohort 1 will use the default expression from JSON.
    expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/cohortgeneration/exits/fixedOffsetExpression.json"));
    options = buildExpressionQueryOptions(1, RESULTS_SCHEMA);
    cohortSql = buildExpressionSql(expression, options);
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));

    // Validate results
    // Load actual records from cohort table
    final ITable actualTable = dbUnitCon.createQueryTable(RESULTS_SCHEMA + ".cohort", String.format("SELECT * from %s ORDER BY cohort_definition_id, subject_id, cohort_start_date", RESULTS_SCHEMA + ".cohort"));
    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[] {"/cohortgeneration/exits/fixedOffset_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);
    final ITable expectedTable = expectedDataSet.getTable(RESULTS_SCHEMA + ".cohort");

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedTable, actualTable);

  }

  @Test
  public void testFixedOffsetCensor() throws Exception {
    final String RESULTS_SCHEMA = "fixedOffsetCensor";
    final String[] testDataSetsPrep = new String[] { 
      "/datasets/vocabulary.json",
      "/cohortgeneration/exits/fixedOffsetCensor_PREP.json" 
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // prepare results schema for the specified options.resultSchema
    prepareSchema(RESULTS_SCHEMA, RESULTS_DDL_PATH);

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    CohortExpressionQueryBuilder.BuildExpressionQueryOptions options;
    CohortExpression expression;   
    String cohortSql;

    // load the default expression: 
    // all drug exposure events, fixed offset of endDate + 31 days.

    // cohort 1 will use the default expression from JSON.
    expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/cohortgeneration/exits/fixedOffsetCensorExpression.json"));
    options = buildExpressionQueryOptions(1, RESULTS_SCHEMA);
    cohortSql = buildExpressionSql(expression, options);
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));

    // Validate results
    // Load actual records from cohort table
    final ITable actualTable = dbUnitCon.createQueryTable(RESULTS_SCHEMA + ".cohort", String.format("SELECT * from %s ORDER BY cohort_definition_id, subject_id, cohort_start_date", RESULTS_SCHEMA + ".cohort"));
    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[] {"/cohortgeneration/exits/fixedOffsetCensor_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);
    final ITable expectedTable = expectedDataSet.getTable(RESULTS_SCHEMA + ".cohort");

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedTable, actualTable);

  }

  @Test
  public void testCensorEvents() throws Exception {
    final String RESULTS_SCHEMA = "censorEvent";
    final String[] testDataSetsPrep = new String[] { 
      "/datasets/vocabulary.json",
      "/cohortgeneration/exits/censorEvent_PREP.json" 
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // prepare results schema for the specified options.resultSchema
    prepareSchema(RESULTS_SCHEMA, RESULTS_DDL_PATH);

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    CohortExpressionQueryBuilder.BuildExpressionQueryOptions options;
    CohortExpression expression;   
    String cohortSql;

    // load the default expression: 
    // all drug exposure events, fixed duration of endDate + 31 days, censored at any procedure occurrence.

    // cohort 1 will use the default expression from JSON.
    expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/cohortgeneration/exits/censorEventExpression.json"));
    options = buildExpressionQueryOptions(1, RESULTS_SCHEMA);
    cohortSql = buildExpressionSql(expression, options);
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));

    // Validate results
    // Load actual records from cohort table
    final ITable actualTable = dbUnitCon.createQueryTable(RESULTS_SCHEMA + ".cohort", String.format("SELECT * from %s ORDER BY cohort_definition_id, subject_id, cohort_start_date", RESULTS_SCHEMA + ".cohort"));
    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[] {"/cohortgeneration/exits/censorEvent_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);
    final ITable expectedTable = expectedDataSet.getTable(RESULTS_SCHEMA + ".cohort");

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedTable, actualTable);    
  }
  
  /**
   * Test censor windows
   */
  @Test
  public void testCensorWindow() throws Exception {
    final String RESULTS_SCHEMA = "censorWindow";
    final String[] testDataSetsPrep = new String[] { 
      "/datasets/vocabulary.json",
      "/cohortgeneration/censorWindow/censorWindow_PREP.json" 
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // prepare results schema for the specified options.resultSchema
    prepareSchema(RESULTS_SCHEMA, RESULTS_DDL_PATH);

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    CohortExpressionQueryBuilder.BuildExpressionQueryOptions options;
    CohortExpression expression;   
    String cohortSql;

    // load the default expression: 
    // condition occurrence persistend for 180 days, censor window between 2000-04-01 to 2000-09-01
    // cohort 1 will use the default expression from JSON.
    expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/cohortgeneration/censorWindow/censorWindowExpression.json"));
    options = buildExpressionQueryOptions(1, RESULTS_SCHEMA);
    cohortSql = buildExpressionSql(expression, options);
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));

    // Validate results
    // Load actual records from cohort table
    final ITable cohortTable = dbUnitCon.createQueryTable(RESULTS_SCHEMA + ".cohort", String.format("SELECT * from %s ORDER BY cohort_definition_id, subject_id, cohort_start_date", RESULTS_SCHEMA + ".cohort"));
    final ITable censorStatsTable = dbUnitCon.createQueryTable(RESULTS_SCHEMA + ".cohort_censor_stats", String.format("SELECT * from %s ORDER BY cohort_definition_id", RESULTS_SCHEMA + ".cohort_censor_stats"));
    final IDataSet actualDataSet = new CompositeDataSet(new ITable[] {cohortTable, censorStatsTable});

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[] {"/cohortgeneration/censorWindow/censorWindow_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedDataSet, actualDataSet);     
  }
  
  /**
   * Other Tests
   */
  @Test
  public void testMixedConceptsets() throws Exception {
    final String RESULTS_SCHEMA = "mixedConceptsets";
    final String[] testDataSetsPrep = new String[] { 
      "/datasets/vocabulary.json",
      "/cohortgeneration/mixedConceptsets/mixedConceptsets_PREP.json" 
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // prepare results schema for the specified options.resultSchema
    prepareSchema(RESULTS_SCHEMA, RESULTS_DDL_PATH);

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    CohortExpressionQueryBuilder.BuildExpressionQueryOptions options;
    CohortExpression expression;   
    String cohortSql;

    // load the default expression: 
    // all drug exposure events, fixed offset of endDate + 31 days.

    // cohort 1 will use the default expression from JSON.
    expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/cohortgeneration/mixedConceptsets/mixedConceptsetsExpression.json"));
    options = buildExpressionQueryOptions(1, RESULTS_SCHEMA);
    cohortSql = buildExpressionSql(expression, options);
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));

    // Validate results
    // Load actual records from cohort table
    final ITable actualTable = dbUnitCon.createQueryTable(RESULTS_SCHEMA + ".cohort", String.format("SELECT * from %s ORDER BY cohort_definition_id, subject_id, cohort_start_date", RESULTS_SCHEMA + ".cohort"));
    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[] {"/cohortgeneration/mixedConceptsets/mixedConceptsets_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);
    final ITable expectedTable = expectedDataSet.getTable(RESULTS_SCHEMA + ".cohort");

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedTable, actualTable);

  }
  @Test
  public void testEraWithDupes() throws Exception {
    final String RESULTS_SCHEMA = "eraDupes";
    final String[] testDataSetsPrep = new String[] { 
      "/datasets/vocabulary.json",
      "/cohortgeneration/eraDupes/eraDupes_PREP.json" 
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // prepare results schema for the specified options.resultSchema
    prepareSchema(RESULTS_SCHEMA, RESULTS_DDL_PATH);

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    CohortExpressionQueryBuilder.BuildExpressionQueryOptions options;
    CohortExpression expression;   
    String cohortSql;

    // load the default expression: 
    // all drug exposure events, fixed offset of endDate + 31 days.

    // cohort 1 will use the default expression from JSON.
    expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/cohortgeneration/eraDupes/eraDupesExpression.json"));
    options = buildExpressionQueryOptions(1, RESULTS_SCHEMA);
    cohortSql = buildExpressionSql(expression, options);
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));

    // Validate results
    // Load actual records from cohort table
    final ITable actualTable = dbUnitCon.createQueryTable(RESULTS_SCHEMA + ".cohort", String.format("SELECT * from %s ORDER BY cohort_definition_id, subject_id, cohort_start_date", RESULTS_SCHEMA + ".cohort"));
    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[] {"/cohortgeneration/eraDupes/eraDupes_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);
    final ITable expectedTable = expectedDataSet.getTable(RESULTS_SCHEMA + ".cohort");

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedTable, actualTable);

  }
}
