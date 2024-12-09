/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.circe.cohortdefinition.builders;

import com.github.mjeanroy.dbunit.core.dataset.DataSetFactory;
import org.dbunit.Assertion;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.operation.DatabaseOperation;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ohdsi.circe.AbstractDatabaseTest;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.circe.cohortdefinition.ConditionOccurrence;
import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;
import org.ohdsi.circe.cohortdefinition.CriteriaGroup;
import org.ohdsi.circe.cohortdefinition.Occurrence;
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

  @BeforeClass
  public static void beforeClass() {
    jdbcTemplate = new JdbcTemplate(getDataSource());
    prepareSchema("cdm", CDM_DDL_PATH);
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
    cc.startWindow = CriteriaUtils.getPrior365Window();
    cc.occurrence = CriteriaUtils.getAtLeast1Occurrence();
    cg.criteriaList = new CorelatedCriteria[] { cc };
    
    // translate to PG
    String eventTable = String.format(CriteriaUtils.EVENT_TABLE_TEMPLATE, RESULTS_SCHEMA + ".cohort", "cdm", 1);
    String inclusionQuery = queryBuilder.getCriteriaGroupQuery(cg, eventTable, null);
    String translatedInclusionQuery = SqlRender.renderSql(SqlTranslate.translateSql(inclusionQuery, "postgresql"),
            new String[] {"cdm_database_schema", "indexId"}, 
            new String[] {"cdm", "0"});
    
    // Validate results
    // perform inclusion query
    final ITable actualInclusion = dbUnitCon.createQueryTable(RESULTS_SCHEMA + ".inclusion", translatedInclusionQuery);
    final ITable expectedInclusion = expectedDataSet.getTable(RESULTS_SCHEMA + ".inclusion");

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedInclusion, actualInclusion);
    
    // perform exclusion query by changing the occurrence type in the CorelatedCriteria
    // Note this is still  an 'inclusion', just only include those with exactly 0 counts
    cc.occurrence = CriteriaUtils.getAtExactly0Occurrence();
    String exclusionQuery = queryBuilder.getCriteriaGroupQuery(cg, eventTable, null);
    String translatedExclusionQuery = SqlRender.renderSql(SqlTranslate.translateSql(exclusionQuery, "postgresql"),
            new String[] {"cdm_database_schema", "indexId"}, 
            new String[] {"cdm", "0"});
    
    final ITable actualExclusion = dbUnitCon.createQueryTable(RESULTS_SCHEMA + ".exclusion", translatedExclusionQuery);
    final ITable expectedExclusion = expectedDataSet.getTable(RESULTS_SCHEMA + ".exclusion");

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedExclusion, actualExclusion);
    

  }

  @Test
  public void distinctStartTest() throws Exception {
    final CohortExpressionQueryBuilder queryBuilder = new CohortExpressionQueryBuilder();
    final String RESULTS_SCHEMA = "distinct_start";
    final String[] testDataSetsPrep = new String[] { "/datasets/vocabulary.json",
      "/corelatedcriteria/distinctStart_PREP.json"};

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[] {"/corelatedcriteria/distinctStart_VERIFY.json"};
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
    cc.startWindow = CriteriaUtils.getPrior365Window();
    cc.occurrence = CriteriaUtils.getDistinctCount(CriteriaColumn.START_DATE, Occurrence.AT_LEAST, 2);
    cg.criteriaList = new CorelatedCriteria[] { cc };
    
    // translate to PG
    String eventTable = String.format(CriteriaUtils.EVENT_TABLE_TEMPLATE, RESULTS_SCHEMA + ".cohort", "cdm", 1);
    String countQuery = queryBuilder.getCriteriaGroupQuery(cg, eventTable, null);
    String translatedCountQuery = SqlRender.renderSql(SqlTranslate.translateSql(countQuery, "postgresql"),
            new String[] {"cdm_database_schema", "indexId"}, 
            new String[] {"cdm", "0"});
    
    // Validate results
    // perform inclusion query
    final ITable actualInclusion = dbUnitCon.createQueryTable(RESULTS_SCHEMA + ".output", translatedCountQuery);
    final ITable expectedInclusion = expectedDataSet.getTable(RESULTS_SCHEMA + ".output");

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedInclusion, actualInclusion);

  }

  @Test
  public void distinctVisitTest() throws Exception {
    final CohortExpressionQueryBuilder queryBuilder = new CohortExpressionQueryBuilder();
    final String RESULTS_SCHEMA = "distinct_visit";
    final String[] testDataSetsPrep = new String[] { "/datasets/vocabulary.json",
      "/corelatedcriteria/distinctVisit_PREP.json"};

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[] {"/corelatedcriteria/distinctVisit_VERIFY.json"};
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
    cc.startWindow = CriteriaUtils.getPrior365Window();
    cc.occurrence = CriteriaUtils.getDistinctCount(CriteriaColumn.VISIT_ID, Occurrence.AT_LEAST, 2);
    cg.criteriaList = new CorelatedCriteria[] { cc };
    
    // translate to PG
    String eventTable = String.format(CriteriaUtils.EVENT_TABLE_TEMPLATE, RESULTS_SCHEMA + ".cohort", "cdm", 1);
    String countQuery = queryBuilder.getCriteriaGroupQuery(cg, eventTable, null);
    String translatedCountQuery = SqlRender.renderSql(SqlTranslate.translateSql(countQuery, "postgresql"),
            new String[] {"cdm_database_schema", "indexId"}, 
            new String[] {"cdm", "0"});
    
    // Validate results
    // perform inclusion query
    final ITable actualInclusion = dbUnitCon.createQueryTable(RESULTS_SCHEMA + ".output", translatedCountQuery);
    final ITable expectedInclusion = expectedDataSet.getTable(RESULTS_SCHEMA + ".output");

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedInclusion, actualInclusion);

  }

  @Test
  public void distinctDefaultTest() throws Exception {
    final CohortExpressionQueryBuilder queryBuilder = new CohortExpressionQueryBuilder();
    final String RESULTS_SCHEMA = "distinct_default";
    final String[] testDataSetsPrep = new String[] { "/datasets/vocabulary.json",
      "/corelatedcriteria/distinctDefault_PREP.json"};

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[] {"/corelatedcriteria/distinctDefault_VERIFY.json"};
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
    cc.startWindow = CriteriaUtils.getPrior365Window();
    cc.occurrence = CriteriaUtils.getDistinctCount(null, Occurrence.AT_LEAST, 2);
    cg.criteriaList = new CorelatedCriteria[] { cc };
    
    // translate to PG
    String eventTable = String.format(CriteriaUtils.EVENT_TABLE_TEMPLATE, RESULTS_SCHEMA + ".cohort", "cdm", 1);
    String countQuery = queryBuilder.getCriteriaGroupQuery(cg, eventTable, null);
    String translatedCountQuery = SqlRender.renderSql(SqlTranslate.translateSql(countQuery, "postgresql"),
            new String[] {"cdm_database_schema", "indexId"}, 
            new String[] {"cdm", "0"});
    
    // Validate results
    // perform inclusion query
    final ITable actualInclusion = dbUnitCon.createQueryTable(RESULTS_SCHEMA + ".output", translatedCountQuery);
    final ITable expectedInclusion = expectedDataSet.getTable(RESULTS_SCHEMA + ".output");

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedInclusion, actualInclusion);

  }

}
