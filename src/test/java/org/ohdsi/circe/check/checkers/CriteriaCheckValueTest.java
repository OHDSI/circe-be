package org.ohdsi.circe.check.checkers;

import org.junit.Test;
import org.ohdsi.circe.check.Warning;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.helper.ResourceHelper;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CriteriaCheckValueTest {
  private static final CohortExpression WRONG_PRIMARY_EXPRESSION =
      CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/primaryCriteriaCheckValueIncorrect.json"));
  private static final CohortExpression CORRECT_PRIMARY_EXPRESSION =
      CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/primaryCriteriaCheckValueCorrect.json"));

  private static final CohortExpression WRONG_ADDITIONAL_EXPRESSION =
      CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/additionalCriteriaCheckValueIncorrect.json"));
  private static final CohortExpression WRONG_ADDITIONAL_EXPRESSION_HOUR =
      CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/additionalCriteriaCheckValueIncorrectHour.json"));
  private static final CohortExpression CORRECT_ADDITIONAL_EXPRESSION =
    CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/additionalCriteriaCheckValueCorrect.json"));
  private static final CohortExpression CORRECT_ADDITIONAL_EXPRESSION_MINUTE =
      CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/additionalCriteriaCheckValueCorrectMinute.json"));

  private static final CohortExpression WRONG_INCLUSION_EXPRESSION =
      CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/inclusionRulesCheckValueIncorrect.json"));
  private static final CohortExpression CORRECT_INCLUSION_EXPRESSION =
      CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/inclusionRulesCheckValueCorrect.json"));

  private static final CohortExpression WRONG_CENSORING_EXPRESSION =
      CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/censoringEventCheckValueIncorrect.json"));
  private static final CohortExpression CORRECT_CENSORING_EXPRESSION =
      CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/censoringEventCheckValueCorrect.json"));

  private static final CohortExpression WRONG_EMPTY_CRITERIA_EXPRESSION =
      CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/emptyDemographicCheckIncorrect.json"));
  private static final CohortExpression CORRECT_EMPTY_CRITERIA_EXPRESSION =
      CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/emptyDemographicCheckCorrect.json"));

  private static final CohortExpression EMPTY_CENSORING_CRITERIA_EXPRESSION =
      CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/emptyCensoringCriteriaList.json"));

  private static final CohortExpression EMPTY_PRIMARY_CRITERIA_EXPRESSION =
      CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/emptyPrimaryCriteriaList.json"));

  private static final CohortExpression EMPTY_INCLUSION_RULES_EXPRESSION =
      CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/emptyInclusionRules.json"));

  private static final CohortExpression EMPTY_CORRELATED_CRITERIA_EXPRESSION =
      CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/emptyCorrelatedCriteria.json"));

  private static final CohortExpression NO_EXIT_CRITERIA_CHECK =
      CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/noExitCriteriaCheck.json"));

  private static final CohortExpression NO_EXIT_CRITERIA_CHECK_EARLIEST_EVENT =
      CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/checkers/noExitCriteriaCheckEarliestEvent.json"));

  private static final int RANGE_PRIMARY_WARNING_COUNT = 148;
  private static final int CONCEPT_PRIMARY_WARNING_COUNT = 61;
  private static final int TEXT_PRIMARY_WARNING_COUNT = 5;

  private static final int RANGE_ADDITIONAL_WARNING_COUNT = 34;
  private static final int CONCEPT_ADDITIONAL_WARNING_COUNT = 23;
  private static final int TEXT_ADDITIONAL_WARNING_COUNT = 2;

  private static final int RANGE_INCLUSION_WARNING_COUNT = 28;
  private static final int CONCEPT_INCLUSION_WARNING_COUNT = 18;
  private static final int TEXT_INCLUSION_WARNING_COUNT = 2;

  private static final int RANGE_CENSORING_WARNING_COUNT = 23;
  private static final int CONCEPT_CENSORING_WARNING_COUNT = 9;
  private static final int TEXT_CENSORING_WARNING_COUNT = 6;

  private static final int EMPTY_CRITERIA_WARNING_COUNT = 1;

  private BaseCheck rangeCheck = new RangeCheck();
  private BaseCheck conceptCheck = new ConceptCheck();
  private BaseCheck attributeCheck = new AttributeCheck();
  private BaseCheck textCheck = new TextCheck();
  private BaseCheck noExitCriteriaCheck = new NoExitCriteriaCheck();

  @Test
  public void checkPrimaryRangeIncorrect() {
    List<Warning> warnings = rangeCheck.check(WRONG_PRIMARY_EXPRESSION);
    assertEquals(RANGE_PRIMARY_WARNING_COUNT, warnings.size());
  }

  @Test
  public void checkPrimaryRangeCorrect() {
    List<Warning> warnings = rangeCheck.check(CORRECT_PRIMARY_EXPRESSION);
    assertEquals(Collections.emptyList(), warnings);
  }

  @Test
  public void checkPrimaryConceptIncorrect() {
    List<Warning> warnings = conceptCheck.check(WRONG_PRIMARY_EXPRESSION);
    assertEquals(CONCEPT_PRIMARY_WARNING_COUNT, warnings.size());
  }

  @Test
  public void checkPrimaryConceptCorrect() {
    List<Warning> warnings = conceptCheck.check(CORRECT_PRIMARY_EXPRESSION);
    assertEquals(Collections.emptyList(), warnings);
  }

  @Test
  public void checkPrimaryTextIncorrect() {
    List<Warning> warnings = textCheck.check(WRONG_PRIMARY_EXPRESSION);
    assertEquals(TEXT_PRIMARY_WARNING_COUNT, warnings.size());
  }

  @Test
  public void checkPrimaryTextCorrect() {
    List<Warning> warnings = textCheck.check(CORRECT_PRIMARY_EXPRESSION);
    assertEquals(Collections.emptyList(), warnings);
  }

  @Test
  public void checkAdditionalRangeIncorrect() {
    List<Warning> warnings = rangeCheck.check(WRONG_ADDITIONAL_EXPRESSION);
    assertEquals(RANGE_ADDITIONAL_WARNING_COUNT, warnings.size());
  }
  @Test
  public void checkAdditionalRangeIncorrectHour() {
    List<Warning> warnings = rangeCheck.check(WRONG_ADDITIONAL_EXPRESSION_HOUR);
    assertEquals(RANGE_ADDITIONAL_WARNING_COUNT, warnings.size());
  }

  @Test
  public void checkAdditionalRangeCorrect() {
    List<Warning> warnings = rangeCheck.check(CORRECT_ADDITIONAL_EXPRESSION);
    assertEquals(Collections.emptyList(), warnings);
  }
  @Test
  public void checkAdditionalRangeCorrectMinute() {
    List<Warning> warnings = rangeCheck.check(CORRECT_ADDITIONAL_EXPRESSION_MINUTE);
    assertEquals(Collections.emptyList(), warnings);
  }

  @Test
  public void checkAdditionalConceptIncorrect() {
    List<Warning> warnings = conceptCheck.check(WRONG_ADDITIONAL_EXPRESSION);
    assertEquals(CONCEPT_ADDITIONAL_WARNING_COUNT, warnings.size());
  }

  @Test
  public void checkAdditionalConceptCorrect() {
    List<Warning> warnings = conceptCheck.check(CORRECT_ADDITIONAL_EXPRESSION);
    assertEquals(Collections.emptyList(), warnings);
  }

  @Test
  public void checkAdditionalTextIncorrect() {
    List<Warning> warnings = textCheck.check(WRONG_ADDITIONAL_EXPRESSION);
    assertEquals(TEXT_ADDITIONAL_WARNING_COUNT, warnings.size());
  }

  @Test
  public void checkAdditionalTextCorrect() {
    List<Warning> warnings = textCheck.check(CORRECT_ADDITIONAL_EXPRESSION);
    assertEquals(Collections.emptyList(), warnings);
  }

  @Test
  public void checkInclusionRangeIncorrect() {
    List<Warning> warnings = rangeCheck.check(WRONG_INCLUSION_EXPRESSION);
    assertEquals(RANGE_INCLUSION_WARNING_COUNT, warnings.size());
  }

  @Test
  public void checkInclusionRangeCorrect() {
    List<Warning> warnings = rangeCheck.check(CORRECT_INCLUSION_EXPRESSION);
    assertEquals(Collections.emptyList(), warnings);
  }

  @Test
  public void checkInclusionConceptIncorrect() {
    List<Warning> warnings = conceptCheck.check(WRONG_INCLUSION_EXPRESSION);
    assertEquals(CONCEPT_INCLUSION_WARNING_COUNT, warnings.size());
  }

  @Test
  public void checkInclusionConceptCorrect() {
    List<Warning> warnings = conceptCheck.check(CORRECT_INCLUSION_EXPRESSION);
    assertEquals(Collections.emptyList(), warnings);
  }

  @Test
  public void checkInclusionTextIncorrect() {
    List<Warning> warnings = textCheck.check(WRONG_INCLUSION_EXPRESSION);
    assertEquals(TEXT_INCLUSION_WARNING_COUNT, warnings.size());
  }

  @Test
  public void checkInclusionTextCorrect() {
    List<Warning> warnings = textCheck.check(CORRECT_INCLUSION_EXPRESSION);
    assertEquals(Collections.emptyList(), warnings);
  }

  @Test
  public void checkCensoringRangeIncorrect() {
    List<Warning> warnings = rangeCheck.check(WRONG_CENSORING_EXPRESSION);
    assertEquals(RANGE_CENSORING_WARNING_COUNT, warnings.size());
  }

  @Test
  public void checkCensoringRangeCorrect() {
    List<Warning> warnings = rangeCheck.check(CORRECT_CENSORING_EXPRESSION);
    assertEquals(Collections.emptyList(), warnings);
  }

  @Test
  public void checkCensoringConceptIncorrect() {
    List<Warning> warnings = conceptCheck.check(WRONG_CENSORING_EXPRESSION);
    assertEquals(CONCEPT_CENSORING_WARNING_COUNT, warnings.size());
  }

  @Test
  public void checkCensoringConceptCorrect() {
    List<Warning> warnings = conceptCheck.check(CORRECT_CENSORING_EXPRESSION);
    assertEquals(Collections.emptyList(), warnings);
  }

  @Test
  public void checkCensoringTextncorrect() {
    List<Warning> warnings = textCheck.check(WRONG_CENSORING_EXPRESSION);
    assertEquals(TEXT_CENSORING_WARNING_COUNT, warnings.size());
  }

  @Test
  public void checkCensoringTextCorrect() {
    List<Warning> warnings = textCheck.check(CORRECT_CENSORING_EXPRESSION);
    assertEquals(Collections.emptyList(), warnings);
  }

  @Test
  public void checkAttributeIncorrect() {
    List<Warning> warnings = attributeCheck.check(WRONG_EMPTY_CRITERIA_EXPRESSION);
    assertEquals(EMPTY_CRITERIA_WARNING_COUNT, warnings.size());
  }

  @Test
  public void checkAttributeCorrect() {
    List<Warning> warnings = attributeCheck.check(CORRECT_EMPTY_CRITERIA_EXPRESSION);
    assertEquals(Collections.emptyList(), warnings);
  }

  @Test
  public void checkEmptyCensoringCriteria() {
    List<Warning> warnings = attributeCheck.check(EMPTY_CENSORING_CRITERIA_EXPRESSION);
    assertEquals(Collections.emptyList(), warnings);
  }

  @Test
  public void checkEmptyPrimaryCriteria() {
    List<Warning> warnings = rangeCheck.check(EMPTY_PRIMARY_CRITERIA_EXPRESSION);
    assertEquals(Collections.emptyList(), warnings);
  }

  @Test
  public void checkEmptyInclusionRules() {
    List<Warning> warnings = rangeCheck.check(EMPTY_INCLUSION_RULES_EXPRESSION);
    assertEquals(Collections.emptyList(), warnings);
  }

  @Test
  public void checkEmptyCorrelatedCriteria() {
    List<Warning> warnings = rangeCheck.check(EMPTY_CORRELATED_CRITERIA_EXPRESSION);
    assertEquals(Collections.emptyList(), warnings);
  }

  @Test
  public void checkNoExitCriteria() {
    List<Warning> warnings = noExitCriteriaCheck.check(NO_EXIT_CRITERIA_CHECK);
    assertEquals(0, warnings.size());
  }

  @Test
  public void checkNoExitCriteriaEarliestEvent() {
    List<Warning> warnings = noExitCriteriaCheck.check(NO_EXIT_CRITERIA_CHECK_EARLIEST_EVENT);
    assertEquals(1, warnings.size());
  }
}
