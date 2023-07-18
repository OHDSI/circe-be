/*
 * Copyright 2022 cknoll1.
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
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.dbunit.Assertion;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.SortedTable;
import org.dbunit.operation.DatabaseOperation;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ohdsi.circe.AbstractDatabaseTest;
import org.ohdsi.circe.cohortdefinition.ConditionEra;
import org.ohdsi.circe.cohortdefinition.ConditionOccurrence;
import org.ohdsi.circe.cohortdefinition.DateAdjustment;
import org.ohdsi.circe.cohortdefinition.Death;
import org.ohdsi.circe.cohortdefinition.DeviceExposure;
import org.ohdsi.circe.cohortdefinition.DoseEra;
import org.ohdsi.circe.cohortdefinition.DrugEra;
import org.ohdsi.circe.cohortdefinition.DrugExposure;
import org.ohdsi.circe.cohortdefinition.Measurement;
import org.ohdsi.circe.cohortdefinition.Observation;
import org.ohdsi.circe.cohortdefinition.ObservationPeriod;
import org.ohdsi.circe.cohortdefinition.PayerPlanPeriod;
import org.ohdsi.circe.cohortdefinition.Period;
import org.ohdsi.circe.cohortdefinition.ProcedureOccurrence;
import org.ohdsi.circe.cohortdefinition.VisitDetail;
import org.ohdsi.circe.cohortdefinition.VisitOccurrence;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author cknoll1
 */
public class CriteriaQuery_5_3_0_Test extends AbstractDatabaseTest {

  private final static Logger log = LoggerFactory.getLogger(CriteriaQuery_5_3_0_Test.class);
  private static final String CDM_DDL_PATH = "/ddl/cdm_v5.3.sql";
  private static final String TEMP_DDL_PATH = "/criteria/temp.sql";

  private String renderQuery(String query) {
    String result = StringUtils.replace(query, "#Codesets", "temp.codesets");
    return SqlRender.renderSql(SqlTranslate.translateSql(result, "postgresql"),
            new String[]{"cdm_database_schema", "vocabulary_database_schema"},
            new String[]{"cdm", "cdm"});
  }

  private DateAdjustment createDateAdjustment(DateAdjustment.DateType startWith, 
          int startOffset, 
          DateAdjustment.DateType endWith,
          int endOffset) {
    DateAdjustment dateAdjustment = new DateAdjustment();
    dateAdjustment.startWith = startWith;
    dateAdjustment.startOffset = startOffset;
    dateAdjustment.endWith = endWith;
    dateAdjustment.endOffset = endOffset;
    
    return dateAdjustment;
  }
  @BeforeClass
  public static void beforeClass() {
    jdbcTemplate = new JdbcTemplate(getDataSource());
    prepareSchema("cdm", CDM_DDL_PATH);
    prepareSchema("temp", TEMP_DDL_PATH);
  }

  @Test
  public void cdmSchemaWasCreated() {
    jdbcTemplate.queryForList("select count(*) as c from cdm.PERSON");
  }

  @Test
  public void testVisitDetailDateOffset() throws Exception {
    final String[] testDataSetsPrep = new String[]{
      "/datasets/vocabulary.json",
      "/criteria/codesets.json",
      "/criteria/visitDetailDateOffset_PREP.json"
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    // capture results in an ArrayList
    ArrayList<ITable> actualTables = new ArrayList<>();

    // Test 1: simple query with no special conditions
    VisitDetail criteria = new VisitDetail();
    criteria.codesetId = 1;

    VisitDetailSqlBuilder<VisitDetail> builder = new VisitDetailSqlBuilder<>();
    String query = renderQuery(builder.getCriteriaSql(criteria));

    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("visitDetail.simple", query), new String[]{"person_id", "start_date"}));

    // Test 2: adjust start and end dates
    criteria.dateAdjustment = createDateAdjustment(DateAdjustment.DateType.START_DATE, 15, DateAdjustment.DateType.END_DATE, -10);

    query = renderQuery(builder.getCriteriaSql(criteria));
    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("visitDetail.date_adjust", query), new String[]{"person_id", "start_date"}));

    // Validate results:
    final IDataSet actualDataSet = new CompositeDataSet(actualTables.toArray(new ITable[]{})); // put actual results into a CompositeDataSet

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[]{"/criteria/visitDetailDateOffset_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedDataSet, actualDataSet);
  }
}
