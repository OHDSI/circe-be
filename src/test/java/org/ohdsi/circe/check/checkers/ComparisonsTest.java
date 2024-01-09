package org.ohdsi.circe.check.checkers;

import org.junit.Test;
import org.ohdsi.circe.cohortdefinition.*;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.function.Predicate;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.Assert.assertEquals;

public class ComparisonsTest {
    private static final String CONCEPT_SET_DUP_ITEMS =
            ResourceHelper.GetResourceAsString("/checkers/conceptSetWithDuplicateItems.json");

    @Test
    public void numberStartIsGreaterThanEnd() {
        NumericRange range = new NumericRange();
        range.value = 3;
        range.extent = 2;
        assertEquals(true, Comparisons.startIsGreaterThanEnd(range));

        range.value = 2;
        range.extent = 3;
        assertEquals(false, Comparisons.startIsGreaterThanEnd(range));
    }

    @Test
    public void dateStartIsGreaterThanEnd() {
        DateRange range = new DateRange();
        range.value = LocalDate.now().toString();
        range.extent = LocalDate.now().minus(1, DAYS).toString();
        assertEquals(true, Comparisons.startIsGreaterThanEnd(range));

        range.value = LocalDate.now().toString();
        range.extent = LocalDate.now().plus(1, DAYS).toString();
        assertEquals(false, Comparisons.startIsGreaterThanEnd(range));
    }

    @Test
    public void periodStartIsGreaterThanEnd() {
        Period period = new Period();
        period.startDate = LocalDate.now().toString();
        period.endDate = LocalDate.now().minus(1, DAYS).toString();
        assertEquals(true, Comparisons.startIsGreaterThanEnd(period));

        period.startDate = LocalDate.now().toString();
        period.endDate = LocalDate.now().plus(1, DAYS).toString();
        assertEquals(false, Comparisons.startIsGreaterThanEnd(period));
    }

    @Test
    public void isDateValid() {
        assertEquals(true, Comparisons.isDateValid(LocalDate.now().toString()));
        assertEquals(false, Comparisons.isDateValid("not date"));
    }

    @Test
    public void isStartNegative() {
        NumericRange range = new NumericRange();
        range.value = -3;
        assertEquals(true, Comparisons.isStartNegative(range));

        range.value = 3;
        assertEquals(false, Comparisons.isStartNegative(range));
    }

    @Test
    public void compareConceptSet() {
        ConceptSet conceptSet = new ConceptSet();
        conceptSet.expression = ConceptSetExpression.fromJson(CONCEPT_SET_DUP_ITEMS);
        Predicate predicate = Comparisons.compare(conceptSet);
        // compare to itself
        assertEquals(true, predicate.test(conceptSet));

        ConceptSet newConceptSet = new ConceptSet();
        newConceptSet.expression = ConceptSetExpression.fromJson(CONCEPT_SET_DUP_ITEMS);
        assertEquals(true, predicate.test(newConceptSet));

    }

    @Test
    public void compareConcept() {
        Concept concept1 = new Concept();
        concept1.conceptCode = "code";
        concept1.domainId = "domain";
        concept1.vocabularyId = "vocabulary";

        Predicate<Concept> predicate = Comparisons.compare(concept1);

        Concept concept2 = new Concept();
        concept2.conceptCode = "code";
        concept2.domainId = "domain";
        concept2.vocabularyId = "vocabulary";

        assertEquals(true, predicate.test(concept2));

        concept2.conceptCode = "code2";
        concept2.domainId = "domain2";
        concept2.vocabularyId = "vocabulary2";

        assertEquals(false, predicate.test(concept2));
    }

