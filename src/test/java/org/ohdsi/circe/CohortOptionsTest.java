package org.ohdsi.circe;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;
import org.ohdsi.circe.cohortdefinition.CohortExpression;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static java.lang.String.format;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.circe.cohortdefinition.ConceptSet;
import org.ohdsi.circe.cohortdefinition.ObservationFilter;
import org.ohdsi.circe.cohortdefinition.PrimaryCriteria;

public class CohortOptionsTest extends BaseTest {

	private Pattern buildPattern(String regex) {
		return Pattern.compile(regex,Pattern.DOTALL);
	}
	
	private void checkCohortFieldSql(String expressionSql, String cohortFieldName) {
		
		// check inserts for cohort table
		assertThat(expressionSql, containsString(format("DELETE FROM @target_database_schema.@target_cohort_table where %s = @target_cohort_id",cohortFieldName)));
		assertThat(expressionSql, matchesPattern(buildPattern(format(".*INSERT INTO @target_database_schema\\.@target_cohort_table \\(%s,.*select @target_cohort_id as %s,.*",cohortFieldName, cohortFieldName))));
		
		// check inserts for cohort_inclusion_result, mode = 0
		assertThat(expressionSql, containsString(format("delete from @results_database_schema.cohort_inclusion_result where %s = @target_cohort_id and mode_id = 0;", cohortFieldName)));
		assertThat(expressionSql, matchesPattern(buildPattern(format(".*insert into @results_database_schema\\.cohort_inclusion_result \\(%s,.*select @target_cohort_id as %s,.+0 as mode_id.+", cohortFieldName, cohortFieldName))));

		// check inserts for cohort_inclusion_stats, mode = 0
		assertThat(expressionSql, containsString(format("delete from @results_database_schema.cohort_inclusion_stats where %s = @target_cohort_id and mode_id = 0;",cohortFieldName)));
		assertThat(expressionSql, matchesPattern(buildPattern(format(".*insert into @results_database_schema\\.cohort_inclusion_stats \\(%s,.*"
						+ "select ir\\.%s,.*0 as mode_id.*"
						+ "CROSS JOIN \\(.+where %s = @target_cohort_id\\).*WHERE ir.%s = @target_cohort_id.+",cohortFieldName,cohortFieldName,cohortFieldName,cohortFieldName))));

		// check inserts for cohort_summary_stats, mode = 0
		assertThat(expressionSql, containsString(format("delete from @results_database_schema.cohort_summary_stats where %s = @target_cohort_id and mode_id = 0;",cohortFieldName)));
		assertThat(expressionSql, matchesPattern(buildPattern(format(".*insert.+\\.cohort_summary_stats \\(%s,.+"
						+ "select @target_cohort_id as %s,.+"
						+ "where sr\\.mode_id = 0.+sr\\.%s = @target_cohort_id.+",cohortFieldName,cohortFieldName,cohortFieldName))));

		// check inserts for cohort_inclusion_result, mode = 1
		assertThat(expressionSql, containsString(format("delete from @results_database_schema.cohort_inclusion_result where %s = @target_cohort_id and mode_id = 1;",cohortFieldName)));
		assertThat(expressionSql, matchesPattern(buildPattern(format(".*insert into @results_database_schema\\.cohort_inclusion_result \\(%s,.+select @target_cohort_id as %s,.+1 as mode_id.+",cohortFieldName,cohortFieldName))));

		// check inserts for cohort_inclusion_stats, mode = 1
		assertThat(expressionSql, containsString(format("delete from @results_database_schema.cohort_inclusion_stats where %s = @target_cohort_id and mode_id = 1;", cohortFieldName)));
		assertThat(expressionSql, matchesPattern(buildPattern(format(".*insert into @results_database_schema\\.cohort_inclusion_stats \\(%s,.+"
						+ "select ir\\.%s,.+, 1 as mode_id.+"
						+ "CROSS JOIN \\(.+where %s = @target_cohort_id.*\\).+WHERE ir.%s = @target_cohort_id.+",cohortFieldName,cohortFieldName,cohortFieldName,cohortFieldName))));

		// check inserts for cohort_summary_stats, mode = 1
		assertThat(expressionSql, containsString(format("delete from @results_database_schema.cohort_summary_stats where %s = @target_cohort_id and mode_id = 1;",cohortFieldName)));
		assertThat(expressionSql, matchesPattern(buildPattern(format(".*insert.+\\.cohort_summary_stats \\(%s,.+"
						+ "select @target_cohort_id as %s.+"
						+ "where sr\\.mode_id = 1.+sr\\.%s = @target_cohort_id.+",cohortFieldName,cohortFieldName,cohortFieldName))));

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
		
		CohortExpressionQueryBuilder builder = new CohortExpressionQueryBuilder();
		
		// test with null options
		String noOptionsSql = builder.buildExpressionQuery(expression,null);
		checkCohortFieldSql(noOptionsSql, "cohort_definition_id");

		// test with default options
		org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = new org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder.BuildExpressionQueryOptions();
		String defaultOptionsSql = builder.buildExpressionQuery(expression,options);
		checkCohortFieldSql(defaultOptionsSql, "cohort_definition_id");

		// check custom id field name
		options.cohortIdFieldName = "custom_id";
		
		String customFieldSql = builder.buildExpressionQuery(expression, options);
		checkCohortFieldSql(customFieldSql, "custom_id");
		
	}
}
