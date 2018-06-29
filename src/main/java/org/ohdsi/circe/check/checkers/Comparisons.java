/*
 *   Copyright 2017 Observational Health Data Sciences and Informatics
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *   Authors: Vitaly Koulakov
 *
 */

package org.ohdsi.circe.check.checkers;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.ohdsi.circe.cohortdefinition.ConceptSet;
import org.ohdsi.circe.cohortdefinition.ConditionEra;
import org.ohdsi.circe.cohortdefinition.ConditionOccurrence;
import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.DateRange;
import org.ohdsi.circe.cohortdefinition.Death;
import org.ohdsi.circe.cohortdefinition.DeviceExposure;
import org.ohdsi.circe.cohortdefinition.DoseEra;
import org.ohdsi.circe.cohortdefinition.DrugEra;
import org.ohdsi.circe.cohortdefinition.DrugExposure;
import org.ohdsi.circe.cohortdefinition.Measurement;
import org.ohdsi.circe.cohortdefinition.NumericRange;
import org.ohdsi.circe.cohortdefinition.Observation;
import org.ohdsi.circe.cohortdefinition.ObservationFilter;
import org.ohdsi.circe.cohortdefinition.Period;
import org.ohdsi.circe.cohortdefinition.ProcedureOccurrence;
import org.ohdsi.circe.cohortdefinition.Specimen;
import org.ohdsi.circe.cohortdefinition.VisitOccurrence;
import org.ohdsi.circe.cohortdefinition.Window;
import org.ohdsi.circe.vocabulary.Concept;

public class Comparisons {

    public static Boolean startIsGreaterThanEnd(NumericRange r) {

        return Objects.nonNull(r.value) && Objects.nonNull(r.extent) && r.value.intValue() > r.extent.intValue();
    }

    public static Boolean startIsGreaterThanEnd(DateRange r) {

        try {
            return Objects.nonNull(r.value) && Objects.nonNull(r.extent) && LocalDate.parse(r.value).isAfter(LocalDate.parse(r.extent));
        }catch (DateTimeParseException ignored) {
            return false;
        }
    }

    public static Boolean startIsGreaterThanEnd(Period p) {

        try{
            if (Objects.nonNull(p.startDate) && Objects.nonNull(p.endDate)) {
                LocalDate startDate = LocalDate.parse(p.startDate);
                LocalDate endDate = LocalDate.parse(p.endDate);
                return startDate.isAfter(endDate);
            }
        }catch (DateTimeParseException ignored) {
        }
        return false;
    }

    public static Boolean isDateValid(String date) {

        try {
            LocalDate.parse(date);
            return true;
        } catch (DateTimeParseException ignored) {
            return false;
        }
    }

    public static Boolean isStartNegative(NumericRange r) {

        return Objects.nonNull(r.value) && r.value.intValue() < 0;
    }

    public static Predicate<ConceptSet> compare(ConceptSet source) {

        return conceptSet -> {
            if (conceptSet.expression == source.expression) {
                return true;
            }
            if (Objects.nonNull(conceptSet.expression) && Objects.nonNull(source.expression)) {
                if (conceptSet.expression.items.length == source.expression.items.length) {
                    List<Concept> sourceConcepts = Arrays.stream(source.expression.items)
                            .map(item -> item.concept)
                            .collect(Collectors.toList());
                    return Arrays.stream(conceptSet.expression.items)
                            .map(item -> item.concept)
                            .allMatch(concept -> sourceConcepts.stream().anyMatch(Comparisons.compare(concept)));
                }
            }
            return false;
        };
    }

    public static Predicate<Concept> compare(Concept source) {

        return concept -> new EqualsBuilder()
                .append(concept.conceptCode, source.conceptCode)
                .append(concept.domainId, source.domainId)
                .append(concept.vocabularyId, source.vocabularyId)
                .build();
    }

    public static int compareTo(ObservationFilter filter, Window window) {

        int result = 0;
        int range1 = filter.postDays + filter.priorDays;
        int range2Start = 0, range2End = 0;
        if (Objects.nonNull(window.start) && Objects.nonNull(window.start.days)) {
            range2Start = window.start.coeff * window.start.days;
        }
        if (Objects.nonNull(window.end) && Objects.nonNull(window.end.days)) {
            range2End = window.end.coeff * window.end.days;
        }
        return range1 - (range2End - range2Start);
    }

    public static boolean compare(Criteria c1, Criteria c2) {

        boolean result = false;
        if (Objects.equals(c1.getClass(), c2.getClass())) {
            if (c1 instanceof ConditionEra) {
                ConditionEra ce1 = (ConditionEra) c1, ce2 = (ConditionEra) c2;
                result = Objects.equals(ce1.codesetId, ce2.codesetId);
            } else if (c1 instanceof ConditionOccurrence) {
                ConditionOccurrence co1 = (ConditionOccurrence) c1, co2 = (ConditionOccurrence) c2;
                result = Objects.equals(co1.codesetId, co2.codesetId);
            } else if (c1 instanceof Death){
                Death d1 = (Death) c1, d2 = (Death) c2;
                result = Objects.equals(d1.codesetId, d2.codesetId);
            } else if (c1 instanceof DeviceExposure) {
                DeviceExposure de1 = (DeviceExposure) c1, de2 = (DeviceExposure) c2;
                result = Objects.equals(de1.codesetId, de2.codesetId);
            } else if (c1 instanceof DoseEra) {
                DoseEra de1 = (DoseEra) c1, de2 = (DoseEra) c2;
                result = Objects.equals(de1.codesetId, de2.codesetId);
            } else if (c1 instanceof DrugEra) {
                DrugEra de1 = (DrugEra) c1, de2 = (DrugEra) c2;
                result = Objects.equals(de1.codesetId, de2.codesetId);
            } else if (c1 instanceof DrugExposure) {
                DrugExposure de1 = (DrugExposure) c1, de2 = (DrugExposure) c2;
                result = Objects.equals(de1.codesetId, de2.codesetId);
            } else if (c1 instanceof Measurement) {
                Measurement m1 = (Measurement) c1, m2 = (Measurement) c2;
                result = Objects.equals(m1.codesetId, m2.codesetId);
            } else if (c1 instanceof Observation) {
                Observation o1 = (Observation) c1, o2 = (Observation) c2;
                result = Objects.equals(o1.codesetId, o2.codesetId);
            } else if (c1 instanceof ProcedureOccurrence) {
                ProcedureOccurrence po1 = (ProcedureOccurrence) c1, po2 = (ProcedureOccurrence) c2;
                result = Objects.equals(po1.codesetId, po2.codesetId);
            } else if (c1 instanceof Specimen) {
                Specimen s1 = (Specimen) c1, s2 = (Specimen) c2;
                result = Objects.equals(s1.codesetId, s2.codesetId);
            } else if (c1 instanceof VisitOccurrence) {
                VisitOccurrence vo1 = (VisitOccurrence) c1, vo2 = (VisitOccurrence) c2;
                result = Objects.equals(vo1.codesetId, vo2.codesetId);
            }
        }
        return result;
    }
}
