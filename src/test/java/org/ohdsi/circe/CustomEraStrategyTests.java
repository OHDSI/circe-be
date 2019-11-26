package org.ohdsi.circe;

import java.util.regex.Pattern;
import org.junit.Test;
import org.ohdsi.circe.cohortdefinition.CohortExpression;

import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.Assert.assertThat;
import static java.lang.String.format;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.circe.cohortdefinition.ConceptSet;
import org.ohdsi.circe.cohortdefinition.CustomEraStrategy;
import org.ohdsi.circe.cohortdefinition.ObservationFilter;
import org.ohdsi.circe.cohortdefinition.PrimaryCriteria;

public class CustomEraStrategyTests extends BaseTest {

  private Pattern buildPattern(String regex) {
    return Pattern.compile(regex, Pattern.DOTALL);
  }

  private void checkDaysSupplyExpression(String expressionSql, String daysSupplyExpression) {

    // escape special characters from daysSupplyExpression
    String escapedExpression = Pattern.quote(daysSupplyExpression);
    // check drug exposure end date expressions are correct
    assertThat(expressionSql, matchesPattern(buildPattern(format(".*INTO #drugTarget.*FROM \\(.*%s.*UNION ALL.*%s.*\\) E.*", escapedExpression, escapedExpression))));
  }

  /**
   * Checks that options are set properly.
   *
   */
  @Test
  public void checkDaysSupplyOverride() {

    CohortExpression expression = new CohortExpression();
    expression.conceptSets = new ConceptSet[0];
    expression.primaryCriteria = new PrimaryCriteria();
    expression.primaryCriteria.observationWindow = new ObservationFilter();
    
    // assign a custom era end strategy
    CustomEraStrategy strat = new CustomEraStrategy();
    strat.drugCodesetId = 0; // required field, but doesn't need to be a valid conceptset ID
    expression.endStrategy = strat;
    
    CohortExpressionQueryBuilder builder = new CohortExpressionQueryBuilder();

    // test with null daysSupply overide
    String noOverrideSql = builder.buildExpressionQuery(expression, null);
    checkDaysSupplyExpression(noOverrideSql, "COALESCE(DRUG_EXPOSURE_END_DATE, DATEADD(day,DAYS_SUPPLY,DRUG_EXPOSURE_START_DATE), DATEADD(day,1,DRUG_EXPOSURE_START_DATE)) as DRUG_EXPOSURE_END_DATE");

    // test with daysSupply 
    strat.daysSupplyOverride = 6;
    String withOverrideSql = builder.buildExpressionQuery(expression, null);
    checkDaysSupplyExpression(withOverrideSql, format("DATEADD(day,%s,DRUG_EXPOSURE_START_DATE) as DRUG_EXPOSURE_END_DATE", strat.daysSupplyOverride));
  }
}
