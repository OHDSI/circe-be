package org.ohdsi.circe.cohortdefinition.negativecontrols;

import org.junit.Assert;
import org.junit.Test;
import org.ohdsi.circe.BaseTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;
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
		testExpression.domainIds.add("CONDITION");
		testExpression.domainIds.add("DRUG");
		testExpression.domainIds.add("DEVICE");
		testExpression.domainIds.add("MEASUREMENT");
		testExpression.domainIds.add("OBSERVATION");
		testExpression.domainIds.add("PROCEDURE");
		testExpression.domainIds.add("VISIT");

		CohortExpressionQueryBuilder queryBuilder = new CohortExpressionQueryBuilder();
		String query = queryBuilder.buildExpressionQuery(testExpression);

		assertThat(query, equalToIgnoringWhiteSpace(readResource("/negativecontrols/allDomainsDetectTrueOccurrenceFirst.sql")));

	}

	@Test
	public void twoDomainsDetectTrueOccurrenceAll() throws Exception {

		OutcomeCohortExpression testExpression = new OutcomeCohortExpression();
		testExpression.occurrenceType = OccurrenceType.ALL;
		testExpression.detectOnDescendants = true;
		testExpression.domainIds.add("CONDITION");
		testExpression.domainIds.add("MEASUREMENT");

		CohortExpressionQueryBuilder queryBuilder = new CohortExpressionQueryBuilder();
		String query = queryBuilder.buildExpressionQuery(testExpression);

		assertThat(query, equalToIgnoringWhiteSpace(readResource("/negativecontrols/twoDomainsDetectTrueOccurrenceAll.sql")));

	}
	
	@Test
	public void oneDomainDetectFalseOccurenceFirst() throws Exception {

        OutcomeCohortExpression testExpression = new OutcomeCohortExpression();
        testExpression.occurrenceType = OccurrenceType.FIRST;
        testExpression.detectOnDescendants = false;
        testExpression.domainIds.add("PROCEDURE");

		CohortExpressionQueryBuilder queryBuilder = new CohortExpressionQueryBuilder();
		String query = queryBuilder.buildExpressionQuery(testExpression);

		assertThat(query, equalToIgnoringWhiteSpace(readResource("/negativecontrols/oneDomainDetectFalseOccurrenceFirst.sql")));
	}	
}
