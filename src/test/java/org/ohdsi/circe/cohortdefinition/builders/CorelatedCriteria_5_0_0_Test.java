/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.circe.cohortdefinition.builders;

import com.github.mjeanroy.dbunit.core.dataset.DataSetFactory;
import java.sql.SQLException;
import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.operation.DatabaseOperation;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ohdsi.circe.AbstractDatabaseTest;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.circe.cohortdefinition.ConditionOccurrence;
import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;
import org.ohdsi.circe.cohortdefinition.CriteriaGroup;
import org.ohdsi.circe.cohortdefinition.Occurrence;
import org.ohdsi.circe.cohortdefinition.Window;
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
public class CorelatedCriteria_5_0_0_Test extends AbstractDatabaseTest {
  private final static Logger log = LoggerFactory.getLogger(CorelatedCriteria_5_0_0_Test.class);  
  private static final String CDM_DDL_PATH = "/ddl/cdm_v5.0.sql";
  private static final String RESULTS_DDL_PATH = "/ddl/resultsSchema.sql";
  
  private static final String EVENT_TABLE_TEMPLATE = "(SELECT ROW_NUMBER() OVER (partition by E.subject_id order by E.cohort_start_date) AS event_id, " +
            "E.subject_id AS person_id, E.cohort_start_date AS start_date, E.cohort_end_date AS end_date, " +
            "OP.observation_period_start_date AS op_start_date, OP.observation_period_end_date AS op_end_date\n" +
            "FROM %s E\n" +
            "JOIN %s.observation_period OP ON E.subject_id = OP.person_id\n" +
            "  AND E.cohort_start_date >= OP.observation_period_start_date AND E.cohort_start_date <= OP.observation_period_end_date\n" +
            "WHERE E.cohort_definition_id = %d)";
  
  @BeforeClass
  public static void beforeClass() {
    jdbcTemplate = new JdbcTemplate(getDataSource());
    prepareSchema("cdm", CDM_DDL_PATH);
  }

  
  private Window getPrior365Window() {
    Window prior365Window = new Window();  
    // index starts between 365d before
    Window.Endpoint startPoint = new Window.Endpoint();
    startPoint.coeff = -1;
    startPoint.days = 365;
    prior365Window.start = startPoint;
    // ... and 0 days before
    Window.Endpoint endPoint = new Window.Endpoint();
    endPoint.coeff = -1;
    endPoint.days = 0;
    prior365Window.end = endPoint;
    return prior365Window;
  }
  
  private Occurrence getAtLeast1Occurrence() {
    Occurrence atLeast1 = new Occurrence();
    atLeast1.type = Occurrence.AT_LEAST;
    atLeast1.count = 1;
    
    return atLeast1;
  }

  private Occurrence getAtExactly0Occurrence() {
    Occurrence exactly0 = new Occurrence();
    exactly0.type = Occurrence.EXACTLY;
    exactly0.count = 0;
    
    return exactly0;
  }

  @Test
  public void simpleWindowCriteriaTest() throws Exception {
    final CohortExpressionQueryBuilder queryBuilder = new CohortExpressionQueryBuilder();
    final String RESULTS_SCHEMA = "simple_window_test";
    final String[] testDataSetsPrep = new String[] { "/datasets/vocabulary.json",
      "/criteria/simpleWindowCriteria_PREP.json"};

    final String[] testDataSetsVerify = new String[] {"/corelatedcriteria/simpleWindowCriteria_VERIFY.json"};

    // prepare results schema for the specified options.resultSchema
    prepareSchema(RESULTS_SCHEMA, RESULTS_DDL_PATH);

    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"
    
    /// build test query for WindowCriteria
    WindowedCriteria wc = new WindowedCriteria();
    wc.criteria = new ConditionOccurrence(); // find any condition occurence
    
    wc.startWindow = getPrior365Window();
    
    // translate to PG
    String eventTable = String.format(EVENT_TABLE_TEMPLATE, RESULTS_SCHEMA + ".cohort", "cdm", 1);
    String query = queryBuilder.getWindowedCriteriaQuery(wc, eventTable);
    String translatedSql = SqlRender.renderSql(SqlTranslate.translateSql(query, "postgresql"),
            new String[] {"cdm_database_schema"}, 
            new String[] {"cdm"});
    
    // Validate results
    // Load actual records from cohort table
    final ITable actualTable = dbUnitCon.createQueryTable(RESULTS_SCHEMA + ".output", translatedSql);
    // Load expected data from an XML dataset
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);
    final ITable expectedTable = expectedDataSet.getTable(RESULTS_SCHEMA + ".output");

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedTable, actualTable);

  }

  @Test
  public void simpleInclusionTest() throws Exception {
    final CohortExpressionQueryBuilder queryBuilder = new CohortExpressionQueryBuilder();
    final String RESULTS_SCHEMA = "simple_inclusion_test";
    final String[] testDataSetsPrep = new String[] { "/datasets/vocabulary.json",
      "/corelatedcriteria/simpleInclusion_PREP.json"};

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[] {"/corelatedcriteria/simpleInclusion_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // prepare results schema for the specified options.resultSchema
    prepareSchema(RESULTS_SCHEMA, RESULTS_DDL_PATH);

    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"
    
    /// build inclusion  query for Group Criteria
    CriteriaGroup cg = new CriteriaGroup();
    cg.type= "ALL";
    CorelatedCriteria cc = new CorelatedCriteria();
    cc.criteria = new ConditionOccurrence(); // find any condition occurence
    cc.startWindow = getPrior365Window();
    cc.occurrence = getAtLeast1Occurrence();
    cg.criteriaList = new CorelatedCriteria[] { cc };
    
    // translate to PG
    String eventTable = String.format(EVENT_TABLE_TEMPLATE, RESULTS_SCHEMA + ".cohort", "cdm", 1);
    String inclusionQuery = queryBuilder.getCriteriaGroupQuery(cg, eventTable);
    String translatedInclusionQuery = SqlRender.renderSql(SqlTranslate.translateSql(inclusionQuery, "postgresql"),
            new String[] {"cdm_database_schema", "indexId"}, 
            new String[] {"cdm", "0"});
    
    // Validate results
    // perform inclusion query
    final ITable actualInclusion = dbUnitCon.createQueryTable(RESULTS_SCHEMA + ".inclusion", translatedInclusionQuery);
    final ITable expectedInclusion = expectedDataSet.getTable(RESULTS_SCHEMA + ".inclusion");

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedInclusion, actualInclusion);
    
    // perform exclusion query by changing the occurrenc etype in the CorelatedCriteria
    cc.occurrence = getAtExactly0Occurrence();
    String exclusionQuery = queryBuilder.getCriteriaGroupQuery(cg, eventTable);
    String translatedExclusionQuery = SqlRender.renderSql(SqlTranslate.translateSql(exclusionQuery, "postgresql"),
            new String[] {"cdm_database_schema", "indexId"}, 
            new String[] {"cdm", "0"});
    
    final ITable actualExclusion = dbUnitCon.createQueryTable(RESULTS_SCHEMA + ".inclusion", translatedInclusionQuery);
    final ITable expectedExclusion = expectedDataSet.getTable(RESULTS_SCHEMA + ".inclusion");

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedExclusion, actualExclusion);
    

  }

}
