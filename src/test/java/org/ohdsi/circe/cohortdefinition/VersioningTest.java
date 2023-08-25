package org.ohdsi.circe.cohortdefinition;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Test;
import org.ohdsi.analysis.Utils;
import org.ohdsi.circe.BaseTest;


import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;

public class VersioningTest extends BaseTest {

    /**
     * Checks deriving of default CDM range for Cohort Expression.
     *
     * Checks that, when a user has not filled "cdmVersionRange" manually,
     * an empty Cohort Expression after serialization contains default derived CDM version range.
     */
    @Test
    public void checkSerializedNonVersionedEmpty() {

        CohortExpression cohortExpression = new CohortExpression();
        String serialized = Utils.serialize(cohortExpression);
        assertThat(serialized, containsString("\"cdmVersionRange\":\">=5.0.0\""));
    }

    /**
     * Checks deriving of non-default CDM range for Cohort Expression.
     *
     * Checks that, when a user has not filled "cdmVersionRange" manually,
     * a Cohort Expression having Payer Plan Period contains appropriate derived CDM version range after serialization.
     */
    @Test
    public void checkSerializedNonVersionedPayerPlan() {

        CohortExpression cohortExpression = new CohortExpression();
        cohortExpression.additionalCriteria = getCriteriaGroupWithCriteriaList(getPayerPlanCriteria());
        String serialized = Utils.serialize(cohortExpression);
        assertThat(serialized, containsString("\"cdmVersionRange\":\">=5.3\""));
    }

    /**
     * Checks that, if user-defined CDM range matches derived CDM range for Cohort Expression,
     * the serialization finishes successfully.
     */
    @Test
    public void checkSerializedProperlyVersionedPayerPlan() {

        CohortExpression cohortExpression = new CohortExpression();
        cohortExpression.additionalCriteria = getCriteriaGroupWithCriteriaList(getPayerPlanCriteria());
        // User defined constraint
        cohortExpression.setCdmVersionRange(">5.4.0");
        String serialized = Utils.serialize(cohortExpression);
        assertThat(serialized, containsString("\"cdmVersionRange\":\">5.4.0\""));
    }

    /**
     * Checks that, if user-defined CDM range doesn't match derived CDM range for Cohort Expression,
     * the serialization fails.
     */
    @Test
    public void checkSerializedImproperlyVersionedPayerPlan() {

        CohortExpression cohortExpression = new CohortExpression();
        cohortExpression.additionalCriteria = getCriteriaGroupWithCriteriaList(getPayerPlanCriteria());
        // User defined constraint
        cohortExpression.setCdmVersionRange("<6.0.0");

        try {
            Utils.serialize(cohortExpression);
        } catch (RuntimeException ex) {
            assertEquals("User-defined CDM range (<6.0.0) does not include derived CDM range (>=5.3.0)", ex.getCause().getMessage());
        }
    }

    @Test
    public void checkDeserializedPayerPlanCompatibility() throws IOException {

        String design = readResource("/versioning/payerPlanCohortExpression.json");
        CohortExpression cohortExpression = Utils.deserialize(design, new TypeReference<CohortExpression>() {});
        assertNotNull(cohortExpression);
        assertEquals(cohortExpression.getCdmVersionRange(), ">=5.3");
    }

    private CorelatedCriteria getPayerPlanCriteria() {

        return getCorrelatedCriteriaWithCriteria(new PayerPlanPeriod());
    }

    private CorelatedCriteria getCorrelatedCriteriaWithCriteria(Criteria criteria) {

        CorelatedCriteria corelatedCriteria = new CorelatedCriteria();
        corelatedCriteria.criteria = criteria;
        return corelatedCriteria;
    }

    private CriteriaGroup getCriteriaGroupWithCriteriaList(CorelatedCriteria... corelatedCriteriaList) {

        CriteriaGroup criteriaGroup = new CriteriaGroup();
        criteriaGroup.criteriaList = corelatedCriteriaList;
        return criteriaGroup;
    }
}