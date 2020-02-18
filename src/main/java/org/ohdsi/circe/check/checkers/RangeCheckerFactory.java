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
 *   Authors: Vitaly Koulakov, Sergey Suvorov
 *
 */

package org.ohdsi.circe.check.checkers;

import org.ohdsi.circe.check.Constants;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.ConditionEra;
import org.ohdsi.circe.cohortdefinition.ConditionOccurrence;
import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.DateRange;
import org.ohdsi.circe.cohortdefinition.Death;
import org.ohdsi.circe.cohortdefinition.DemographicCriteria;
import org.ohdsi.circe.cohortdefinition.DeviceExposure;
import org.ohdsi.circe.cohortdefinition.DoseEra;
import org.ohdsi.circe.cohortdefinition.DrugEra;
import org.ohdsi.circe.cohortdefinition.DrugExposure;
import org.ohdsi.circe.cohortdefinition.LocationRegion;
import org.ohdsi.circe.cohortdefinition.Measurement;
import org.ohdsi.circe.cohortdefinition.NumericRange;
import org.ohdsi.circe.cohortdefinition.Observation;
import org.ohdsi.circe.cohortdefinition.ObservationPeriod;
import org.ohdsi.circe.cohortdefinition.PayerPlanPeriod;
import org.ohdsi.circe.cohortdefinition.Period;
import org.ohdsi.circe.cohortdefinition.ProcedureOccurrence;
import org.ohdsi.circe.cohortdefinition.Specimen;
import org.ohdsi.circe.cohortdefinition.VisitOccurrence;

import java.util.Objects;
import java.util.function.Consumer;

import static org.ohdsi.circe.check.checkers.Comparisons.isDateValid;
import static org.ohdsi.circe.check.operations.Operations.match;

public class RangeCheckerFactory extends BaseCheckerFactory {

    private static final String WARNING_EMPTY_START_VALUE = "%s in the %s has empty %s start value";
    private static final String WARNING_EMPTY_END_VALUE = "%s in the %s has empty %s end value";
    private static final String WARNING_START_GREATER_THAN_END = "%s in the %s has start value greater than end in %s";
    private static final String WARNING_START_IS_NEGATIVE = "%s in the %s start value is negative at %s";
    private static final String WARNING_DATE_IS_INVALID = "%s in the %s has invalid date value at %s";
    private static final String ROOT_OBJECT = "root object";

    private RangeCheckerFactory(WarningReporter reporter, String groupName) {
        super(reporter, groupName);
    }

    public static RangeCheckerFactory getFactory(WarningReporter reporter, String groupName) {
        return new RangeCheckerFactory(reporter, groupName);
    }

