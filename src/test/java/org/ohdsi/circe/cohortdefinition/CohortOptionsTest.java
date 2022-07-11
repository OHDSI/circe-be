package org.ohdsi.circe.cohortdefinition;

import java.util.regex.Pattern;

import org.junit.Test;
import org.ohdsi.circe.BaseTest;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static java.lang.String.format;
import org.ohdsi.circe.helper.ResourceHelper;

public class CohortOptionsTest extends BaseTest {

  private Pattern buildPattern(String regex) {
    return Pattern.compile(regex, Pattern.DOTALL);
  }

  private void checkCohortFieldSql(String expressionSql, String cohortFieldName) {

    // check inserts for cohort table
    assertThat(expressionSql, containsString(format("DELETE FROM @target_database_schema.@target_cohort_table where %s = @target_cohort_id", cohortFieldName)));
    assertThat(expressionSql, matchesPattern(buildPattern(format(".*INSERT INTO @target_database_schema\\.@target_cohort_table \\(%s,.*select @target_cohort_id as %s,.*", cohortFieldName, cohortFieldName))));

    // check inserts for cohort_inclusion_result, mode = 0
    assertThat(expressionSql, containsString(format("delete from @results_database_schema.cohort_inclusion_result where %s = @target_cohort_id and mode_id = 0;", cohortFieldName)));
    assertThat(expressionSql, matchesPattern(buildPattern(format(".*insert into @results_database_schema\\.cohort_inclusion_result \\(%s,.*select @target_cohort_id as %s,.+0 as mode_id.+", cohortFieldName, cohortFieldName))));

    // check inserts for cohort_inclusion_stats, mode = 0
    assertThat(expressionSql, containsString(format("delete from @results_database_schema.cohort_inclusion_stats where %s = @target_cohort_id and mode_id = 0;", cohortFieldName)));
    assertThat(expressionSql, matchesPattern(buildPattern(format(".*insert into @results_database_schema\\.cohort_inclusion_stats \\(%s,.*"
            + "select .*0 as mode_id.+", cohortFieldName))));

    // check inserts for cohort_summary_stats, mode = 0
    assertThat(expressionSql, containsString(format("delete from @results_database_schema.cohort_summary_stats where %s = @target_cohort_id and mode_id = 0;", cohortFieldName)));
    assertThat(expressionSql, matchesPattern(buildPattern(format(".*insert.+\\.cohort_summary_stats \\(%s,.+"
            + "select @target_cohort_id as %s,.+"
            + "where sr\\.mode_id = 0.+sr\\.%s = @target_cohort_id.+", cohortFieldName, cohortFieldName, cohortFieldName))));

    // check inserts for cohort_inclusion_result, mode = 1
    assertThat(expressionSql, containsString(format("delete from @results_database_schema.cohort_inclusion_result where %s = @target_cohort_id and mode_id = 1;", cohortFieldName)));
    assertThat(expressionSql, matchesPattern(buildPattern(format(".*insert into @results_database_schema\\.cohort_inclusion_result \\(%s,.+select @target_cohort_id as %s,.+1 as mode_id.+", cohortFieldName, cohortFieldName))));

    // check inserts for cohort_inclusion_stats, mode = 1
    assertThat(expressionSql, containsString(format("delete from @results_database_schema.cohort_inclusion_stats where %s = @target_cohort_id and mode_id = 1;", cohortFieldName)));
    assertThat(expressionSql, matchesPattern(buildPattern(format(".*insert into @results_database_schema\\.cohort_inclusion_stats \\(%s,.+"
            + "select .*1 as mode_id.+", cohortFieldName))));

    // check inserts for cohort_summary_stats, mode = 1
    assertThat(expressionSql, containsString(format("delete from @results_database_schema.cohort_summary_stats where %s = @target_cohort_id and mode_id = 1;", cohortFieldName)));
    assertThat(expressionSql, matchesPattern(buildPattern(format(".*insert.+\\.cohort_summary_stats \\(%s,.+"
            + "select @target_cohort_id as %s.+"
            + "where sr\\.mode_id = 1.+sr\\.%s = @target_cohort_id.+", cohortFieldName, cohortFieldName, cohortFieldName))));

  }

  /**
   * Checks that options are set properly.
   *
   */
  @Test
  public void checkCohortFieldName() {

    CohortExpression expression = new CohortExpression();
    expression.conceptSets = new ConceptSet[0];
    expression.primaryCriteria = new PrimaryCriteria();
    expression.primaryCriteria.observationWindow = new ObservationFilter();
    InclusionRule inclusionRule = new InclusionRule();
    inclusionRule.expression = new CriteriaGroup();
    expression.inclusionRules.add(inclusionRule);

    CohortExpressionQueryBuilder builder = new CohortExpressionQueryBuilder();

    // test with null options
    String noOptionsSql = builder.buildExpressionQuery(expression, null);
    checkCohortFieldSql(noOptionsSql, "cohort_definition_id");

    // test with default options
    org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = new org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder.BuildExpressionQueryOptions();
    String defaultOptionsSql = builder.buildExpressionQuery(expression, options);
    checkCohortFieldSql(defaultOptionsSql, "cohort_definition_id");

    // check custom id field name
    options.cohortIdFieldName = "custom_id";

    String customFieldSql = builder.buildExpressionQuery(expression, options);
    checkCohortFieldSql(customFieldSql, "custom_id");

  }

  /**
   * Check serialization
   * 
   */
  @Test
  public void checkSerialization() {
    // load a json file and check the value of the options.

    final String TEST_EXPRESSION_JSON = ResourceHelper.GetResourceAsString("/cohortdefinition/buildOptionsTest.json");
    CohortExpressionQueryBuilder.BuildExpressionQueryOptions testOptions = CohortExpressionQueryBuilder.BuildExpressionQueryOptions.fromJson(TEST_EXPRESSION_JSON);
    assertEquals(testOptions.cdmSchema, "testCdmSchema");
    assertEquals(testOptions.cohortId, Integer.valueOf(999));
    assertEquals(testOptions.cohortIdFieldName, "test_cohort_field");
    assertEquals(testOptions.generateStats, Boolean.valueOf(true));
    assertEquals(testOptions.resultSchema, "testResultSchema");
    assertEquals(testOptions.targetTable, "test_target_table");
    assertEquals(testOptions.vocabularySchema, "testVocabSchema");

    // test an invalid json file.
    boolean exceptionThrown = false;
    try {
      String invalidJson = "badJSON...." + TEST_EXPRESSION_JSON;
      CohortExpressionQueryBuilder.BuildExpressionQueryOptions.fromJson(invalidJson);
    }
    catch (RuntimeException re)
    {
      exceptionThrown = true;
    }
    assertTrue("Invalid Json did not throw an exception", exceptionThrown);
  }

}
