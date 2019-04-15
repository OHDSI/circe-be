package org.ohdsi.circe;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Test;
import org.ohdsi.analysis.Utils;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;
import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.CriteriaGroup;
import org.ohdsi.circe.cohortdefinition.LocationRegion;
import org.ohdsi.circe.cohortdefinition.PayerPlanPeriod;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class VersioningTest extends BaseTest {

    @Test
    public void checkDeserializedPayerPlanCompatibility() throws IOException {

        String design = readResource("/versioning/payerPlanCohortExpression.json");
        CohortExpression cohortExpression = Utils.deserialize(design, new TypeReference<CohortExpression>() {});
        assertNotNull(cohortExpression);
        assertEquals(cohortExpression.getMinCdmVersion(), 5.3, 0);
    }

    @Test
    public void checkDeserializedLocationRegionCompatibility() throws IOException {

        String design = readResource("/versioning/locationRegionCohortExpression.json");
        CohortExpression cohortExpression = Utils.deserialize(design, new TypeReference<CohortExpression>() {});
        assertNotNull(cohortExpression);
        assertEquals(cohortExpression.getMinCdmVersion(), 6.1, 0);
    }

    @Test
    public void checkDeserializedMixedCompatibility() throws IOException {

        String design = readResource("/versioning/mixedCohortExpression.json");
        CohortExpression cohortExpression = Utils.deserialize(design, new TypeReference<CohortExpression>() {});
        assertNotNull(cohortExpression);
        assertEquals(cohortExpression.getMinCdmVersion(), 6.1, 0);
    }

    @Test
    public void checkSerializedPayerPlanCompatibility() {

        CohortExpression cohortExpression = new CohortExpression();
        cohortExpression.additionalCriteria = getCriteriaGroupWithCriteriaList(getPayerPlanCriteria());
        String serialized = Utils.serialize(cohortExpression);
        assertThat(serialized, containsString("\"minCdmVersion\":5.3"));
    }

    @Test
    public void checkSerializedLocationRegionCompatibility() {

        CohortExpression cohortExpression = new CohortExpression();
        cohortExpression.additionalCriteria = getCriteriaGroupWithCriteriaList(getLocationRegionCriteria());
        String serialized = Utils.serialize(cohortExpression);
        assertThat(serialized, containsString("\"minCdmVersion\":6.1"));
    }

    @Test
    public void checkSerializedMixedCompatibility() {

        CohortExpression cohortExpression = new CohortExpression();
        cohortExpression.additionalCriteria = getCriteriaGroupWithCriteriaList(getLocationRegionCriteria(), getPayerPlanCriteria());
        String serialized = Utils.serialize(cohortExpression);
        assertThat(serialized, containsString("\"minCdmVersion\":6.1"));
    }

    private CorelatedCriteria getLocationRegionCriteria() {

        return getCorrelatedCriteriaWithCriteria(new LocationRegion());
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
