package org.ohdsi.circe.check.checkers;

import org.junit.Test;
import org.ohdsi.circe.check.Warning;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.helper.ResourceHelper;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class DomainTypeCheckTest {
    private static final CohortExpression INCORRECT_DOMAIN_TYPE_EXPRESSION =
            CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/domainTypeCheckIncorrect.json"));
    private static final CohortExpression CORRECT_DOMAIN_TYPE_EXPRESSION =
            CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/domainTypeCheckCorrect.json"));

    private static final int DOMAIN_TYPE_WARNING_COUNT =9;

    private BaseCheck domainTypeCheck = new DomainTypeCheck();

    @Test
    public void checkDuplicatesIncorrect() {
        List<Warning> warnings = domainTypeCheck.check(INCORRECT_DOMAIN_TYPE_EXPRESSION);
        assertEquals(1, warnings.size());
        int incorrectDomainsCount = warnings.get(0).toMessage().split("at initial event").length;
        assertEquals(DOMAIN_TYPE_WARNING_COUNT, incorrectDomainsCount);
    }

    @Test
    public void checkDuplicatesCorrect() {
        List<Warning> warnings = domainTypeCheck.check(CORRECT_DOMAIN_TYPE_EXPRESSION);
        assertEquals(0, warnings.size());
    }
}
