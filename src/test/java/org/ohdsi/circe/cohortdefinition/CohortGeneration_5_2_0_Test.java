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
package org.ohdsi.circe.cohortdefinition;

import com.github.mjeanroy.dbunit.core.dataset.DataSetFactory;
import org.dbunit.Assertion;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.operation.DatabaseOperation;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ohdsi.circe.AbstractDatabaseTest;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.circe.vocabulary.Concept;
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
public class CohortGeneration_5_2_0_Test extends AbstractDatabaseTest {

  private final static Logger log = LoggerFactory.getLogger(CohortGeneration_5_2_0_Test.class);
  private static final String CDM_DDL_PATH = "/ddl/cdm_v5.2.sql";
  private static final String RESULTS_DDL_PATH = "/ddl/resultsSchema.sql";

  private static CohortExpressionQueryBuilder.BuildExpressionQueryOptions buildExpressionQueryOptions(
      final int cohortId, final String resultsSchema) {
    final CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = new CohortExpressionQueryBuilder.BuildExpressionQueryOptions();
    options.cdmSchema = "cdm";
    options.cohortId = cohortId;
    options.generateStats = true;
    options.resultSchema = resultsSchema;
    options.targetTable = resultsSchema + ".cohort";
    options.retainCohortCovariates = false;
    options.sourceKey = "'CDM'";

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
  public void testConditionStatusCriteria() throws Exception  {
    final String RESULTS_SCHEMA = "conditionStatusCriteria";
    final String[] testDataSetsPrep = new String[] { 
      "/datasets/vocabulary.json",
      "/cohortgeneration/conditionOccurrence/conditionStatusTest_PREP.json" 
    };
    final String[] testDataSetsVerify = new String[] {"/cohortgeneration/conditionOccurrence/conditionStatusTest_VERIFY.json"};
    final IDatabaseConnection dbUnitCon = getConnection();

    // prepare results schema for the specified options.resultSchema
    prepareSchema(RESULTS_SCHEMA, RESULTS_DDL_PATH);

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    
    // create 3 cohorts: one looking for a conditionStatusConcept of NULL, zero-length array, and '1'
    String cohortSql = null;
    CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = null;
    // null case: id = 1
    CohortExpression expression = new CohortExpression();   
    expression.conceptSets = new ConceptSet[0];
    expression.primaryCriteria = new PrimaryCriteria();
    expression.primaryCriteria.observationWindow = new ObservationFilter();
    final ConditionOccurrence c = new ConditionOccurrence();
    expression.primaryCriteria.criteriaList = new Criteria[] { c };

    options = buildExpressionQueryOptions(1, RESULTS_SCHEMA);
    cohortSql = buildExpressionSql(expression, options);
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));

    // zero-length case: id = 2
    c.conditionStatus = new Concept[0];
    options = buildExpressionQueryOptions(2, RESULTS_SCHEMA);
    cohortSql = buildExpressionSql(expression, options);
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));

    // condition_status = 1
    Concept conditionStatusConcept = new Concept();
    conditionStatusConcept.conceptId = 1L;
    c.conditionStatus = new Concept[] {conditionStatusConcept};
    options = buildExpressionQueryOptions(3, RESULTS_SCHEMA);
    cohortSql = buildExpressionSql(expression, options);
    // execute on database, expect no errors
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(cohortSql));

    // Validate results
    // Load actual records from cohort table
    final ITable actualTable = dbUnitCon.createQueryTable(RESULTS_SCHEMA + ".cohort", String.format("SELECT * from %s ORDER BY cohort_definition_id, subject_id, cohort_start_date", RESULTS_SCHEMA + ".cohort"));
    // Load expected data from an XML dataset
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);
    final ITable expectedTable = expectedDataSet.getTable(RESULTS_SCHEMA + ".cohort");

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedTable, actualTable);

  }
  
}
