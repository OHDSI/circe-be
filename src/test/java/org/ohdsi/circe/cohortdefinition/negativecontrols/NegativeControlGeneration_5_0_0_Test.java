/*
 * Copyright 2020 cknoll1.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ohdsi.circe.cohortdefinition.negativecontrols;

import com.github.mjeanroy.dbunit.core.dataset.DataSetFactory;
import java.util.ArrayList;
import java.util.List;
import org.dbunit.Assertion;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.operation.DatabaseOperation;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ohdsi.circe.AbstractDatabaseTest;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author cknoll1
 */
public class NegativeControlGeneration_5_0_0_Test extends AbstractDatabaseTest {
  private final static Logger log = LoggerFactory.getLogger(NegativeControlGeneration_5_0_0_Test.class);
  private static final String CDM_DDL_PATH = "/ddl/cdm_v5.0.sql";
  private static final String RESULTS_DDL_PATH = "/ddl/resultsSchema.sql";

  @BeforeClass
  public static void beforeClass() {
    jdbcTemplate = new JdbcTemplate(getDataSource());
    prepareSchema("cdm", CDM_DDL_PATH);
  }
  
  @Test
  public void buildEmptyExpressionQuery() throws Exception {
    boolean exceptionThrown = false;
    CohortExpressionQueryBuilder queryBuilder = new CohortExpressionQueryBuilder();
    OutcomeCohortExpression cohortExpression = new OutcomeCohortExpression();
    try {
      queryBuilder.buildExpressionQuery(cohortExpression);
    } catch (Exception re) {
      exceptionThrown = true;
    }
    assertTrue("A NULL cohort expression results in an exception.", exceptionThrown);
  }
  
  @Test
  public void getDomainQueryExceptionTest() throws Exception {
    boolean exceptionThrown = false;
    CohortExpressionQueryBuilder queryBuilder = new CohortExpressionQueryBuilder();
    try {
      ArrayList<String> domainIds = new ArrayList<>();
      domainIds.add("Some Invalid Domain Name");
      queryBuilder.getDomainQuery(domainIds, true);
      } catch (Exception re)
    {
      exceptionThrown = true;
    }
    assertTrue("A NULL domain id list results in an exception.", exceptionThrown);
  }
  
  
  @Test
  public void outcomeCohortExpressionDeseralizationTest() throws Exception {
    final String json = "{\"occurrenceType\": \"all\",\"detectOnDescendants\": true,\"domains\": [\"condition\",\"procedure\"]}";
    OutcomeCohortExpression cohortExpression = OutcomeCohortExpression.fromJson(json);
    assertTrue(cohortExpression.detectOnDescendants == true && 
               cohortExpression.occurrenceType == OccurrenceType.ALL &&
               cohortExpression.domains.size() == 2);
  }
  
  @Test
  public void outcomeCohortExpressionDeseralizationExceptionTest() throws Exception {
    boolean exceptionThrown = false;
    try {
      OutcomeCohortExpression.fromJson(null);
    } catch (Exception re) {
      exceptionThrown = true;
    }
    assertTrue("Parsing an invalid outcome cohort expression JSON results in an exception.", exceptionThrown);
  }
  
  @Test
  public void occurrenceTypeNullTest() throws Exception {
    assertTrue(OccurrenceType.fromString("Some invalid string") == null);
  }
  
  @Test
  public void getEmptyDomainConfigurationTest() throws Exception {
    boolean exceptionThrown = false;
    try {
      CohortExpressionQueryBuilder queryBuilder = new CohortExpressionQueryBuilder();
      String query = queryBuilder.getDomainQuery(1, "invalidDomain", false);
    } catch (Exception re) {
      exceptionThrown = true;
    }
    assertTrue("Passing an invalid domain id for the domain query results in an exception.", exceptionThrown);
  }
  
