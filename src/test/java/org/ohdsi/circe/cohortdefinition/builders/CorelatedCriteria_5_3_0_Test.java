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
import org.ohdsi.circe.cohortdefinition.ConceptSetSelection;
import org.ohdsi.circe.cohortdefinition.ConditionOccurrence;
import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;
import org.ohdsi.circe.cohortdefinition.CriteriaGroup;
import org.ohdsi.circe.cohortdefinition.Occurrence;
import org.ohdsi.circe.cohortdefinition.VisitDetail;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author cknoll1
 */
public class CorelatedCriteria_5_3_0_Test extends AbstractDatabaseTest {
  private final static Logger log = LoggerFactory.getLogger(CorelatedCriteria_5_3_0_Test.class);  
  private static final String CDM_DDL_PATH = "/ddl/cdm_v5.3.sql";
  private static final String RESULTS_DDL_PATH = "/ddl/resultsSchema.sql";

  @BeforeClass
  public static void beforeClass() {
    jdbcTemplate = new JdbcTemplate(getDataSource());
    prepareSchema("cdm", CDM_DDL_PATH);
  }
  @Test
  public void visitDetailCodesetCriteriaTest() throws Exception {
    final CohortExpressionQueryBuilder queryBuilder = new CohortExpressionQueryBuilder();
    final String RESULTS_SCHEMA = "visit_detail_codeset_criteria";
    final String[] testDataSetsPrep = new String[] { "/datasets/vocabulary.json",
      "/corelatedcriteria/visitDetailCodesetCriteria_PREP.json"};

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[] {"/corelatedcriteria/visitDetailCodesetCriteria_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    // prepare results schema for the specified options.resultSchema
    prepareSchema(RESULTS_SCHEMA, RESULTS_DDL_PATH);

    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"

    // event table query
    String eventTable = String.format(CriteriaUtils.EVENT_TABLE_TEMPLATE, RESULTS_SCHEMA + ".cohort", "cdm", 1);
    
    // Concept set selection criteria
    ConceptSetSelection inCsSelection = new ConceptSetSelection();
    inCsSelection.codesetId = 1;
    inCsSelection.isExclusion = false;
    
    ConceptSetSelection notInCsSelection = new ConceptSetSelection();
    notInCsSelection.codesetId = 1;
    notInCsSelection.isExclusion = true;
    
    // VisitDetail criteria
    VisitDetail visitDetail = new VisitDetail();
    // build inclusion  query for Group Criteria
    CriteriaGroup cg = new CriteriaGroup();
    cg.type= "ALL";
    CorelatedCriteria cc = new CorelatedCriteria();
    cc.criteria = visitDetail; // find any condition occurence
    cc.startWindow = CriteriaUtils.getPrior365Window();
    cc.occurrence = CriteriaUtils.getAtExactly1Occurrence();
    cg.criteriaList = new CorelatedCriteria[] { cc };

    // Query 1: exactly 1 occurrence where gener concept in codeset 1
    visitDetail.genderCS = inCsSelection;

    // translate to PG
    String inGenderQuery = queryBuilder.getCriteriaGroupQuery(cg, eventTable, false);
    inGenderQuery = inGenderQuery.replace("#Codesets", RESULTS_SCHEMA + ".codesets");
    String translatedInGenderQuery = SqlRender.renderSql(SqlTranslate.translateSql(inGenderQuery, "postgresql"),
            new String[] {"cdm_database_schema", "indexId"}, 
            new String[] {"cdm", "0"});
    
    // Validate results
    // perform inclusion query
    final ITable actualInGender = dbUnitCon.createQueryTable(RESULTS_SCHEMA + ".gender_in_codeset", translatedInGenderQuery);
    final ITable expectedInGender = expectedDataSet.getTable(RESULTS_SCHEMA + ".gender_in_codeset");
    Assertion.assertEquals(expectedInGender, actualInGender);

    //Query 2: exaclty 1 occurrence where gender concept not in codeset 1
    visitDetail.genderCS = notInCsSelection;
    
    // translate to PG
    String notInGenderQuery = queryBuilder.getCriteriaGroupQuery(cg, eventTable, false);
    notInGenderQuery = notInGenderQuery.replace("#Codesets", RESULTS_SCHEMA + ".codesets");
    String translatedNotInGenderQuery = SqlRender.renderSql(SqlTranslate.translateSql(notInGenderQuery, "postgresql"),
            new String[] {"cdm_database_schema", "indexId"}, 
            new String[] {"cdm", "0"});
    
    // Validate results
    // perform inclusion query
    final ITable actualNotInGender = dbUnitCon.createQueryTable(RESULTS_SCHEMA + ".gender_not_in_codeset", translatedNotInGenderQuery);
    final ITable expectedNotInGender = expectedDataSet.getTable(RESULTS_SCHEMA + ".gender_not_in_codeset");
    Assertion.assertEquals(expectedNotInGender, actualNotInGender);

    // Query 3: exaclty 1 occurrence where provider concept in codeset 1
    visitDetail.genderCS = null;
    visitDetail.providerSpecialtyCS = inCsSelection;
    
    // translate to PG
    String inProviderQuery = queryBuilder.getCriteriaGroupQuery(cg, eventTable, false);
    inProviderQuery = inProviderQuery.replace("#Codesets", RESULTS_SCHEMA + ".codesets");
    String translatedInProviderQuery = SqlRender.renderSql(SqlTranslate.translateSql(inProviderQuery, "postgresql"),
            new String[] {"cdm_database_schema", "indexId"}, 
            new String[] {"cdm", "0"});
    
    // Validate results
    // perform inclusion query
    final ITable actualInProvider = dbUnitCon.createQueryTable(RESULTS_SCHEMA + ".provider_in_codeset", translatedInProviderQuery);
    final ITable expectedInProvider = expectedDataSet.getTable(RESULTS_SCHEMA + ".provider_in_codeset");
    Assertion.assertEquals(expectedInProvider, actualInProvider);
    
    // Query 4: exaclty 1 occurrence where provider concept not in codeset 1
    visitDetail.providerSpecialtyCS = notInCsSelection;
    
    // translate to PG
    String notInProviderQuery = queryBuilder.getCriteriaGroupQuery(cg, eventTable, false);
    notInProviderQuery = notInProviderQuery.replace("#Codesets", RESULTS_SCHEMA + ".codesets");
    String translatedNotInProviderQuery = SqlRender.renderSql(SqlTranslate.translateSql(notInProviderQuery, "postgresql"),
            new String[] {"cdm_database_schema", "indexId"}, 
            new String[] {"cdm", "0"});
    
    // Validate results
    // perform inclusion query
    final ITable actualNotInProvider = dbUnitCon.createQueryTable(RESULTS_SCHEMA + ".provider_not_in_codeset", translatedNotInProviderQuery);
    final ITable expectedNotInProvider = expectedDataSet.getTable(RESULTS_SCHEMA + ".provider_not_in_codeset");
    Assertion.assertEquals(expectedNotInProvider, actualNotInProvider);
    
    // Query 5: exaclty 1 occurrence where place of service concept in codeset 1
    visitDetail.providerSpecialtyCS = null;
    visitDetail.placeOfServiceCS = inCsSelection;
    
    // translate to PG
    String inPlaceOfServiceQuery = queryBuilder.getCriteriaGroupQuery(cg, eventTable, false);
    inPlaceOfServiceQuery = inPlaceOfServiceQuery.replace("#Codesets", RESULTS_SCHEMA + ".codesets");
    String translatedInPlaceOfServiceQuery = SqlRender.renderSql(SqlTranslate.translateSql(inPlaceOfServiceQuery, "postgresql"),
            new String[] {"cdm_database_schema", "indexId"}, 
            new String[] {"cdm", "0"});
    
    // Validate results
    // perform inclusion query
    final ITable actualInPlaceOfService = dbUnitCon.createQueryTable(RESULTS_SCHEMA + ".provider_in_codeset", translatedInPlaceOfServiceQuery);
    final ITable expectedInPlaceOfService = expectedDataSet.getTable(RESULTS_SCHEMA + ".provider_in_codeset");
    Assertion.assertEquals(expectedInPlaceOfService, actualInPlaceOfService);
    
    // Query 6: exaclty 1 occurrence where place of service not in codeset 1
    visitDetail.placeOfServiceCS = notInCsSelection;
    
    // translate to PG
    String notInPlaceOfServiceQuery = queryBuilder.getCriteriaGroupQuery(cg, eventTable, false);
    notInPlaceOfServiceQuery = notInPlaceOfServiceQuery.replace("#Codesets", RESULTS_SCHEMA + ".codesets");
    String translatedNotInPlaceOfServiceQuery = SqlRender.renderSql(SqlTranslate.translateSql(notInPlaceOfServiceQuery, "postgresql"),
            new String[] {"cdm_database_schema", "indexId"}, 
            new String[] {"cdm", "0"});
    
    // Validate results
    // perform inclusion query
    final ITable actualNotInPlaceOfService = dbUnitCon.createQueryTable(RESULTS_SCHEMA + ".provider_not_in_codeset", translatedNotInPlaceOfServiceQuery);
    final ITable expectedNotInPlaceOfService = expectedDataSet.getTable(RESULTS_SCHEMA + ".provider_not_in_codeset");
    Assertion.assertEquals(expectedNotInPlaceOfService, actualNotInPlaceOfService);
  }

}