    @Override
    protected Consumer<Criteria> getCheck(Criteria criteria) {

        Consumer<Criteria> result = c -> { };
        if (criteria instanceof ConditionEra) {
            result = c -> {
                ConditionEra conditionEra = (ConditionEra) c;
                checkRange(conditionEra.ageAtStart, Constants.Criteria.CONDITION_ERA, Constants.Attributes.AGE_AT_ERA_START_ATTR);
                checkRange(conditionEra.ageAtEnd, Constants.Criteria.CONDITION_ERA, Constants.Attributes.AGE_AT_ERA_END_ATTR);
                checkRange(conditionEra.eraLength, Constants.Criteria.CONDITION_ERA, Constants.Attributes.ERA_LENGTH_ATTR);
                checkRange(conditionEra.occurrenceCount, Constants.Criteria.CONDITION_ERA, Constants.Attributes.OCCURRENCE_COUNT_ATTR);
                checkRange(conditionEra.eraStartDate, Constants.Criteria.CONDITION_ERA, Constants.Attributes.ERA_START_DATE_ATTR);
                checkRange(conditionEra.eraEndDate, Constants.Criteria.CONDITION_ERA, Constants.Attributes.ERA_END_DATE_ATTR);
            };
        } else if (criteria instanceof ConditionOccurrence) {
            result = c -> {
                ConditionOccurrence co = (ConditionOccurrence) c;
                checkRange(co.occurrenceStartDate, Constants.Criteria.CONDITION_OCCURRENCE, Constants.Attributes.OCCURRENCE_START_DATE_ATTR);
                checkRange(co.occurrenceEndDate, Constants.Criteria.CONDITION_OCCURRENCE, Constants.Attributes.OCCURRENCE_END_DATE_ATTR);
                checkRange(co.age, Constants.Criteria.CONDITION_OCCURRENCE, Constants.Attributes.AGE_ATTR);
            };
        } else if (criteria instanceof Death) {
            result = c -> {
                Death death = (Death) c;
                checkRange(death.age, Constants.Criteria.DEATH, Constants.Attributes.AGE_ATTR);
                checkRange(death.occurrenceStartDate, Constants.Criteria.DEATH, Constants.Attributes.OCCURRENCE_START_DATE_ATTR);
            };
        } else if (criteria instanceof DeviceExposure) {
            result = c -> {
                DeviceExposure de = (DeviceExposure) c;
                checkRange(de.occurrenceStartDate, Constants.Criteria.DEVICE_EXPOSURE, Constants.Attributes.OCCURRENCE_START_DATE_ATTR);
                checkRange(de.occurrenceEndDate, Constants.Criteria.DEVICE_EXPOSURE, Constants.Attributes.OCCURRENCE_END_DATE_ATTR);
                checkRange(de.quantity, Constants.Criteria.DEVICE_EXPOSURE, Constants.Attributes.QUANTITY_ATTR);
                checkRange(de.age, Constants.Criteria.DEVICE_EXPOSURE, Constants.Attributes.AGE_ATTR);
            };
        } else if (criteria instanceof DoseEra) {
            result = c -> {
                DoseEra doseEra = (DoseEra) c;
                checkRange(doseEra.eraStartDate, Constants.Criteria.DOSE_ERA, Constants.Attributes.ERA_START_DATE_ATTR);
                checkRange(doseEra.eraEndDate, Constants.Criteria.DOSE_ERA, Constants.Attributes.ERA_END_DATE_ATTR);
                checkRange(doseEra.doseValue, Constants.Criteria.DOSE_ERA, Constants.Attributes.DOSE_VALUE_ATTR);
                checkRange(doseEra.eraLength, Constants.Criteria.DOSE_ERA, Constants.Attributes.ERA_LENGTH_ATTR);
                checkRange(doseEra.ageAtStart, Constants.Criteria.DOSE_ERA, Constants.Attributes.AGE_AT_START_ATTR);
                checkRange(doseEra.ageAtEnd, Constants.Criteria.DOSE_ERA, Constants.Attributes.AGE_AT_END_ATTR);
            };
        } else if (criteria instanceof DrugEra) {
            result = c -> {
                DrugEra drugEra = (DrugEra) c;
                checkRange(drugEra.eraStartDate, Constants.Criteria.DRUG_ERA, Constants.Attributes.ERA_START_DATE_ATTR);
                checkRange(drugEra.eraEndDate, Constants.Criteria.DRUG_ERA, Constants.Attributes.ERA_END_DATE_ATTR);
                checkRange(drugEra.occurrenceCount, Constants.Criteria.DRUG_ERA, Constants.Attributes.OCCURRENCE_COUNT_ATTR);
                checkRange(drugEra.gapDays, Constants.Criteria.DRUG_ERA, Constants.Attributes.GAP_DAYS_ATTR);
                checkRange(drugEra.eraLength, Constants.Criteria.DRUG_ERA, Constants.Attributes.ERA_LENGTH_ATTR);
                checkRange(drugEra.ageAtStart, Constants.Criteria.DRUG_ERA, Constants.Attributes.AGE_AT_START_ATTR);
                checkRange(drugEra.ageAtEnd, Constants.Criteria.DRUG_ERA, Constants.Attributes.AGE_AT_END_ATTR);
            };
        } else if (criteria instanceof DrugExposure) {
            result = c -> {
                DrugExposure de = (DrugExposure) c;
                checkRange(de.occurrenceStartDate, Constants.Criteria.DRUG_EXPOSURE, Constants.Attributes.OCCURRENCE_START_DATE_ATTR);
                checkRange(de.occurrenceEndDate, Constants.Criteria.DRUG_EXPOSURE, Constants.Attributes.OCCURRENCE_END_DATE_ATTR);
                checkRange(de.refills, Constants.Criteria.DRUG_EXPOSURE, Constants.Attributes.REFILLS_ATTR);
                checkRange(de.quantity, Constants.Criteria.DRUG_EXPOSURE, Constants.Attributes.QUANTITY_ATTR);
                checkRange(de.daysSupply, Constants.Criteria.DRUG_EXPOSURE, Constants.Attributes.DAYS_SUPPLY_ATTR);
                checkRange(de.effectiveDrugDose, Constants.Criteria.DRUG_EXPOSURE, Constants.Attributes.EFFECTIVE_DRUG_DOSE_ATTR);
                checkRange(de.age, Constants.Criteria.DRUG_EXPOSURE, Constants.Attributes.AGE_ATTR);
            };
        } else if (criteria instanceof Measurement) {
            result = c -> {
                Measurement m = (Measurement) c;
                checkRange(m.occurrenceStartDate, Constants.Criteria.MEASUREMENT, Constants.Attributes.OCCURRENCE_START_DATE_ATTR);
                checkRange(m.valueAsNumber, Constants.Criteria.MEASUREMENT, Constants.Attributes.VALUE_AS_NUMBER_ATTR);
                checkRange(m.rangeLow, Constants.Criteria.MEASUREMENT, Constants.Attributes.RANGE_LOW_ATTR);
                checkRange(m.rangeHigh, Constants.Criteria.MEASUREMENT, Constants.Attributes.RANGE_HIGH_ATTR);
                checkRange(m.rangeLowRatio, Constants.Criteria.MEASUREMENT, Constants.Attributes.RANGE_LOW_RATIO_ATTR);
                checkRange(m.rangeHighRatio, Constants.Criteria.MEASUREMENT, Constants.Attributes.RANGE_HIGH_RATIO_ATTR);
                checkRange(m.age, Constants.Criteria.MEASUREMENT, Constants.Attributes.AGE_ATTR);
            };
        } else if (criteria instanceof Observation) {
            result = c -> {
                Observation o = (Observation) c;
                checkRange(o.occurrenceStartDate, Constants.Criteria.OBSERVATION, Constants.Attributes.OCCURRENCE_START_DATE_ATTR);
                checkRange(o.valueAsNumber, Constants.Criteria.OBSERVATION, Constants.Attributes.VALUE_AS_NUMBER_ATTR);
                checkRange(o.age, Constants.Criteria.OBSERVATION, Constants.Attributes.AGE_ATTR);
            };
        } else if (criteria instanceof ObservationPeriod) {
            result = c -> {
                ObservationPeriod op = (ObservationPeriod) c;
                checkRange(op.periodStartDate, Constants.Criteria.OBSERVATION_PERIOD, Constants.Attributes.PERIOD_START_DATE_ATTR);
                checkRange(op.periodEndDate, Constants.Criteria.OBSERVATION_PERIOD, Constants.Attributes.PERIOD_END_DATE_ATTR);
                checkRange(op.periodLength, Constants.Criteria.OBSERVATION_PERIOD, Constants.Attributes.PERIOD_LENGTH_ATTR);
                checkRange(op.ageAtStart, Constants.Criteria.OBSERVATION_PERIOD, Constants.Attributes.AGE_AT_START_ATTR);
                checkRange(op.ageAtEnd, Constants.Criteria.OBSERVATION_PERIOD, Constants.Attributes.AGE_AT_END_ATTR);
                checkRange(op.userDefinedPeriod, Constants.Criteria.OBSERVATION_PERIOD, Constants.Attributes.USER_DEFINED_PERIOD_ATTR);
            };
        } else if (criteria instanceof ProcedureOccurrence) {
            result = c -> {
                ProcedureOccurrence po = (ProcedureOccurrence) c;
                checkRange(po.occurrenceStartDate, Constants.Criteria.PROCEDURE_OCCURRENCE, Constants.Attributes.OCCURRENCE_START_DATE_ATTR);
                checkRange(po.quantity, Constants.Criteria.PROCEDURE_OCCURRENCE, Constants.Attributes.QUANTITY_ATTR);
                checkRange(po.age, Constants.Criteria.PROCEDURE_OCCURRENCE, Constants.Attributes.AGE_ATTR);
            };
        } else if (criteria instanceof Specimen) {
            result = c -> {
                Specimen specimen = (Specimen) c;
                checkRange(specimen.occurrenceStartDate, Constants.Criteria.SPECIMEN, Constants.Attributes.OCCURRENCE_START_DATE_ATTR);
                checkRange(specimen.quantity, Constants.Criteria.SPECIMEN, Constants.Attributes.QUANTITY_ATTR);
                checkRange(specimen.age, Constants.Criteria.SPECIMEN, Constants.Attributes.AGE_ATTR);
            };
        } else if (criteria instanceof VisitOccurrence) {
            result = c -> {
                VisitOccurrence vo = (VisitOccurrence) c;
                checkRange(vo.occurrenceStartDate, Constants.Criteria.VISIT_OCCURRENCE, Constants.Attributes.OCCURRENCE_START_DATE_ATTR);
                checkRange(vo.occurrenceEndDate, Constants.Criteria.VISIT_OCCURRENCE, Constants.Attributes.OCCURRENCE_END_DATE_ATTR);
                checkRange(vo.visitLength, Constants.Criteria.VISIT_OCCURRENCE, Constants.Attributes.VISIT_LENGTH_ATTR);
                checkRange(vo.age, Constants.Criteria.VISIT_OCCURRENCE, Constants.Attributes.AGE_ATTR);
            };
        } else if (criteria instanceof PayerPlanPeriod) {
            result = c -> {
                PayerPlanPeriod planPeriod = (PayerPlanPeriod) c;
                checkRange(planPeriod.periodStartDate, Constants.Criteria.PAYER_PLAN_PERIOD, Constants.Attributes.PERIOD_START_DATE_ATTR);
                checkRange(planPeriod.periodEndDate, Constants.Criteria.PAYER_PLAN_PERIOD, Constants.Attributes.PERIOD_END_DATE_ATTR);
                checkRange(planPeriod.periodLength, Constants.Criteria.PAYER_PLAN_PERIOD, Constants.Attributes.PERIOD_LENGTH_ATTR);
                checkRange(planPeriod.ageAtStart, Constants.Criteria.PAYER_PLAN_PERIOD, Constants.Attributes.AGE_AT_START_ATTR);
                checkRange(planPeriod.ageAtEnd, Constants.Criteria.PAYER_PLAN_PERIOD, Constants.Attributes.AGE_AT_END_ATTR);
                checkRange(planPeriod.userDefinedPeriod, Constants.Criteria.PAYER_PLAN_PERIOD, Constants.Attributes.USER_DEFINED_PERIOD_ATTR);
            };
        } else if (criteria instanceof LocationRegion) {
            result = c -> {
                LocationRegion region = (LocationRegion) c;
                checkRange(region.endDate, Constants.Criteria.LOCATION_REGION, Constants.Attributes.LOCATION_REGION_START_DATE_ATTR);
                checkRange(region.startDate, Constants.Criteria.LOCATION_REGION, Constants.Attributes.LOCATION_REGION_END_DATE_ATTR);
            };
        }
        return result;
    }

