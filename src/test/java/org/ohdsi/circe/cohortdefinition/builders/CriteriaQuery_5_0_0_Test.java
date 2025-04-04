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
import org.ohdsi.circe.cohortdefinition.ConceptSetSelection;
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
import org.ohdsi.circe.cohortdefinition.Specimen;
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
public class CriteriaQuery_5_0_0_Test extends AbstractDatabaseTest {

  private final static Logger log = LoggerFactory.getLogger(CriteriaQuery_5_0_0_Test.class);
  private static final String CDM_DDL_PATH = "/ddl/cdm_v5.0.sql";
  private static final String TEMP_DDL_PATH = "/criteria/temp.sql";
  private static final ConceptSetSelection CONCEPTSET_1 = null;
  private static final ConceptSetSelection CONCEPTSET_2 = null;
  private static final ConceptSetSelection CONCEPTSET_3 = null;
  private static final ConceptSetSelection CONCEPTSET_4 = null;
  

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
  
  private ConceptSetSelection createConceptSetSelection(Integer id, boolean isExcluded) {
    ConceptSetSelection css = new ConceptSetSelection();
    css.codesetId = id;
    css.isExclusion = isExcluded;
    return css;
  }

  @Test
  public void cdmSchemaWasCreated() {
    jdbcTemplate.queryForList("select count(*) as c from cdm.PERSON");
  }

  @Test
  public void testConditionEraDateOffset() throws Exception {
    final String[] testDataSetsPrep = new String[]{
      "/datasets/vocabulary.json",
      "/criteria/codesets.json",
      "/criteria/conditionEraDateOffset_PREP.json"
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    // capture results in an ArrayList
    ArrayList<ITable> actualTables = new ArrayList<>();

    // Test 1: simple query with no special conditions
    ConditionEra criteria = new ConditionEra();
    criteria.codesetId = 1;

    ConditionEraSqlBuilder<ConditionEra> builder = new ConditionEraSqlBuilder<>();
    String query = renderQuery(builder.getCriteriaSql(criteria));

    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("conditionEra.simple", query), new String[]{"person_id", "start_date"}));

    // Test 2: adjust start and end dates
    criteria.dateAdjustment = createDateAdjustment(DateAdjustment.DateType.START_DATE, 15, DateAdjustment.DateType.END_DATE, -10);

