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
package org.ohdsi.circe.cohortdefinition.builders;

import com.github.mjeanroy.dbunit.core.dataset.DataSetFactory;
import java.util.Arrays;
import java.util.List;
import org.dbunit.Assertion;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.operation.DatabaseOperation;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ohdsi.circe.AbstractDatabaseTest;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.circe.cohortdefinition.ConditionEra;
import org.ohdsi.circe.cohortdefinition.ConditionOccurrence;
import org.ohdsi.circe.cohortdefinition.WindowedCriteria;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author cknoll1
 */
public class WindowCriteria_5_0_0_Test  extends AbstractDatabaseTest {
  private final static Logger LOG = LoggerFactory.getLogger(WindowCriteria_5_0_0_Test.class);  
  private static final String CDM_DDL_PATH = "/ddl/cdm_v5.0.sql";
  private static final String RESULTS_DDL_PATH = "/ddl/resultsSchema.sql";

  @BeforeClass
  public static void beforeClass() {
    jdbcTemplate = new JdbcTemplate(getDataSource());
    prepareSchema("cdm", CDM_DDL_PATH);
  }  

  private void performWindowTest(WindowedCriteria wc, 
          String resultsSchema, 
          String[] prepSets, 
          String[] verifySets,
          List<CriteriaColumn> additionalColumns) throws Exception {
    final CohortExpressionQueryBuilder queryBuilder = new CohortExpressionQueryBuilder();

    // prepare results schema for the specified options.resultSchema
    prepareSchema(resultsSchema, RESULTS_DDL_PATH);

    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(prepSets);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"
    
    // translate to PG
    String eventTable = String.format(CriteriaUtils.EVENT_TABLE_TEMPLATE, resultsSchema + ".cohort", "cdm", 1);
    String query = queryBuilder.getWindowedCriteriaQuery(wc, eventTable);
    String translatedSql = SqlRender.renderSql(SqlTranslate.translateSql(query, "postgresql"),
            new String[] {"cdm_database_schema"}, 
            new String[] {"cdm"});
    
    // Validate results
    // Load actual records from cohort table
    final ITable actualTable = dbUnitCon.createQueryTable(resultsSchema + ".no_columns", translatedSql);
    // Load expected data from an XML dataset
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(verifySets);
    final ITable expectedTable = expectedDataSet.getTable(resultsSchema + ".no_columns");

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedTable, actualTable);
    
    // requesting additional columns
    BuilderOptions options = new BuilderOptions();
    options.additionalColumns = additionalColumns;
    String queryWithColumns = queryBuilder.getWindowedCriteriaQuery(wc, eventTable,options);
    String translatedWithColumnsSql = SqlRender.renderSql(SqlTranslate.translateSql(queryWithColumns, "postgresql"),
            new String[] {"cdm_database_schema"}, 
            new String[] {"cdm"});
    // Validate results
    // Load actual records from cohort table
    final ITable actualAdditionalColumnTable = dbUnitCon.createQueryTable(resultsSchema + ".add_columns", translatedWithColumnsSql);
    // Load expected data from an XML dataset
    final ITable expectedAdditionalColumnTable = expectedDataSet.getTable(resultsSchema + ".add_columns");
    // Assert actual database table match expected table
    Assertion.assertEquals(expectedAdditionalColumnTable, actualAdditionalColumnTable);    

  }

  @Test
  public void windowConditionEraTest() throws Exception {

    final String resultsSchema = "window_condition_era";
    final String[] testDataSetsPrep = new String[] { "/datasets/vocabulary.json",
      "/windowcriteria/windowConditionEra_PREP.json"};
    final String[] testDataSetsVerify = new String[] {"/windowcriteria/windowConditionEra_VERIFY.json"};
    WindowedCriteria wc = new WindowedCriteria();
    wc.criteria = new ConditionEra(); // find any condition era
    wc.startWindow = CriteriaUtils.getPrior365Window();
    List<CriteriaColumn> additionalColumns = Arrays.asList(CriteriaColumn.START_DATE, CriteriaColumn.END_DATE, CriteriaColumn.DOMAIN_CONCEPT, CriteriaColumn.ERA_OCCURRENCES);
    this.performWindowTest(wc, resultsSchema, testDataSetsPrep, testDataSetsVerify, additionalColumns);

  }

  @Test
  public void windowConditionOccurrenceTest() throws Exception {

    final String resultsSchema = "window_condition_occurrence";
    final String[] testDataSetsPrep = new String[] { "/datasets/vocabulary.json",
      "/windowcriteria/windowConditionOccurrence_PREP.json"};
    final String[] testDataSetsVerify = new String[] {"/windowcriteria/windowConditionOccurrence_VERIFY.json"};
    WindowedCriteria wc = new WindowedCriteria();
    wc.criteria = new ConditionOccurrence(); // find any condition occurence
    wc.startWindow = CriteriaUtils.getPrior365Window();
    List<CriteriaColumn> additionalColumns = Arrays.asList(CriteriaColumn.START_DATE, CriteriaColumn.END_DATE, CriteriaColumn.DOMAIN_CONCEPT);
    this.performWindowTest(wc, resultsSchema, testDataSetsPrep, testDataSetsVerify, additionalColumns);

  }

}
