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
import org.ohdsi.circe.cohortdefinition.DateRange;
import org.ohdsi.circe.cohortdefinition.Episode;
import org.ohdsi.circe.cohortdefinition.NumericRange;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.springframework.jdbc.core.JdbcTemplate;

public class CriteriaQuery_5_4_0_Test extends AbstractDatabaseTest {

  private static final String CDM_DDL_PATH = "/ddl/cdm_v5.4.sql";
  private static final String TEMP_DDL_PATH = "/criteria/temp.sql";

  private String renderQuery(String query) {
    String result = StringUtils.replace(query, "#Codesets", "temp.codesets");
    return SqlRender.renderSql(SqlTranslate.translateSql(result, "postgresql"),
            new String[]{"cdm_database_schema", "vocabulary_database_schema"},
            new String[]{"cdm", "cdm"});
  }

  @BeforeClass
  public static void beforeClass() {
    jdbcTemplate = new JdbcTemplate(getDataSource());
    prepareSchema("cdm", CDM_DDL_PATH);
    prepareSchema("temp", TEMP_DDL_PATH);
  }

  @Test
  public void cdmSchemaWasCreated() {
    jdbcTemplate.queryForList("select count(*) as c from cdm.person");
  }

  private ConceptSetSelection createConceptSetSelection(Integer id) {
    ConceptSetSelection selection = new ConceptSetSelection();
    selection.codesetId = id;
    return selection;
  }

  private NumericRange createNumericRange(String op, Number value, Number extent) {
    NumericRange range = new NumericRange();
    range.op = op;
    range.value = value;
    range.extent = extent;
    return range;
  }

  private DateRange createDateRange(String op, String value) {
    DateRange range = new DateRange();
    range.op = op;
    range.value = value;
    return range;
  }

  @Test
  public void testEpisodeCriteria() throws Exception {
    final String[] testDataSetsPrep = new String[]{
      "/datasets/vocabulary.json",
      "/criteria/codesets.json",
      "/criteria/episode_PREP.json"
    };
    final IDatabaseConnection dbUnitCon = getConnection();

    final IDataSet dsPrep = DataSetFactory.createDataSet(testDataSetsPrep);
    DatabaseOperation.CLEAN_INSERT.execute(dbUnitCon, dsPrep);

    ArrayList<ITable> actualTables = new ArrayList<>();

    Episode criteria = new Episode();
    criteria.codesetId = 1;
    criteria.first = true;
    criteria.episodeStartDate = createDateRange("gt", "2018-01-01");
    criteria.episodeEndDate = createDateRange("lt", "2018-12-31");
    criteria.episodeNumber = createNumericRange("eq", 1, null);
    criteria.age = createNumericRange("gte", 18, null);
    criteria.genderCS = createConceptSetSelection(4);
    criteria.episodeObjectConceptCS = createConceptSetSelection(2);
    criteria.episodeTypeCS = createConceptSetSelection(3);

    EpisodeSqlBuilder<Episode> builder = new EpisodeSqlBuilder<>();
    String query = renderQuery(builder.getCriteriaSql(criteria));

    actualTables.add(new SortedTable(dbUnitCon.createQueryTable("episode.simple", query), new String[]{"person_id", "start_date"}));

    final IDataSet actualDataSet = new CompositeDataSet(actualTables.toArray(new ITable[]{}));
    final String[] testDataSetsVerify = new String[]{"/criteria/episode_VERIFY.json"};
    final IDataSet expectedDataSet = DataSetFactory.createDataSet(testDataSetsVerify);

    Assertion.assertEquals(expectedDataSet, actualDataSet);
  }
}