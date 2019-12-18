package org.ohdsi.circe.cohortdefinition.negativecontrols;

import org.junit.Test;
import org.ohdsi.circe.BaseTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;

/**
 *
 * @author cknoll1
 */
public class NegativeControlTests extends BaseTest {

	@Test
	public void allDomainsDetectTrueOccurrenceFirst() throws Exception {

		OutcomeCohortExpression testExpression = new OutcomeCohortExpression();
		testExpression.occurrenceType = OccurrenceType.FIRST;
		testExpression.detectOnDescendants = true;
		testExpression.domains.add("CONDITION");
		testExpression.domains.add("DRUG");
		testExpression.domains.add("DEVICE");
		testExpression.domains.add("MEASUREMENT");
		testExpression.domains.add("OBSERVATION");
		testExpression.domains.add("PROCEDURE");
		testExpression.domains.add("VISIT");

		CohortExpressionQueryBuilder queryBuilder = new CohortExpressionQueryBuilder();
		String query = queryBuilder.buildExpressionQuery(testExpression);

		assertThat(query, equalToIgnoringWhiteSpace(readResource("/negativecontrols/allDomainsDetectTrueOccurrenceFirst.sql")));

	}

	@Test
	public void twoDomainsDetectTrueOccurrenceAll() throws Exception {

		OutcomeCohortExpression testExpression = new OutcomeCohortExpression();
		testExpression.occurrenceType = OccurrenceType.ALL;
		testExpression.detectOnDescendants = true;
		testExpression.domains.add("CONDITION");
		testExpression.domains.add("MEASUREMENT");

		CohortExpressionQueryBuilder queryBuilder = new CohortExpressionQueryBuilder();
		String query = queryBuilder.buildExpressionQuery(testExpression);

		assertThat(query, equalToIgnoringWhiteSpace(readResource("/negativecontrols/twoDomainsDetectTrueOccurrenceAll.sql")));

	}
	
	@Test
	public void oneDomainDetectFalseOccurenceFirst() throws Exception {

        OutcomeCohortExpression testExpression = new OutcomeCohortExpression();
        testExpression.occurrenceType = OccurrenceType.FIRST;
        testExpression.detectOnDescendants = false;
        testExpression.domains.add("PROCEDURE");

		CohortExpressionQueryBuilder queryBuilder = new CohortExpressionQueryBuilder();
		String query = queryBuilder.buildExpressionQuery(testExpression);

		assertThat(query, equalToIgnoringWhiteSpace(readResource("/negativecontrols/oneDomainDetectFalseOccurrenceFirst.sql")));
	}	
}
