package org.ohdsi.circe;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Test;
import org.ohdsi.analysis.Utils;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;
import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.CriteriaGroup;
import org.ohdsi.circe.cohortdefinition.PayerPlanPeriod;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class VersioningTest extends BaseTest {

    @Test
    public void checkSerializedNonVersionedEmpty() {

        CohortExpression cohortExpression = new CohortExpression();
        String serialized = Utils.serialize(cohortExpression);
        assertThat(serialized, containsString("\"cdmVersionRange\":\">=5.0.0\""));
    }

    @Test
    public void checkSerializedNonVersionedPayerPlan() {

        CohortExpression cohortExpression = new CohortExpression();
        cohortExpression.additionalCriteria = getCriteriaGroupWithCriteriaList(getPayerPlanCriteria());
        String serialized = Utils.serialize(cohortExpression);
        assertThat(serialized, containsString("\"cdmVersionRange\":\">=5.3.0\""));
    }

    @Test
    public void checkSerializedVersionedPayerPlan() {

        CohortExpression cohortExpression = new CohortExpression();
        cohortExpression.additionalCriteria = getCriteriaGroupWithCriteriaList(getPayerPlanCriteria());
        // User defined constraint
        cohortExpression.setCdmVersionRange("<6.0.0");
        String serialized = Utils.serialize(cohortExpression);
        assertThat(serialized, containsString("\"cdmVersionRange\":\"^5.3.0\""));
    }

    @Test
    public void checkDeserializedPayerPlanCompatibility() throws IOException {

        String design = readResource("/versioning/payerPlanCohortExpression.json");
        CohortExpression cohortExpression = Utils.deserialize(design, new TypeReference<CohortExpression>() {});
        assertNotNull(cohortExpression);
        assertEquals(cohortExpression.getCdmVersionRange(), ">=5.3.0");
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