    query = renderQuery(builder.getCriteriaSql(criteria));
    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("conditionEra.date_adjust", query), new String[]{"person_id", "start_date"}));

    // Validate results:
    final IDataSet actualDataSet = new CompositeDataSet(actualTables.toArray(new ITable[]{})); // put actual results into a CompositeDataSet

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[]{"/criteria/conditionEraDateOffset_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedDataSet, actualDataSet);
  }
  
  @Test
  public void testConditionEraConceptSet() throws Exception {
    final String[] testDataSetsPrep = new String[]{
      "/datasets/vocabulary.json",
      "/criteria/codesets.json",
      "/criteria/conditionEraConceptSet_PREP.json"
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    // capture results in an ArrayList
    ArrayList<ITable> actualTables = new ArrayList<>();

    // Test 1: simple query with no special conditions
    ConditionEra criteria = new ConditionEra();
    criteria.codesetId = 1;
    criteria.genderCS = createConceptSetSelection(1,false);
    ConditionEraSqlBuilder<ConditionEra> builder = new ConditionEraSqlBuilder<>();
    String query = renderQuery(builder.getCriteriaSql(criteria));

    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("conditionEra.conceptset", query), new String[]{"person_id", "start_date"}));

    // Validate results:
    final IDataSet actualDataSet = new CompositeDataSet(actualTables.toArray(new ITable[]{})); // put actual results into a CompositeDataSet

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[]{"/criteria/conditionEraConceptSet_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedDataSet, actualDataSet);
  }

  @Test
  public void testConditionOccurrenceDateOffset() throws Exception {
    final String[] testDataSetsPrep = new String[]{
      "/datasets/vocabulary.json",
      "/criteria/codesets.json",
      "/criteria/conditionOccurrenceDateOffset_PREP.json"
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    // capture results in an ArrayList
    ArrayList<ITable> actualTables = new ArrayList<>();

    // Test 1: simple query with no special conditions
    ConditionOccurrence criteria = new ConditionOccurrence();
    criteria.codesetId = 1;

    ConditionOccurrenceSqlBuilder<ConditionOccurrence> builder = new ConditionOccurrenceSqlBuilder<>();
    String query = renderQuery(builder.getCriteriaSql(criteria));

    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("conditionOccurrence.simple", query), new String[]{"person_id", "start_date"}));

    // Test 2: adjust start and end dates
    criteria.dateAdjustment = createDateAdjustment(DateAdjustment.DateType.START_DATE, 15, DateAdjustment.DateType.END_DATE, -10);

    query = renderQuery(builder.getCriteriaSql(criteria));
    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("conditionOccurrence.date_adjust", query), new String[]{"person_id", "start_date"}));

    // Validate results:
    final IDataSet actualDataSet = new CompositeDataSet(actualTables.toArray(new ITable[]{})); // put actual results into a CompositeDataSet

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[]{"/criteria/conditionOccurrenceDateOffset_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedDataSet, actualDataSet);
  }
  
  @Test
  public void testConditionOccurrenceConceptSet() throws Exception {
    final String[] testDataSetsPrep = new String[]{
      "/datasets/vocabulary.json",
      "/criteria/codesets.json",
      "/criteria/conditionOccurrenceConceptSet_PREP.json"
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    // capture results in an ArrayList
    ArrayList<ITable> actualTables = new ArrayList<>();

    // Test 1: simple query with no special conditions
    ConditionOccurrence criteria = new ConditionOccurrence();
    criteria.codesetId = 1;
    criteria.conditionTypeCS = createConceptSetSelection(1,false);
    criteria.genderCS = createConceptSetSelection(1,false);
    criteria.visitTypeCS = createConceptSetSelection(2,false);
    criteria.providerSpecialtyCS = createConceptSetSelection(3,false);
    

    ConditionOccurrenceSqlBuilder<ConditionOccurrence> builder = new ConditionOccurrenceSqlBuilder<>();
    String query = renderQuery(builder.getCriteriaSql(criteria));

    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("conditionOccurrence.conceptset", query), new String[]{"person_id", "start_date"}));

    // Validate results:
    final IDataSet actualDataSet = new CompositeDataSet(actualTables.toArray(new ITable[]{})); // put actual results into a CompositeDataSet

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[]{"/criteria/conditionOccurrenceConceptSet_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedDataSet, actualDataSet);
  }
  
  @Test
  public void testDeathDateOffset() throws Exception {
    final String[] testDataSetsPrep = new String[]{
      "/datasets/vocabulary.json",
      "/criteria/codesets.json",
      "/criteria/deathDateOffset_PREP.json"
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    // capture results in an ArrayList
    ArrayList<ITable> actualTables = new ArrayList<>();

    // Test 1: simple query with no special conditions
    Death criteria = new Death();
    criteria.codesetId = 1;

    DeathSqlBuilder<Death> builder = new DeathSqlBuilder<>();
    String query = renderQuery(builder.getCriteriaSql(criteria));

    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("death.simple", query), new String[]{"person_id", "start_date"}));

    // Test 2: adjust start and end dates
    criteria.dateAdjustment = createDateAdjustment(DateAdjustment.DateType.START_DATE, 15, DateAdjustment.DateType.END_DATE, -10);

    query = renderQuery(builder.getCriteriaSql(criteria));
    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("death.date_adjust", query), new String[]{"person_id", "start_date"}));

    // Validate results:
    final IDataSet actualDataSet = new CompositeDataSet(actualTables.toArray(new ITable[]{})); // put actual results into a CompositeDataSet

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[]{"/criteria/deathDateOffset_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedDataSet, actualDataSet);
  }

  
  @Test
  public void testDeathConceptSet() throws Exception {
    final String[] testDataSetsPrep = new String[]{
      "/datasets/vocabulary.json",
      "/criteria/codesets.json",
      "/criteria/deathConceptSet_PREP.json"
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    // capture results in an ArrayList
    ArrayList<ITable> actualTables = new ArrayList<>();

    // Test 1: simple query with no special conditions
    Death criteria = new Death();
    criteria.codesetId = 1;
    criteria.genderCS = createConceptSetSelection(1,false);
    criteria.deathTypeCS = createConceptSetSelection(1,false);

    DeathSqlBuilder<Death> builder = new DeathSqlBuilder<>();
    String query = renderQuery(builder.getCriteriaSql(criteria));

    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("death.conceptset", query), new String[]{"person_id", "start_date"}));

    // Validate results:
    final IDataSet actualDataSet = new CompositeDataSet(actualTables.toArray(new ITable[]{})); // put actual results into a CompositeDataSet

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[]{"/criteria/deathConceptSet_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedDataSet, actualDataSet);
  }

  @Test
  public void testDeviceExposureDateOffset() throws Exception {
    final String[] testDataSetsPrep = new String[]{
      "/datasets/vocabulary.json",
      "/criteria/codesets.json",
      "/criteria/deviceExposureDateOffset_PREP.json"
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    // capture results in an ArrayList
    ArrayList<ITable> actualTables = new ArrayList<>();

    // Test 1: simple query with no special conditions
    DeviceExposure criteria = new DeviceExposure();
    criteria.codesetId = 1;

    DeviceExposureSqlBuilder<DeviceExposure> builder = new DeviceExposureSqlBuilder<>();
    String query = renderQuery(builder.getCriteriaSql(criteria));

    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("deviceExposure.simple", query), new String[]{"person_id", "start_date"}));

    // Test 2: adjust start and end dates
    criteria.dateAdjustment = createDateAdjustment(DateAdjustment.DateType.START_DATE, 15, DateAdjustment.DateType.END_DATE, -10);

    query = renderQuery(builder.getCriteriaSql(criteria));
    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("deviceExposure.date_adjust", query), new String[]{"person_id", "start_date"}));

    // Validate results:
    final IDataSet actualDataSet = new CompositeDataSet(actualTables.toArray(new ITable[]{})); // put actual results into a CompositeDataSet

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[]{"/criteria/deviceExposureDateOffset_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedDataSet, actualDataSet);
  }

  @Test
  public void testDeviceExposureConceptSet() throws Exception {
    final String[] testDataSetsPrep = new String[]{
      "/datasets/vocabulary.json",
      "/criteria/codesets.json",
      "/criteria/deviceExposureConceptSet_PREP.json"
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    // capture results in an ArrayList
    ArrayList<ITable> actualTables = new ArrayList<>();

    // Test 1: simple query with no special conditions
    DeviceExposure criteria = new DeviceExposure();
    criteria.codesetId = 1;
    criteria.deviceTypeCS = createConceptSetSelection(1,false);
    criteria.genderCS = createConceptSetSelection(1,false);
    criteria.visitTypeCS = createConceptSetSelection(2,false);
    criteria.providerSpecialtyCS = createConceptSetSelection(3,false);

    DeviceExposureSqlBuilder<DeviceExposure> builder = new DeviceExposureSqlBuilder<>();
    String query = renderQuery(builder.getCriteriaSql(criteria));

    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("deviceExposure.conceptset", query), new String[]{"person_id", "start_date"}));

    // Validate results:
    final IDataSet actualDataSet = new CompositeDataSet(actualTables.toArray(new ITable[]{})); // put actual results into a CompositeDataSet

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[]{"/criteria/deviceExposureConceptSet_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedDataSet, actualDataSet);
  }

  @Test
  public void testDoseEraDateOffset() throws Exception {
    final String[] testDataSetsPrep = new String[]{
      "/datasets/vocabulary.json",
      "/criteria/codesets.json",
      "/criteria/doseEraDateOffset_PREP.json"
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    // capture results in an ArrayList
    ArrayList<ITable> actualTables = new ArrayList<>();

    // Test 1: simple query with no special conditions
    DoseEra criteria = new DoseEra();
    criteria.codesetId = 1;

    DoseEraSqlBuilder<DoseEra> builder = new DoseEraSqlBuilder<>();
    String query = renderQuery(builder.getCriteriaSql(criteria));

    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("doseEra.simple", query), new String[]{"person_id", "start_date"}));

    // Test 2: adjust start and end dates
    criteria.dateAdjustment = createDateAdjustment(DateAdjustment.DateType.START_DATE, 15, DateAdjustment.DateType.END_DATE, -10);

    query = renderQuery(builder.getCriteriaSql(criteria));
    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("doseEra.date_adjust", query), new String[]{"person_id", "start_date"}));

    // Validate results:
    final IDataSet actualDataSet = new CompositeDataSet(actualTables.toArray(new ITable[]{})); // put actual results into a CompositeDataSet

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[]{"/criteria/doseEraDateOffset_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedDataSet, actualDataSet);
  }  

  @Test
  public void testDoseEraConceptSet() throws Exception {
    final String[] testDataSetsPrep = new String[]{
      "/datasets/vocabulary.json",
      "/criteria/codesets.json",
      "/criteria/doseEraConceptSet_PREP.json"
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    // capture results in an ArrayList
    ArrayList<ITable> actualTables = new ArrayList<>();

    // Test 1: simple query with no special conditions
    DoseEra criteria = new DoseEra();
    criteria.codesetId = 1;
    criteria.genderCS = createConceptSetSelection(1,false);
    criteria.unitCS = createConceptSetSelection(3,false);
    

    DoseEraSqlBuilder<DoseEra> builder = new DoseEraSqlBuilder<>();
    String query = renderQuery(builder.getCriteriaSql(criteria));

    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("doseEra.conceptset", query), new String[]{"person_id", "start_date"}));

    // Validate results:
    final IDataSet actualDataSet = new CompositeDataSet(actualTables.toArray(new ITable[]{})); // put actual results into a CompositeDataSet

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[]{"/criteria/doseEraConceptSet_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedDataSet, actualDataSet);
  }  

  @Test
  public void testDrugEraDateOffset() throws Exception {
    final String[] testDataSetsPrep = new String[]{
      "/datasets/vocabulary.json",
      "/criteria/codesets.json",
      "/criteria/drugEraDateOffset_PREP.json"
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    // capture results in an ArrayList
    ArrayList<ITable> actualTables = new ArrayList<>();

    // Test 1: simple query with no special conditions
    DrugEra criteria = new DrugEra();
    criteria.codesetId = 1;

    DrugEraSqlBuilder<DrugEra> builder = new DrugEraSqlBuilder<>();
    String query = renderQuery(builder.getCriteriaSql(criteria));

    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("drugEra.simple", query), new String[]{"person_id", "start_date"}));

    // Test 2: adjust start and end dates
    criteria.dateAdjustment = createDateAdjustment(DateAdjustment.DateType.START_DATE, 15, DateAdjustment.DateType.END_DATE, -10);

    query = renderQuery(builder.getCriteriaSql(criteria));
    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("drugEra.date_adjust", query), new String[]{"person_id", "start_date"}));

    // Validate results:
    final IDataSet actualDataSet = new CompositeDataSet(actualTables.toArray(new ITable[]{})); // put actual results into a CompositeDataSet

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[]{"/criteria/drugEraDateOffset_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedDataSet, actualDataSet);
  }

  @Test
  public void testDrugEraConceptSet() throws Exception {
    final String[] testDataSetsPrep = new String[]{
      "/datasets/vocabulary.json",
      "/criteria/codesets.json",
      "/criteria/drugEraConceptSet_PREP.json"
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    // capture results in an ArrayList
    ArrayList<ITable> actualTables = new ArrayList<>();

    // Test 1: simple query with no special conditions
    DrugEra criteria = new DrugEra();
    criteria.codesetId = 1;
    criteria.genderCS = createConceptSetSelection(1,false);

    DrugEraSqlBuilder<DrugEra> builder = new DrugEraSqlBuilder<>();
    String query = renderQuery(builder.getCriteriaSql(criteria));

    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("drugEra.conceptset", query), new String[]{"person_id", "start_date"}));

    // Validate results:
    final IDataSet actualDataSet = new CompositeDataSet(actualTables.toArray(new ITable[]{})); // put actual results into a CompositeDataSet

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[]{"/criteria/drugEraConceptSet_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedDataSet, actualDataSet);
  }

  @Test
  public void testDrugExposureDateOffset() throws Exception {
    final String[] testDataSetsPrep = new String[]{
      "/datasets/vocabulary.json",
      "/criteria/codesets.json",
      "/criteria/drugExposureDateOffset_PREP.json"
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    // capture results in an ArrayList
    ArrayList<ITable> actualTables = new ArrayList<>();

    // Test 1: simple query with no special conditions
    DrugExposure criteria = new DrugExposure();
    criteria.codesetId = 1;

    DrugExposureSqlBuilder<DrugExposure> builder = new DrugExposureSqlBuilder<>();
    String query = renderQuery(builder.getCriteriaSql(criteria));

    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("drugExposure.simple", query), new String[]{"person_id", "start_date"}));

    // Test 2: adjust start and end dates
    criteria.dateAdjustment = createDateAdjustment(DateAdjustment.DateType.START_DATE, 15, DateAdjustment.DateType.END_DATE, -10);

    query = renderQuery(builder.getCriteriaSql(criteria));
    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("drugExposure.date_adjust", query), new String[]{"person_id", "start_date"}));

    // Validate results:
    final IDataSet actualDataSet = new CompositeDataSet(actualTables.toArray(new ITable[]{})); // put actual results into a CompositeDataSet

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[]{"/criteria/drugExposureDateOffset_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedDataSet, actualDataSet);
  }  

  @Test
  public void testDrugExposureConceptSet() throws Exception {
    final String[] testDataSetsPrep = new String[]{
      "/datasets/vocabulary.json",
      "/criteria/codesets.json",
      "/criteria/drugExposureConceptSet_PREP.json"
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    // capture results in an ArrayList
    ArrayList<ITable> actualTables = new ArrayList<>();

    // Test 1: simple query with no special conditions
    DrugExposure criteria = new DrugExposure();
    criteria.codesetId = 1;
    criteria.drugTypeCS = createConceptSetSelection(2,false);
    criteria.genderCS = createConceptSetSelection(1,false);
    criteria.routeConceptCS = createConceptSetSelection(3,false);
    criteria.doseUnitCS = createConceptSetSelection(2,false);
    criteria.providerSpecialtyCS = createConceptSetSelection(3,false);
    criteria.visitTypeCS = createConceptSetSelection(2,false);

    DrugExposureSqlBuilder<DrugExposure> builder = new DrugExposureSqlBuilder<>();
    String query = renderQuery(builder.getCriteriaSql(criteria));

    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("drugExposure.conceptset", query), new String[]{"person_id", "start_date"}));

    // Validate results:
    final IDataSet actualDataSet = new CompositeDataSet(actualTables.toArray(new ITable[]{})); // put actual results into a CompositeDataSet

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[]{"/criteria/drugExposureConceptSet_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedDataSet, actualDataSet);
  }  

  @Test
  public void testMeasurementDateOffset() throws Exception {
    final String[] testDataSetsPrep = new String[]{
      "/datasets/vocabulary.json",
      "/criteria/codesets.json",
      "/criteria/measurementDateOffset_PREP.json"
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    // capture results in an ArrayList
    ArrayList<ITable> actualTables = new ArrayList<>();

    // Test 1: simple query with no special conditions
    Measurement criteria = new Measurement();
    criteria.codesetId = 1;

    MeasurementSqlBuilder<Measurement> builder = new MeasurementSqlBuilder<>();
    String query = renderQuery(builder.getCriteriaSql(criteria));

    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("measurement.simple", query), new String[]{"person_id", "start_date"}));

    // Test 2: adjust start and end dates
    criteria.dateAdjustment = createDateAdjustment(DateAdjustment.DateType.START_DATE, 15, DateAdjustment.DateType.END_DATE, -10);

    query = renderQuery(builder.getCriteriaSql(criteria));
    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("measurement.date_adjust", query), new String[]{"person_id", "start_date"}));

    // Validate results:
    final IDataSet actualDataSet = new CompositeDataSet(actualTables.toArray(new ITable[]{})); // put actual results into a CompositeDataSet

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[]{"/criteria/measurementDateOffset_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedDataSet, actualDataSet);
  }

  @Test
  public void testMeasurementConceptSet() throws Exception {
    final String[] testDataSetsPrep = new String[]{
      "/datasets/vocabulary.json",
      "/criteria/codesets.json",
      "/criteria/measurementConceptSet_PREP.json"
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    // capture results in an ArrayList
    ArrayList<ITable> actualTables = new ArrayList<>();

    // Test 1: simple query with no special conditions
    Measurement criteria = new Measurement();
    criteria.codesetId = 1;
    criteria.genderCS = createConceptSetSelection(1,false);
    criteria.operatorCS = createConceptSetSelection(3,false);
    criteria.valueAsConceptCS = createConceptSetSelection(2,false);
    criteria.unitCS = createConceptSetSelection(1,false);
    criteria.genderCS = createConceptSetSelection(1,false);
    criteria.providerSpecialtyCS = createConceptSetSelection(3,false);
    criteria.visitTypeCS = createConceptSetSelection(2,false);
    
    MeasurementSqlBuilder<Measurement> builder = new MeasurementSqlBuilder<>();
    String query = renderQuery(builder.getCriteriaSql(criteria));

    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("measurement.conceptset", query), new String[]{"person_id", "start_date"}));

    // Validate results:
    final IDataSet actualDataSet = new CompositeDataSet(actualTables.toArray(new ITable[]{})); // put actual results into a CompositeDataSet

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[]{"/criteria/measurementConceptSet_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedDataSet, actualDataSet);
  }

  @Test
  public void testObservationDateOffset() throws Exception {
    final String[] testDataSetsPrep = new String[]{
      "/datasets/vocabulary.json",
      "/criteria/codesets.json",
      "/criteria/observationDateOffset_PREP.json"
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    // capture results in an ArrayList
    ArrayList<ITable> actualTables = new ArrayList<>();

    // Test 1: simple query with no special conditions
    Observation criteria = new Observation();
    criteria.codesetId = 1;

    ObservationSqlBuilder<Observation> builder = new ObservationSqlBuilder<>();
    String query = renderQuery(builder.getCriteriaSql(criteria));

    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("observation.simple", query), new String[]{"person_id", "start_date"}));

    // Test 2: adjust start and end dates
    criteria.dateAdjustment = createDateAdjustment(DateAdjustment.DateType.START_DATE, 15, DateAdjustment.DateType.END_DATE, -10);

    query = renderQuery(builder.getCriteriaSql(criteria));
    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("observation.date_adjust", query), new String[]{"person_id", "start_date"}));

    
    // Validate results:
    final IDataSet actualDataSet = new CompositeDataSet(actualTables.toArray(new ITable[]{})); // put actual results into a CompositeDataSet

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[]{"/criteria/observationDateOffset_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedDataSet, actualDataSet);
  }

  @Test
  public void testObservationConceptSet() throws Exception {
    final String[] testDataSetsPrep = new String[]{
      "/datasets/vocabulary.json",
      "/criteria/codesets.json",
      "/criteria/observationConceptSet_PREP.json"
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    // capture results in an ArrayList
    ArrayList<ITable> actualTables = new ArrayList<>();

    // Test 1: simple query with no special conditions
    Observation criteria = new Observation();
    criteria.codesetId = 1;
    criteria.observationTypeCS = createConceptSetSelection(1,false);
    criteria.qualifierCS = createConceptSetSelection(3,false);
    criteria.valueAsConceptCS = createConceptSetSelection(2,false);
    criteria.unitCS = createConceptSetSelection(1,false);
    criteria.genderCS = createConceptSetSelection(1,false);
    criteria.providerSpecialtyCS = createConceptSetSelection(3,false);
    criteria.visitTypeCS = createConceptSetSelection(2,false);

    ObservationSqlBuilder<Observation> builder = new ObservationSqlBuilder<>();
    String query = renderQuery(builder.getCriteriaSql(criteria));

    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("observation.conceptset", query), new String[]{"person_id", "start_date"}));

    // Validate results:
    final IDataSet actualDataSet = new CompositeDataSet(actualTables.toArray(new ITable[]{})); // put actual results into a CompositeDataSet

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[]{"/criteria/observationConceptSet_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedDataSet, actualDataSet);
  }

  @Test
  public void testObservationPeriodDateOffset() throws Exception {
    final String[] testDataSetsPrep = new String[]{
      "/datasets/vocabulary.json",
      "/criteria/codesets.json",
      "/criteria/observationPeriodDateOffset_PREP.json"
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    // capture results in an ArrayList
    ArrayList<ITable> actualTables = new ArrayList<>();

    // Test 1: simple query with no special conditions
    ObservationPeriod criteria = new ObservationPeriod();

    ObservationPeriodSqlBuilder<ObservationPeriod> builder = new ObservationPeriodSqlBuilder<>();
    String query = renderQuery(builder.getCriteriaSql(criteria));

    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("observationPeriod.simple", query), new String[]{"person_id", "start_date"}));

    // Test 2: adjust start and end dates
    criteria.dateAdjustment = createDateAdjustment(DateAdjustment.DateType.START_DATE, 15, DateAdjustment.DateType.END_DATE, -10);

    query = renderQuery(builder.getCriteriaSql(criteria));
    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("observationPeriod.date_adjust", query), new String[]{"person_id", "start_date"}));

    // Test 3: user-defined start and end dates
    Period userPeriod = new Period();
    userPeriod.startDate = "2000-09-01";
    userPeriod.endDate = "2000-09-30";
    criteria.userDefinedPeriod = userPeriod;

    query = renderQuery(builder.getCriteriaSql(criteria));
    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("observationPeriod.user_defined", query), new String[]{"person_id", "start_date"}));

    // Validate results:
    final IDataSet actualDataSet = new CompositeDataSet(actualTables.toArray(new ITable[]{})); // put actual results into a CompositeDataSet

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[]{"/criteria/observationPeriodDateOffset_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedDataSet, actualDataSet);
  }

  @Test
  public void testObservationPeriodConceptSet() throws Exception {
    final String[] testDataSetsPrep = new String[]{
      "/datasets/vocabulary.json",
      "/criteria/codesets.json",
      "/criteria/observationPeriodConceptSet_PREP.json"
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    // capture results in an ArrayList
    ArrayList<ITable> actualTables = new ArrayList<>();

    // Test 1: simple query with no special conditions
    ObservationPeriod criteria = new ObservationPeriod();
    criteria.periodTypeCS = createConceptSetSelection(1,false);
    
    ObservationPeriodSqlBuilder<ObservationPeriod> builder = new ObservationPeriodSqlBuilder<>();
    String query = renderQuery(builder.getCriteriaSql(criteria));

    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("observationPeriod.conceptset", query), new String[]{"person_id", "start_date"}));

    // Validate results:
    final IDataSet actualDataSet = new CompositeDataSet(actualTables.toArray(new ITable[]{})); // put actual results into a CompositeDataSet

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[]{"/criteria/observationPeriodConceptSet_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedDataSet, actualDataSet);
  }

  @Test
  public void testPayerPlanPeriodDateOffset() throws Exception {
    final String[] testDataSetsPrep = new String[]{
      "/datasets/vocabulary.json",
      "/criteria/codesets.json",
      "/criteria/payerPlanPeriodDateOffset_PREP.json"
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    // capture results in an ArrayList
    ArrayList<ITable> actualTables = new ArrayList<>();

    // Test 1: simple query with no special conditions
    PayerPlanPeriod criteria = new PayerPlanPeriod();

    PayerPlanPeriodSqlBuilder<PayerPlanPeriod> builder = new PayerPlanPeriodSqlBuilder<>();
    String query = renderQuery(builder.getCriteriaSql(criteria));

    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("payerPlanPeriod.simple", query), new String[]{"person_id", "start_date"}));

    // Test 2: adjust start and end dates
    criteria.dateAdjustment = createDateAdjustment(DateAdjustment.DateType.START_DATE, 15, DateAdjustment.DateType.END_DATE, -10);

    query = renderQuery(builder.getCriteriaSql(criteria));
    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("payerPlanPeriod.date_adjust", query), new String[]{"person_id", "start_date"}));

    // Test 3: user-defined start and end dates
    Period userPeriod = new Period();
    userPeriod.startDate = "2000-09-01";
    userPeriod.endDate = "2000-09-30";
    criteria.userDefinedPeriod = userPeriod;

    query = renderQuery(builder.getCriteriaSql(criteria));
    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("payerPlanPeriod.user_defined", query), new String[]{"person_id", "start_date"}));

    // Validate results:
    final IDataSet actualDataSet = new CompositeDataSet(actualTables.toArray(new ITable[]{})); // put actual results into a CompositeDataSet

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[]{"/criteria/payerPlanPeriodDateOffset_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedDataSet, actualDataSet);
  }

  @Test
  public void testProcedureOccurrenceDateOffset() throws Exception {
    final String[] testDataSetsPrep = new String[]{
      "/datasets/vocabulary.json",
      "/criteria/codesets.json",
      "/criteria/procedureOccurrenceDateOffset_PREP.json"
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    // capture results in an ArrayList
    ArrayList<ITable> actualTables = new ArrayList<>();

    // Test 1: simple query with no special conditions
    ProcedureOccurrence criteria = new ProcedureOccurrence();
    criteria.codesetId = 1;

    ProcedureOccurrenceSqlBuilder<ProcedureOccurrence> builder = new ProcedureOccurrenceSqlBuilder<>();
    String query = renderQuery(builder.getCriteriaSql(criteria));

    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("procedureOccurrence.simple", query), new String[]{"person_id", "start_date"}));

    // Test 2: adjust start and end dates
    criteria.dateAdjustment = createDateAdjustment(DateAdjustment.DateType.START_DATE, 15, DateAdjustment.DateType.END_DATE, -10);

    query = renderQuery(builder.getCriteriaSql(criteria));
    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("procedureOccurrence.date_adjust", query), new String[]{"person_id", "start_date"}));

    
    // Validate results:
    final IDataSet actualDataSet = new CompositeDataSet(actualTables.toArray(new ITable[]{})); // put actual results into a CompositeDataSet

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[]{"/criteria/procedureOccurrenceDateOffset_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedDataSet, actualDataSet);
  }

  @Test
  public void testProcedureOccurrenceConceptSet() throws Exception {
    final String[] testDataSetsPrep = new String[]{
      "/datasets/vocabulary.json",
      "/criteria/codesets.json",
      "/criteria/procedureOccurrenceConceptSet_PREP.json"
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    // capture results in an ArrayList
    ArrayList<ITable> actualTables = new ArrayList<>();

    // Test 1: simple query with no special conditions
    ProcedureOccurrence criteria = new ProcedureOccurrence();
    criteria.codesetId = 1;
    criteria.genderCS = createConceptSetSelection(1,false);
    criteria.modifierCS = createConceptSetSelection(3,false);
    criteria.procedureTypeCS = createConceptSetSelection(2,false);
    criteria.visitTypeCS = createConceptSetSelection(2,false);
    criteria.providerSpecialtyCS = createConceptSetSelection(3,false);
    
    ProcedureOccurrenceSqlBuilder<ProcedureOccurrence> builder = new ProcedureOccurrenceSqlBuilder<>();
    String query = renderQuery(builder.getCriteriaSql(criteria));

    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("procedureOccurrence.conceptset", query), new String[]{"person_id", "start_date"}));

    // Validate results:
    final IDataSet actualDataSet = new CompositeDataSet(actualTables.toArray(new ITable[]{})); // put actual results into a CompositeDataSet

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[]{"/criteria/procedureOccurrenceConceptSet_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedDataSet, actualDataSet);
  }

  @Test
  public void testSpecimenConceptSet() throws Exception {
    final String[] testDataSetsPrep = new String[]{
      "/datasets/vocabulary.json",
      "/criteria/codesets.json",
      "/criteria/specimenConceptSet_PREP.json"
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    // capture results in an ArrayList
    ArrayList<ITable> actualTables = new ArrayList<>();

    // Test 1: simple query with no special conditions
    Specimen criteria = new Specimen();
    criteria.codesetId = 1;
    criteria.genderCS = createConceptSetSelection(1,false);
    criteria.specimenTypeCS = createConceptSetSelection(2,false);
    criteria.unitCS = createConceptSetSelection(3,false);
    criteria.anatomicSiteCS = createConceptSetSelection(2,false);
    criteria.diseaseStatusCS = createConceptSetSelection(3,false);
    
    SpecimenSqlBuilder<Specimen> builder = new SpecimenSqlBuilder<>();
    String query = renderQuery(builder.getCriteriaSql(criteria));

    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("specimen.conceptset", query), new String[]{"person_id", "start_date"}));

    // Validate results:
    final IDataSet actualDataSet = new CompositeDataSet(actualTables.toArray(new ITable[]{})); // put actual results into a CompositeDataSet

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[]{"/criteria/specimenConceptSet_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedDataSet, actualDataSet);
  }

  @Test
  public void testVisitOccurrenceDateOffset() throws Exception {
    final String[] testDataSetsPrep = new String[]{
      "/datasets/vocabulary.json",
      "/criteria/codesets.json",
      "/criteria/visitOccurrenceDateOffset_PREP.json"
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    // capture results in an ArrayList
    ArrayList<ITable> actualTables = new ArrayList<>();

    // Test 1: simple query with no special conditions
    VisitOccurrence criteria = new VisitOccurrence();
    criteria.codesetId = 1;

    VisitOccurrenceSqlBuilder<VisitOccurrence> builder = new VisitOccurrenceSqlBuilder<>();
    String query = renderQuery(builder.getCriteriaSql(criteria));

    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("visitOccurrence.simple", query), new String[]{"person_id", "start_date"}));

    // Test 2: adjust start and end dates
    criteria.dateAdjustment = createDateAdjustment(DateAdjustment.DateType.START_DATE, 15, DateAdjustment.DateType.END_DATE, -10);

    query = renderQuery(builder.getCriteriaSql(criteria));
    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("visitOccurrence.date_adjust", query), new String[]{"person_id", "start_date"}));

    // Validate results:
    final IDataSet actualDataSet = new CompositeDataSet(actualTables.toArray(new ITable[]{})); // put actual results into a CompositeDataSet

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[]{"/criteria/visitOccurrenceDateOffset_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedDataSet, actualDataSet);
  }

  @Test
  public void testVisitOccurrenceConceptSet() throws Exception {
    final String[] testDataSetsPrep = new String[]{
      "/datasets/vocabulary.json",
      "/criteria/codesets.json",
      "/criteria/visitOccurrenceConceptSet_PREP.json"
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    // capture results in an ArrayList
    ArrayList<ITable> actualTables = new ArrayList<>();

    // Test 1: simple query with no special conditions
    VisitOccurrence criteria = new VisitOccurrence();
    criteria.codesetId = 1;
    criteria.genderCS = createConceptSetSelection(1,false);
    criteria.visitTypeCS = createConceptSetSelection(2,false);
    criteria.providerSpecialtyCS = createConceptSetSelection(3,false);
    criteria.placeOfServiceCS = createConceptSetSelection(2,false);

    VisitOccurrenceSqlBuilder<VisitOccurrence> builder = new VisitOccurrenceSqlBuilder<>();
    String query = renderQuery(builder.getCriteriaSql(criteria));

    // Store actual records from criteria query into actualTables
    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("visitOccurrence.conceptset", query), new String[]{"person_id", "start_date"}));

    // Validate results:
    final IDataSet actualDataSet = new CompositeDataSet(actualTables.toArray(new ITable[]{})); // put actual results into a CompositeDataSet

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[]{"/criteria/visitOccurrenceConceptSet_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedDataSet, actualDataSet);
  }
}