    @Test
    public void compareTo() {
        ObservationFilter filter = new ObservationFilter();
        filter.postDays = 10;
        filter.priorDays = 5;

        Window window = new Window();
        window.start = new Window.Endpoint();
        window.start.days = 10;
        window.start.coeff = -1;

        window.end = new Window.Endpoint();
        window.end.days = 5;
        window.end.coeff = 1;

        assertEquals(0, Comparisons.compareTo(filter, window));

        window.start.days = 5;
        window.end.days = 5;

        assertEquals(5, Comparisons.compareTo(filter, window));
    }
    @Test
    public void compareToHours(){
      ObservationFilter filter = new ObservationFilter();
      filter.postDays = 10;
      filter.priorDays = 5;
      Window window = new Window();
      window.start = new Window.Endpoint();
      window.end = new Window.Endpoint();
      window.start.timeUnit = "hour";
      window.start.timeUnitValue = 72;
      window.start.coeff = -1;

      window.end.timeUnit = "hour";
      window.end.timeUnitValue = 120;
      window.end.coeff = 1;
      assertEquals(604800, Comparisons.compareTo(filter, window));
    }
  @Test
  public void compareToMinute() {
    ObservationFilter filter = new ObservationFilter();
    filter.postDays = 10;
    filter.priorDays = 5;
    Window window = new Window();
    window.start = new Window.Endpoint();
    window.end = new Window.Endpoint();
    window.start.timeUnit = "minute";
    window.start.timeUnitValue = 30;
    window.start.coeff = -1;

    window.end.timeUnit = "minute";
    window.end.timeUnitValue = 75;
    window.end.coeff = 1;
    assertEquals(1289700, Comparisons.compareTo(filter, window));
    }
  @Test
  public void compareToSecond() {
    ObservationFilter filter = new ObservationFilter();
    filter.postDays = 10;
    filter.priorDays = 5;
    Window window = new Window();
    window.start = new Window.Endpoint();
    window.end = new Window.Endpoint();
    window.start.timeUnit = "second";
    window.start.timeUnitValue = 30;
    window.start.coeff = -1;

    window.end.timeUnit = "second";
    window.end.timeUnitValue = 75;
    window.end.coeff = 1;
    assertEquals(1295895, Comparisons.compareTo(filter, window));
  }
    @Test
    public void compareCriteria() {
        ConditionEra conditionEra1 = new ConditionEra();
        ConditionEra conditionEra2 = new ConditionEra();
        conditionEra1.codesetId = 1;
        conditionEra2.codesetId = 1;
        assertEquals(true, Comparisons.compare(conditionEra1, conditionEra2));
        conditionEra2.codesetId = 2;
        assertEquals(false, Comparisons.compare(conditionEra1, conditionEra2));

        ConditionOccurrence conditionOccurrence1 = new ConditionOccurrence();
        ConditionOccurrence conditionOccurrence2 = new ConditionOccurrence();
        conditionOccurrence1.codesetId = 1;
        conditionOccurrence2.codesetId = 1;
        assertEquals(true, Comparisons.compare(conditionOccurrence1, conditionOccurrence2));
        conditionOccurrence2.codesetId = 2;
        assertEquals(false, Comparisons.compare(conditionOccurrence1, conditionOccurrence2));

        Death death1 = new Death();
        Death death2 = new Death();
        death1.codesetId = 1;
        death2.codesetId = 1;
        assertEquals(true, Comparisons.compare(death1, death2));
        death2.codesetId = 2;
        assertEquals(false, Comparisons.compare(death1, death2));

        DeviceExposure deviceExposure1 = new DeviceExposure();
        DeviceExposure deviceExposure2 = new DeviceExposure();
        deviceExposure1.codesetId = 1;
        deviceExposure2.codesetId = 1;
        assertEquals(true, Comparisons.compare(deviceExposure1, deviceExposure2));
        deviceExposure2.codesetId = 2;
        assertEquals(false, Comparisons.compare(deviceExposure1, deviceExposure2));

        DoseEra doseEra1 = new DoseEra();
        DoseEra doseEra2 = new DoseEra();
        doseEra1.codesetId = 1;
        doseEra2.codesetId = 1;
        assertEquals(true, Comparisons.compare(doseEra1, doseEra2));
        doseEra2.codesetId = 2;
        assertEquals(false, Comparisons.compare(doseEra1, doseEra2));

        DrugEra drugEra1 = new DrugEra();
        DrugEra drugEra2 = new DrugEra();
        drugEra1.codesetId = 1;
        drugEra2.codesetId = 1;
        assertEquals(true, Comparisons.compare(drugEra1, drugEra2));
        drugEra2.codesetId = 2;
        assertEquals(false, Comparisons.compare(drugEra1, drugEra2));

        DrugExposure drugExposure1 = new DrugExposure();
        DrugExposure drugExposure2 = new DrugExposure();
        drugExposure1.codesetId = 1;
        drugExposure2.codesetId = 1;
        assertEquals(true, Comparisons.compare(drugExposure1, drugExposure2));
        drugExposure2.codesetId = 2;
        assertEquals(false, Comparisons.compare(drugExposure1, drugExposure2));

        Measurement measurement1 = new Measurement();
        Measurement measurement2 = new Measurement();
        measurement1.codesetId = 1;
        measurement2.codesetId = 1;
        assertEquals(true, Comparisons.compare(measurement1, measurement2));
        measurement2.codesetId = 2;
        assertEquals(false, Comparisons.compare(measurement1, measurement2));

        Observation observation1 = new Observation();
        Observation observation2 = new Observation();
        observation1.codesetId = 1;
        observation2.codesetId = 1;
        assertEquals(true, Comparisons.compare(observation1, observation2));
        observation2.codesetId = 2;
        assertEquals(false, Comparisons.compare(observation1, observation2));

        ProcedureOccurrence procedureOccurrence1 = new ProcedureOccurrence();
        ProcedureOccurrence procedureOccurrence2 = new ProcedureOccurrence();
        procedureOccurrence1.codesetId = 1;
        procedureOccurrence2.codesetId = 1;
        assertEquals(true, Comparisons.compare(procedureOccurrence1, procedureOccurrence2));
        procedureOccurrence2.codesetId = 2;
        assertEquals(false, Comparisons.compare(procedureOccurrence1, procedureOccurrence2));

        Specimen specimen1 = new Specimen();
        Specimen specimen2 = new Specimen();
        specimen1.codesetId = 1;
        specimen2.codesetId = 1;
        assertEquals(true, Comparisons.compare(specimen1, specimen2));
        specimen2.codesetId = 2;
        assertEquals(false, Comparisons.compare(specimen1, specimen2));

        VisitOccurrence visitOccurrence1 = new VisitOccurrence();
        VisitOccurrence visitOccurrence2 = new VisitOccurrence();
        visitOccurrence1.codesetId = 1;
        visitOccurrence2.codesetId = 1;
        assertEquals(true, Comparisons.compare(visitOccurrence1, visitOccurrence2));
        visitOccurrence2.codesetId = 2;
        assertEquals(false, Comparisons.compare(visitOccurrence1, visitOccurrence2));

        VisitDetail visitDetail1 = new VisitDetail();
        VisitDetail visitDetail2 = new VisitDetail();
        visitDetail1.codesetId = 1;
        visitDetail2.codesetId = 1;
        assertEquals(true, Comparisons.compare(visitDetail1, visitDetail2));
        visitDetail2.codesetId = 2;
        assertEquals(false, Comparisons.compare(visitDetail1, visitDetail2));
    }

    @Test
    public void isBefore() {
        Window window = new Window();
        window.start = new Window.Endpoint();
        window.start.days = 1;
        window.start.coeff = -1;
        window.end = new Window.Endpoint();
        window.end.days = 1;
        window.end.coeff = 1;
        assertEquals(false, Comparisons.isBefore(window));

        window.end.coeff = -1;
        assertEquals(true, Comparisons.isBefore(window));
    }
}