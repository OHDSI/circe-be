package org.ohdsi.circe.check.checkers;

import org.junit.Test;
import org.ohdsi.circe.check.Warning;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.helper.ResourceHelper;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ConceptSetCriteriaCheckTest {
    private static final CohortExpression INCORRECT_EXPRESSION =
            CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/conceptSetCriteriaCheckIncorrect.json"));
    private static final CohortExpression CORRECT_EXPRESSION =
            CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/conceptSetCriteriaCheckCorrect.json"));

    private static final int WARNING_COUNT = 12;

    private BaseCheck check = new ConceptSetCriteriaCheck();

    @Test
    public void checkIncorrect() {
        List<Warning> warnings = check.check(INCORRECT_EXPRESSION);
        assertEquals(WARNING_COUNT, warnings.size());
    }

    @Test
    public void checkCorrect() {
        List<Warning> warnings = check.check(CORRECT_EXPRESSION);
        assertEquals(0, warnings.size());
    }
}
