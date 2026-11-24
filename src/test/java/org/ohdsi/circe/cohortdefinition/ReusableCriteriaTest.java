package org.ohdsi.circe.cohortdefinition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.ohdsi.analysis.Utils;

/**
 * Reusables imported into a cohort expression: the catalog and the references
 * to it survive a round trip, an expression without them serializes as
 * before, and they do not change the SQL.
 */
public class ReusableCriteriaTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  /** A cohort that imported one criterion reusable and one group reusable, and uses each once. */
  private static final String WITH_REUSABLES = "{"
      + "\"ConceptSets\":[{\"id\":0,\"name\":\"MI\",\"expression\":{\"items\":[]}},"
      + "{\"id\":1,\"name\":\"Metformin\",\"expression\":{\"items\":[]}}],"
      + "\"ReusableCriteria\":[{\"Id\":1,\"RefId\":12,\"Version\":3,\"Name\":\"Prior MI\","
      + "\"ConceptSets\":[{\"id\":0,\"name\":\"MI\",\"expression\":{\"items\":[]}}],"
      + "\"Criteria\":{\"ConditionOccurrence\":{\"CodesetId\":0}}}],"
      + "\"ReusableCriteriaGroups\":[{\"Id\":2,\"RefId\":30,\"Version\":1,\"Name\":\"Prior metformin\","
      + "\"ConceptSets\":[{\"id\":0,\"name\":\"Metformin\",\"expression\":{\"items\":[]}}],"
      + "\"CriteriaGroup\":{\"Type\":\"ANY\",\"CriteriaList\":[{\"Criteria\":{\"DrugExposure\":{\"CodesetId\":0}},"
      + "\"StartWindow\":{\"Start\":{\"Days\":365,\"Coeff\":-1},\"End\":{\"Days\":0,\"Coeff\":1}},"
      + "\"Occurrence\":{\"Type\":2,\"Count\":1}}]}}],"
      + "\"PrimaryCriteria\":{\"CriteriaList\":[{\"ConditionOccurrence\":{\"CodesetId\":0,"
      + "\"ReusableCriteriaRef\":{\"Id\":1}}}],"
      + "\"ObservationWindow\":{\"PriorDays\":0,\"PostDays\":0},\"PrimaryCriteriaLimit\":{\"Type\":\"First\"}},"
      + "\"InclusionRules\":[{\"name\":\"Prior metformin\",\"expression\":{\"Type\":\"ALL\","
      + "\"Groups\":[{\"Type\":\"ANY\",\"ReusableCriteriaGroupRef\":{\"Id\":2},"
      + "\"CriteriaList\":[{\"Criteria\":{\"DrugExposure\":{\"CodesetId\":1}},"
      + "\"StartWindow\":{\"Start\":{\"Days\":365,\"Coeff\":-1},\"End\":{\"Days\":0,\"Coeff\":1}},"
      + "\"Occurrence\":{\"Type\":2,\"Count\":1}}]}]}}]"
      + "}";

  private static JsonNode tree(String json) throws Exception {
    return MAPPER.readTree(json);
  }

  /** The same cohort with the catalog and the references taken out. */
  private static String withoutReusables() throws Exception {
    ObjectNode root = (ObjectNode) tree(WITH_REUSABLES);
    root.remove("ReusableCriteria");
    root.remove("ReusableCriteriaGroups");
    ((ObjectNode) root.at("/PrimaryCriteria/CriteriaList/0/ConditionOccurrence")).remove("ReusableCriteriaRef");
    ((ObjectNode) root.at("/InclusionRules/0/expression/Groups/0")).remove("ReusableCriteriaGroupRef");
    return MAPPER.writeValueAsString(root);
  }

  @Test
  public void readsTheCatalogAndTheReferences() {
    CohortExpression expression = CohortExpression.fromJson(WITH_REUSABLES);

    ReusableCriteria criteria = expression.reusableCriteria[0];
    assertEquals(Integer.valueOf(1), criteria.id);
    assertEquals(Integer.valueOf(12), criteria.refId);
    assertEquals(Integer.valueOf(3), criteria.version);
    assertEquals("Prior MI", criteria.name);
    assertEquals("MI", criteria.conceptSets[0].name);
    assertTrue(criteria.criteria instanceof ConditionOccurrence);

    ReusableCriteriaGroup group = expression.reusableCriteriaGroups[0];
    assertEquals(Integer.valueOf(30), group.refId);
    assertEquals("ANY", group.criteriaGroup.type);

    assertEquals(Integer.valueOf(1), expression.primaryCriteria.criteriaList[0].reusableCriteriaRef.id);
    assertEquals(Integer.valueOf(2), expression.inclusionRules.get(0).expression.groups[0].reusableCriteriaGroupRef.id);
  }

  @Test
  public void keepsTheCatalogAndTheReferencesThroughARoundTrip() throws Exception {
    JsonNode saved = tree(Utils.serialize(CohortExpression.fromJson(WITH_REUSABLES)));
    JsonNode original = tree(WITH_REUSABLES);

    assertEquals(original.get("ReusableCriteria").get(0).get("RefId"), saved.get("ReusableCriteria").get(0).get("RefId"));
    assertEquals(original.at("/ReusableCriteria/0/Criteria"), saved.at("/ReusableCriteria/0/Criteria"));
    assertEquals(original.at("/ReusableCriteria/0/ConceptSets/0/name"), saved.at("/ReusableCriteria/0/ConceptSets/0/name"));
    assertEquals(original.at("/ReusableCriteriaGroups/0/Name"), saved.at("/ReusableCriteriaGroups/0/Name"));
    assertEquals(original.at("/ReusableCriteriaGroups/0/CriteriaGroup/Type"), saved.at("/ReusableCriteriaGroups/0/CriteriaGroup/Type"));
    assertEquals(original.at("/PrimaryCriteria/CriteriaList/0/ConditionOccurrence/ReusableCriteriaRef"),
        saved.at("/PrimaryCriteria/CriteriaList/0/ConditionOccurrence/ReusableCriteriaRef"));
    assertEquals(original.at("/InclusionRules/0/expression/Groups/0/ReusableCriteriaGroupRef"),
        saved.at("/InclusionRules/0/expression/Groups/0/ReusableCriteriaGroupRef"));
  }

  @Test
  public void writesNothingNewForAnExpressionWithoutReusables() throws Exception {
    String saved = Utils.serialize(CohortExpression.fromJson(withoutReusables()));
    assertFalse(saved, saved.contains("Reusable"));
    CohortExpression expression = CohortExpression.fromJson(withoutReusables());
    assertNull(expression.reusableCriteria);
    assertNull(expression.primaryCriteria.criteriaList[0].reusableCriteriaRef);
  }

  @Test
  public void buildsTheSameSqlWithOrWithoutReusables() throws Exception {
    CohortExpressionQueryBuilder builder = new CohortExpressionQueryBuilder();
    CohortExpressionQueryBuilder.BuildExpressionQueryOptions options =
        new CohortExpressionQueryBuilder.BuildExpressionQueryOptions();
    String with = builder.buildExpressionQuery(CohortExpression.fromJson(WITH_REUSABLES), options);
    String without = builder.buildExpressionQuery(CohortExpression.fromJson(withoutReusables()), options);
    assertEquals(without, with);
  }
}