  @Test
  public void firstOccurrenceTest() throws Exception {
    final String RESULTS_SCHEMA = "negFirstOccurrenceTest";
    final String[] testDataSetsPrep = new String[] { 
      "/datasets/vocabulary.json",
      "/negativecontrols/firstOccurrence/firstOccurrenceTest_PREP.json" 
    };

    final String[] testDataSetsVerify = new String[] {"/negativecontrols/firstOccurrence/firstOccurrenceTest_VERIFY.json"};
    final IDatabaseConnection dbUnitCon = getConnection();

    // prepare results schema for the specified options.resultSchema
    prepareSchema(RESULTS_SCHEMA, RESULTS_DDL_PATH);

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"
    
    // Perform test
    OutcomeCohortExpression cohortExpression = new OutcomeCohortExpression();
    cohortExpression.occurrenceType = OccurrenceType.FIRST;
    cohortExpression.domains.add("condition");
    cohortExpression.detectOnDescendants = true;
    String sql = buildExpressionSql(cohortExpression, RESULTS_SCHEMA, "1"); // 1 == ancestor concept id
    
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(sql));

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
  public void allOccurrencesTest() throws Exception {
    final String RESULTS_SCHEMA = "negAllOccurrencesTest";
    final String[] testDataSetsPrep = new String[] { 
      "/datasets/vocabulary.json",
      "/negativecontrols/allOccurrences/allOccurrencesTest_PREP.json" 
    };

    final String[] testDataSetsVerify = new String[] {"/negativecontrols/allOccurrences/allOccurrencesTest_VERIFY.json"};
    final IDatabaseConnection dbUnitCon = getConnection();

    // prepare results schema for the specified options.resultSchema
    prepareSchema(RESULTS_SCHEMA, RESULTS_DDL_PATH);

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"
    
    // Perform test
    OutcomeCohortExpression cohortExpression = new OutcomeCohortExpression();
    cohortExpression.occurrenceType = OccurrenceType.ALL;
    cohortExpression.domains.add("drug");
    cohortExpression.detectOnDescendants = true;
    String sql = buildExpressionSql(cohortExpression, RESULTS_SCHEMA, "1"); // 1 == ancestor concept id
    
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(sql));

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
  public void detectOnDescendantsTrueTest() throws Exception {
    final String RESULTS_SCHEMA = "negDetectOnDescendantsTrueTest";
    final String[] testDataSetsPrep = new String[] { 
      "/datasets/vocabulary.json",
      "/negativecontrols/detectOnDescendants/detectOnDescendantsTrueTest_PREP.json" 
    };

    final String[] testDataSetsVerify = new String[] {"/negativecontrols/detectOnDescendants/detectOnDescendantsTrueTest_VERIFY.json"};
    final IDatabaseConnection dbUnitCon = getConnection();

    // prepare results schema for the specified options.resultSchema
    prepareSchema(RESULTS_SCHEMA, RESULTS_DDL_PATH);

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"
    
    // Perform test
    OutcomeCohortExpression cohortExpression = new OutcomeCohortExpression();
    cohortExpression.occurrenceType = OccurrenceType.ALL;
    cohortExpression.domains.add("procedure");
    cohortExpression.detectOnDescendants = true;
    String sql = buildExpressionSql(cohortExpression, RESULTS_SCHEMA, "1"); // 1 == ancestor concept id
    
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(sql));

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
  public void detectOnDescendantsFalseTest() throws Exception {
    final String RESULTS_SCHEMA = "negDetectOnDescendantsFalseTest";
    final String[] testDataSetsPrep = new String[] { 
      "/datasets/vocabulary.json",
      "/negativecontrols/detectOnDescendants/detectOnDescendantsFalseTest_PREP.json" 
    };

    final String[] testDataSetsVerify = new String[] {"/negativecontrols/detectOnDescendants/detectOnDescendantsFalseTest_VERIFY.json"};
    final IDatabaseConnection dbUnitCon = getConnection();

    // prepare results schema for the specified options.resultSchema
    prepareSchema(RESULTS_SCHEMA, RESULTS_DDL_PATH);

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"
    
    // Perform test
    OutcomeCohortExpression cohortExpression = new OutcomeCohortExpression();
    cohortExpression.occurrenceType = OccurrenceType.ALL;
    cohortExpression.domains.add("observation");
    cohortExpression.detectOnDescendants = false;
    String sql = buildExpressionSql(cohortExpression, RESULTS_SCHEMA, "1"); // 1 == ancestor concept id
    
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(sql));

    // Validate results
    // Load actual records from cohort table
    final ITable actualTable = dbUnitCon.createQueryTable(RESULTS_SCHEMA + ".cohort", String.format("SELECT * from %s ORDER BY cohort_definition_id, subject_id, cohort_start_date", RESULTS_SCHEMA + ".cohort"));
    // Load expected data from an XML dataset
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);
    final ITable expectedTable = expectedDataSet.getTable(RESULTS_SCHEMA + ".cohort");

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedTable, actualTable);
  }
  
  private static String buildExpressionSql(final OutcomeCohortExpression expression,
      String targetDatabaseSchema, String outcomeId) throws Exception {
    // build SQL
    final CohortExpressionQueryBuilder builder = new CohortExpressionQueryBuilder();
    String cohortSql = builder.buildExpressionQuery(expression);

    // translate to PG
    String[] parameters = new String[] {"outcome_ids", "cdm_database_schema", "target_database_schema", "target_cohort_table"};
    String[] values = new String[] { outcomeId, "cdm", targetDatabaseSchema, "cohort" };
    cohortSql = SqlRender.renderSql(SqlTranslate.translateSql(cohortSql, "postgresql"), parameters, values);

    return cohortSql;
  }
  
}