    @Override
    protected  Consumer<DemographicCriteria> getCheck(DemographicCriteria criteria) {
        Consumer<DemographicCriteria> result = c -> {
            checkRange(criteria.occurrenceEndDate, Constants.Criteria.DEMOGRAPHIC, Constants.Attributes.OCCURRENCE_END_DATE_ATTR);
            checkRange(criteria.occurrenceStartDate, Constants.Criteria.DEMOGRAPHIC, Constants.Attributes.OCCURRENCE_START_DATE_ATTR);
            checkRange(criteria.age, Constants.Criteria.DEMOGRAPHIC, Constants.Attributes.AGE_ATTR);
        };
        return result;
    }

    private void checkRange(NumericRange range, String criteriaName, String attribute) {

        Consumer<String> warning = (t) -> reporter.add(t, groupName, criteriaName, attribute);
        match(range)
                .when(r -> Objects.nonNull(r.op) && r.op.endsWith("bt"))
                .then(r -> {
                    match(r)
                            .when(x -> Objects.isNull(x.value))
                            .then(x -> warning.accept(WARNING_EMPTY_START_VALUE))
                            .when(x -> Objects.isNull(x.extent))
                            .then(x -> warning.accept(WARNING_EMPTY_END_VALUE))
                            .when(Comparisons::startIsGreaterThanEnd)
                            .then(x -> warning.accept(WARNING_START_GREATER_THAN_END));
                })
                .orElse( r -> match(r)
                        .when(Comparisons::isStartNegative)
                        .then(() -> warning.accept(WARNING_START_IS_NEGATIVE))
                        .when(x -> Objects.isNull(x.value))
                        .then(() -> warning.accept(WARNING_EMPTY_START_VALUE)));
    }

