package org.ohdsi.circe.check.checkers;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.ohdsi.circe.check.Warning;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.helper.ResourceHelper;

public class UnusedConceptsCheckTest {

    private UnusedConceptsCheck unusedConceptsCheck = new UnusedConceptsCheck();

    @Test
    public void check_usageInTheChildGroups() throws IOException {
        CohortExpression cohortExpression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/childGroupExpression.json"));
        List<Warning> warnings = unusedConceptsCheck.check(cohortExpression);
        assertEquals(Collections.emptyList(), warnings);
    }
}