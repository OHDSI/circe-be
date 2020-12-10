package org.ohdsi.circe.check.checkers;

import org.junit.Test;
import org.ohdsi.circe.check.Warning;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.helper.ResourceHelper;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class EventsProgressionCheckTest {
    private static final CohortExpression INCORRECT_EVENTS_PROGRESSION_EXPRESSION =
            CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/eventsProgressionCheckIncorrect.json"));
    private static final CohortExpression CORRECT_EVENTS_PROGRESSION_EXPRESSION =
            CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/eventsProgressionCheckCorrect.json"));

    private static final int EVENTS_PROGRESSION_WARNING_COUNT = 2;

    private BaseCheck eventsProgressionCheck = new EventsProgressionCheck();

    @Test
    public void checkDuplicatesIncorrect() {
        List<Warning> warnings = eventsProgressionCheck.check(INCORRECT_EVENTS_PROGRESSION_EXPRESSION);
        assertEquals(EVENTS_PROGRESSION_WARNING_COUNT, warnings.size());
    }

    @Test
    public void checkDuplicatesCorrect() {
        List<Warning> warnings = eventsProgressionCheck.check(CORRECT_EVENTS_PROGRESSION_EXPRESSION);
        assertEquals(0, warnings.size());
    }
}
