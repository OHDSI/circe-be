package org.ohdsi.circe.check.checkers;

import org.junit.Test;
import org.ohdsi.circe.check.Warning;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.helper.ResourceHelper;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ConceptSetCriteriaCheckTest {
    private static final CohortExpression INCORRECT_CONCEPT_SET_EXPRESSION =
            CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/conceptSetCriteriaCheckIncorrect.json"));
    private static final CohortExpression CORRECT_CONCEPT_SET_EXPRESSION =
            CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/conceptSetCriteriaCheckCorrect.json"));

    private static final int DUPLICATES_WARNING_COUNT = 12;

    private BaseCheck conceptSetCriteriaCheck = new ConceptSetCriteriaCheck();

    @Test
    public void checkConceptSetIncorrect() {
        List<Warning> warnings = conceptSetCriteriaCheck.check(INCORRECT_CONCEPT_SET_EXPRESSION);
        assertEquals(DUPLICATES_WARNING_COUNT, warnings.size());
    }

    @Test
    public void checkConceptSetCorrect() {
        List<Warning> warnings = conceptSetCriteriaCheck.check(CORRECT_CONCEPT_SET_EXPRESSION);
        assertEquals(0, warnings.size());
    }
}
