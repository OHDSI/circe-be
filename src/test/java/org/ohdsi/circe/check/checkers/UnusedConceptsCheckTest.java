package org.ohdsi.circe.check.checkers;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.ohdsi.circe.check.Warning;
import org.ohdsi.circe.check.warnings.ConceptSetWarning;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.helper.ResourceHelper;

public class UnusedConceptsCheckTest {
    private static final CohortExpression UNUSED_CONCEPT_EXPRESSION =
            CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/unusedConceptSet.json"));
    private static final CohortExpression UNUSED_CONCEPT_CORRECT_EXPRESSION =
            CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/unusedConceptSetCorrect.json"));
    private static final long UNUSED_CONCEPT_COUNT = 13;

    private UnusedConceptsCheck unusedConceptsCheck = new UnusedConceptsCheck();

    @Test
    public void check_usageInTheChildGroups() throws IOException {
        CohortExpression cohortExpression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/childGroupExpression.json"));
        List<Warning> warnings = unusedConceptsCheck.check(cohortExpression);
        assertEquals(Collections.emptyList(), warnings);
    }

    @Test
    public void checkUnusedConceptSetCorrect() {
        List<Warning> warnings = unusedConceptsCheck.check(UNUSED_CONCEPT_CORRECT_EXPRESSION);
        warnings.forEach((warning) -> assertNotEquals(ConceptSetWarning.class, warning.getClass()));
    }

    @Test
    public void checkUnusedConceptSet() {
        List<Warning> warnings = unusedConceptsCheck.check(UNUSED_CONCEPT_EXPRESSION);
        long count = warnings
                .stream()
                .filter(warning -> warning instanceof ConceptSetWarning)
                .count();
        assertEquals(UNUSED_CONCEPT_COUNT, count);
    }
}