    private void checkRange(DateRange range, String criteriaName, String attribute) {

        Consumer<String> warning = (t) -> reporter.add(t, groupName, criteriaName, attribute);
        match(range)
                .when(r -> Objects.nonNull(r.value) && !isDateValid(r.value))
                .then(x -> warning.accept(WARNING_DATE_IS_INVALID))
                .when(r -> Objects.nonNull(r.op) && r.op.endsWith("bt"))
                .then(r -> {
                    match(r)
                            .when(x -> Objects.isNull(x.value))
                            .then(() -> warning.accept(WARNING_EMPTY_START_VALUE))
                            .when(x -> Objects.isNull(x.extent))
                            .then(() -> warning.accept(WARNING_EMPTY_END_VALUE))
                            .when(x -> Objects.nonNull(x.extent) && !isDateValid(x.extent))
                            .then(() -> warning.accept(WARNING_DATE_IS_INVALID))
                            .when(Comparisons::startIsGreaterThanEnd)
                            .then(() -> warning.accept(WARNING_START_GREATER_THAN_END));
                }).orElse(r ->
                    match(r).when(x -> Objects.isNull(x.value))
                    .then(() -> warning.accept(WARNING_EMPTY_START_VALUE))
                );
    }

    public void checkRange(Period period, String criteriaName, String attribute) {

        Consumer<String> warning = (t) -> reporter.add(t, groupName, criteriaName, attribute);
        match(period)
                .when(x -> Objects.nonNull(x.startDate) && !isDateValid(x.startDate))
                .then(x -> warning.accept(WARNING_DATE_IS_INVALID))
                .when(x -> Objects.nonNull(x.endDate) && !isDateValid(x.endDate))
                .then(x -> warning.accept(WARNING_DATE_IS_INVALID))
                .when(Comparisons::startIsGreaterThanEnd)
                .then(x -> warning.accept(WARNING_START_GREATER_THAN_END));
    }

    public void check(CohortExpression expression) {

        checkRange(expression.censorWindow, ROOT_OBJECT, Constants.Attributes.CENSOR_WINDOW_ATTR);
    }
}
