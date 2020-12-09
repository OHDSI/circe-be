package org.ohdsi.circe.check.checkers;

import org.junit.Test;
import org.ohdsi.circe.check.Warning;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.helper.ResourceHelper;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TimePatternCheckTest {
    private static final CohortExpression WRONG_TIME_PATTERN_EXPRESSION =
            CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/timePatternCheckIncorrect.json"));
    private static final CohortExpression CORRECT_TIME_PATTERN_EXPRESSION =
            CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/timePatternCheckCorrect.json"));

    private static final int TIME_PATTERN_WARNING_COUNT = 3;

    private BaseCheck timePatternCheck = new TimePatternCheck();

    @Test
    public void checkPrimaryRangeIncorrect() {
        List<Warning> warnings = timePatternCheck.check(WRONG_TIME_PATTERN_EXPRESSION);
        assertEquals(TIME_PATTERN_WARNING_COUNT, warnings.size());
    }

    @Test
    public void checkPrimaryRangeCorrect() {
        List<Warning> warnings = timePatternCheck.check(CORRECT_TIME_PATTERN_EXPRESSION);
        assertEquals(0, warnings.size());
    }
}
