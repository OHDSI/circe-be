/*
 * Copyright 2024 cknoll1.
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
package org.ohdsi.circe.conceptset;

import com.github.mjeanroy.dbunit.core.dataset.DataSetFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.dbunit.Assertion;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.operation.DatabaseOperation;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ohdsi.circe.AbstractDatabaseTest;
import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.circe.vocabulary.ConceptSetExpression.ConceptSetItem;
import org.ohdsi.circe.vocabulary.ConceptSetExpressionQueryBuilder;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author cknoll1
 */
public class ConceptSetDbTest extends AbstractDatabaseTest {
  private final static Logger log = LoggerFactory.getLogger(ConceptSetDbTest.class);
  private static final String CDM_DDL_PATH = "/ddl/cdm_v5.0.sql";
  
  @BeforeClass
  public static void beforeClass() {
    jdbcTemplate = new JdbcTemplate(getDataSource());
    prepareSchema("cdm", CDM_DDL_PATH);
  }
  @Test
  public void inClauseConceptSetQueryTest() throws Exception {  
    
    ConceptSetExpressionQueryBuilder queryBuilder = new ConceptSetExpressionQueryBuilder();
    final String[] testDataSetsPrep = new String[] { "/datasets/vocabulary.json" };

    // build a concept set expression with > 2000 concepts
    ConceptSetExpression cse = new ConceptSetExpression();
    List<ConceptSetItem> items = IntStream.range(1,4000).mapToObj((i) -> {
      Concept c = new Concept();
      c.conceptId = i*1L;
      c.conceptName = String.format("Concept %d", i);
      ConceptSetItem ci = new ConceptSetItem();
      ci.concept = c;
      ci.includeDescendants = true;
      ci.isExcluded = (i%2 == 0) ? true : false;
      
      return ci;
    }).collect(Collectors.toList());
    
    cse.items = items.toArray(new ConceptSetItem[0]);

    final IDatabaseConnection dbUnitCon = getConnection();

    // load test data into DB.
    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep); // clean load of the DB. Careful, clean means "delete the old stuff"
    
    String conceptsetQuery = queryBuilder.buildExpressionQuery(cse);
    String translatedConceptsetQuery = SqlRender.renderSql(SqlTranslate.translateSql(conceptsetQuery, "postgresql"),
            new String[] {"vocabulary_database_schema"}, 
            new String[] {"cdm"});
    
    final ITable actualResult = dbUnitCon.createQueryTable("inclause.result", translatedConceptsetQuery);

    // Load expected data from an XML dataset
    final String[] testDataSetsVerify = new String[] {"/conceptset/inClause_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);
    final ITable expectedResult = expectedDataSet.getTable("inclause.result");

    // Assert actual database table match expected table
    Assertion.assertEquals(expectedResult, actualResult);
    
    
    
  }
}
