package org.ohdsi.circe.check.checkers;

import org.junit.Test;
import org.ohdsi.circe.check.Warning;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.helper.ResourceHelper;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class DuplicatesCriteriaCheckTest {
    private static final CohortExpression WRONG_DUPLICATES_EXPRESSION =
            CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/duplicatesCriteriaCheckIncorrect.json"));
    private static final CohortExpression CORRECT_DUPLICATES_EXPRESSION =
            CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/duplicatesCriteriaCheckCorrect.json"));

    private static final int DUPLICATES_WARNING_COUNT = 16;

    private BaseCheck duplicatesCriteriaCheck = new DuplicatesCriteriaCheck();

    @Test
    public void checkPrimaryRangeIncorrect() {
        List<Warning> warnings = duplicatesCriteriaCheck.check(WRONG_DUPLICATES_EXPRESSION);
        assertEquals(DUPLICATES_WARNING_COUNT, warnings.size());
    }

    @Test
    public void checkPrimaryRangeCorrect() {
        List<Warning> warnings = duplicatesCriteriaCheck.check(CORRECT_DUPLICATES_EXPRESSION);
        assertEquals(0, warnings.size());
    }
}
