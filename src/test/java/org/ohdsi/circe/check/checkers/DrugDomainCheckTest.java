package org.ohdsi.circe.check.checkers;

import org.junit.Test;
import org.ohdsi.circe.check.Warning;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.helper.ResourceHelper;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class DrugDomainCheckTest {
    private static final CohortExpression INCORRECT_DRUG_DOMAIN_EXPRESSION =
            CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/drugDomainCheckIncorrect.json"));
    private static final CohortExpression CORRECT_DRUG_DOMAIN_EXPRESSION =
            CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/drugDomainCheckCorrect.json"));

    private static final int DRUG_DOMAIN_WARNING_COUNT = 1;

    private BaseCheck drugDomainCheck = new DrugDomainCheck();

    @Test
    public void checkDuplicatesIncorrect() {
        List<Warning> warnings = drugDomainCheck.check(INCORRECT_DRUG_DOMAIN_EXPRESSION);
        assertEquals(DRUG_DOMAIN_WARNING_COUNT, warnings.size());
    }

    @Test
    public void checkDuplicatesCorrect() {
        List<Warning> warnings = drugDomainCheck.check(CORRECT_DRUG_DOMAIN_EXPRESSION);
        assertEquals(0, warnings.size());
    }
}
