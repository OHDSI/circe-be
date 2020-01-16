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
 *   Authors: Sergey Suvorov
 *
 */

package org.ohdsi.circe.check.checkers;

import org.ohdsi.circe.check.Constants;
import org.ohdsi.circe.cohortdefinition.ConditionEra;
import org.ohdsi.circe.cohortdefinition.ConditionOccurrence;
import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.Death;
import org.ohdsi.circe.cohortdefinition.DemographicCriteria;
import org.ohdsi.circe.cohortdefinition.DeviceExposure;
import org.ohdsi.circe.cohortdefinition.DoseEra;
import org.ohdsi.circe.cohortdefinition.DrugEra;
import org.ohdsi.circe.cohortdefinition.DrugExposure;
import org.ohdsi.circe.cohortdefinition.Measurement;
import org.ohdsi.circe.cohortdefinition.Observation;
import org.ohdsi.circe.cohortdefinition.ObservationPeriod;
import org.ohdsi.circe.cohortdefinition.PayerPlanPeriod;
import org.ohdsi.circe.cohortdefinition.ProcedureOccurrence;
import org.ohdsi.circe.cohortdefinition.Specimen;
import org.ohdsi.circe.cohortdefinition.VisitOccurrence;
import org.ohdsi.circe.vocabulary.Concept;

import java.util.Objects;
import java.util.function.Consumer;

import static org.ohdsi.circe.check.Constants.Attributes.UNIT_ATTR;
import static org.ohdsi.circe.check.operations.Operations.match;

public class ConceptCheckerFactory extends BaseCheckerFactory{
    private static final String WARNING_EMPTY_VALUE = "%s in the %s has empty %s value";

    private ConceptCheckerFactory(WarningReporter reporter, String groupName) {
        super(reporter, groupName);
    }

    public static ConceptCheckerFactory getFactory(WarningReporter reporter, String groupName) {
        return new ConceptCheckerFactory(reporter, groupName);
    }

    @Override
    protected Consumer<Criteria> getCheck(Criteria criteria) {
        Consumer<Criteria> result = c -> { };
        if (criteria instanceof ConditionEra) {
            result = c -> {
                ConditionEra conditionEra = (ConditionEra) c;
                checkConcept(conditionEra.gender, Constants.Criteria.CONDITION_ERA, Constants.Attributes.GENDER_ATTR);
            };
        } else if (criteria instanceof ConditionOccurrence) {
            result = c -> {
                ConditionOccurrence co = (ConditionOccurrence) c;
                checkConcept(co.conditionType, Constants.Criteria.CONDITION_OCCURRENCE, Constants.Attributes.CONDITION_TYPE_ATTR);
                checkConcept(co.gender, Constants.Criteria.CONDITION_OCCURRENCE, Constants.Attributes.GENDER_ATTR);
                checkConcept(co.providerSpecialty, Constants.Criteria.CONDITION_OCCURRENCE, Constants.Attributes.PROVIDER_SPECIALITY_ATTR);
                checkConcept(co.visitType, Constants.Criteria.CONDITION_OCCURRENCE, Constants.Attributes.VISIT_TYPE_ATTR);
            };
        } else if (criteria instanceof Death) {
            result = c -> {
                Death death = (Death) c;
                checkConcept(death.deathType, Constants.Criteria.DEATH, Constants.Attributes.DEATH_TYPE_ATTR);
                checkConcept(death.gender, Constants.Criteria.DEATH, Constants.Attributes.GENDER_ATTR);
            };
        } else if (criteria instanceof DeviceExposure) {
            result = c -> {
                DeviceExposure de = (DeviceExposure) c;
                checkConcept(de.deviceType, Constants.Criteria.DEVICE_EXPOSURE, Constants.Attributes.DEVICE_TYPE_ATTR);
                checkConcept(de.gender, Constants.Criteria.DEVICE_EXPOSURE, Constants.Attributes.GENDER_ATTR);
                checkConcept(de.providerSpecialty, Constants.Criteria.DEVICE_EXPOSURE, Constants.Attributes.PROVIDER_SPECIALITY_ATTR);
                checkConcept(de.visitType, Constants.Criteria.DEVICE_EXPOSURE, Constants.Attributes.VISIT_TYPE_ATTR);
            };
        } else if (criteria instanceof DoseEra) {
            result = c -> {
                DoseEra doseEra = (DoseEra) c;
                checkConcept(doseEra.unit, Constants.Criteria.DOSE_ERA, UNIT_ATTR);
                checkConcept(doseEra.gender, Constants.Criteria.DOSE_ERA, Constants.Attributes.GENDER_ATTR);
            };
        } else if (criteria instanceof DrugEra) {
            result = c -> {
                DrugEra drugEra = (DrugEra) c;
                checkConcept(drugEra.gender, Constants.Criteria.DRUG_ERA, Constants.Attributes.GENDER_ATTR);
            };
        } else if (criteria instanceof DrugExposure) {
            result = c -> {
                DrugExposure de = (DrugExposure) c;
                checkConcept(de.drugType, Constants.Criteria.DRUG_EXPOSURE, Constants.Attributes.DRUG_TYPE_ATTR);
                checkConcept(de.routeConcept, Constants.Criteria.DRUG_EXPOSURE, Constants.Attributes.ROUTE_CONCEPT_ATTR);
                checkConcept(de.doseUnit, Constants.Criteria.DRUG_EXPOSURE, Constants.Attributes.DOSE_UNIT_ATTR);
                checkConcept(de.gender, Constants.Criteria.DRUG_EXPOSURE, Constants.Attributes.GENDER_ATTR);
                checkConcept(de.providerSpecialty, Constants.Criteria.DRUG_EXPOSURE, Constants.Attributes.PROVIDER_SPECIALITY_ATTR);
                checkConcept(de.visitType, Constants.Criteria.DRUG_EXPOSURE, Constants.Attributes.VISIT_TYPE_ATTR);
            };
        } else if (criteria instanceof Measurement) {
            result = c -> {
                Measurement m = (Measurement) c;
                checkConcept(m.measurementType, Constants.Criteria.MEASUREMENT, Constants.Attributes.MEASUREMENT_TYPE_ATTR);
                checkConcept(m.operator, Constants.Criteria.MEASUREMENT, Constants.Attributes.OPERATOR_ATTR);
                checkConcept(m.valueAsConcept, Constants.Criteria.MEASUREMENT, Constants.Attributes.VALUE_AS_CONCEPT_ATTR);
                checkConcept(m.unit, Constants.Criteria.MEASUREMENT, Constants.Attributes.UNIT_ATTR);
                checkConcept(m.gender, Constants.Criteria.MEASUREMENT, Constants.Attributes.GENDER_ATTR);
                checkConcept(m.providerSpecialty, Constants.Criteria.MEASUREMENT, Constants.Attributes.PROVIDER_SPECIALITY_ATTR);
                checkConcept(m.visitType, Constants.Criteria.MEASUREMENT, Constants.Attributes.VISIT_TYPE_ATTR);
            };
        } else if (criteria instanceof Observation) {
            result = c -> {
                Observation o = (Observation) c;
                checkConcept(o.observationType, Constants.Criteria.OBSERVATION, Constants.Attributes.OBSERVATION_TYPE_ATTR);
                checkConcept(o.valueAsConcept, Constants.Criteria.OBSERVATION, Constants.Attributes.VALUE_AS_CONCEPT_ATTR);
                checkConcept(o.qualifier, Constants.Criteria.OBSERVATION, Constants.Attributes.QUALIFIER_ATTR);
                checkConcept(o.unit, Constants.Criteria.OBSERVATION, Constants.Attributes.UNIT_ATTR);
                checkConcept(o.gender, Constants.Criteria.OBSERVATION, Constants.Attributes.GENDER_ATTR);
                checkConcept(o.providerSpecialty, Constants.Criteria.OBSERVATION, Constants.Attributes.PROVIDER_SPECIALITY_ATTR);
                checkConcept(o.visitType, Constants.Criteria.OBSERVATION, Constants.Attributes.VISIT_TYPE_ATTR);
            };
        } else if (criteria instanceof ObservationPeriod) {
            result = c -> {
                ObservationPeriod op = (ObservationPeriod) c;
                checkConcept(op.periodType, Constants.Criteria.OBSERVATION_PERIOD, Constants.Attributes.PERIOD_TYPE_ATTR);
            };
        } else if (criteria instanceof ProcedureOccurrence) {
            result = c -> {
                ProcedureOccurrence po = (ProcedureOccurrence) c;
                checkConcept(po.procedureType, Constants.Criteria.PROCEDURE_OCCURRENCE, Constants.Attributes.PROCEDURE_TYPE_ATTR);
                checkConcept(po.modifier, Constants.Criteria.PROCEDURE_OCCURRENCE, Constants.Attributes.MODIFIER_ATTR);
                checkConcept(po.gender, Constants.Criteria.PROCEDURE_OCCURRENCE, Constants.Attributes.GENDER_ATTR);
                checkConcept(po.providerSpecialty, Constants.Criteria.PROCEDURE_OCCURRENCE, Constants.Attributes.PROVIDER_SPECIALITY_ATTR);
                checkConcept(po.visitType, Constants.Criteria.PROCEDURE_OCCURRENCE, Constants.Attributes.VISIT_TYPE_ATTR);
            };
        } else if (criteria instanceof Specimen) {
            result = c -> {
                Specimen specimen = (Specimen) c;
                checkConcept(specimen.specimenType, Constants.Criteria.SPECIMEN, Constants.Attributes.SPECIMEN_TYPE_ATTR);
                checkConcept(specimen.unit, Constants.Criteria.SPECIMEN, Constants.Attributes.UNIT_ATTR);
                checkConcept(specimen.anatomicSite, Constants.Criteria.SPECIMEN, Constants.Attributes.ANATOMIC_SITE_ATTR);
                checkConcept(specimen.diseaseStatus, Constants.Criteria.SPECIMEN, Constants.Attributes.DISEASE_STATUS_ATTR);
                checkConcept(specimen.gender, Constants.Criteria.SPECIMEN, Constants.Attributes.GENDER_ATTR);
            };
        } else if (criteria instanceof VisitOccurrence) {
            result = c -> {
                VisitOccurrence vo = (VisitOccurrence) c;
                checkConcept(vo.visitType, Constants.Criteria.VISIT_OCCURRENCE, Constants.Attributes.VISIT_TYPE_ATTR);
                checkConcept(vo.gender, Constants.Criteria.VISIT_OCCURRENCE, Constants.Attributes.GENDER_ATTR);
                checkConcept(vo.providerSpecialty, Constants.Criteria.VISIT_OCCURRENCE, Constants.Attributes.PROVIDER_SPECIALITY_ATTR);
                checkConcept(vo.placeOfService, Constants.Criteria.VISIT_OCCURRENCE, Constants.Attributes.PLACE_OF_SERVICE_ATTR);
            };
        } else if (criteria instanceof PayerPlanPeriod) {
            result = c -> {
                PayerPlanPeriod planPeriod = (PayerPlanPeriod) c;
                checkConcept(planPeriod.gender, Constants.Criteria.PAYER_PLAN_PERIOD, Constants.Attributes.GENDER_ATTR);
            };
        }
        return result;
    }

    @Override
    protected Consumer<DemographicCriteria> getCheck(DemographicCriteria criteria) {
        Consumer<DemographicCriteria> result = c -> {
            checkConcept(criteria.ethnicity, Constants.Criteria.DEMOGRAPHIC, Constants.Attributes.ETHNICITY_ATTR);
            checkConcept(criteria.gender, Constants.Criteria.DEMOGRAPHIC, Constants.Attributes.GENDER_ATTR);
            checkConcept(criteria.race, Constants.Criteria.DEMOGRAPHIC, Constants.Attributes.RACE_ATTR);
        };
        return result;
    }

    private void checkConcept(Concept[] concepts, String criteriaName, String attribute) {
        Consumer<String> warning = (t) -> reporter.add(t, groupName, criteriaName, attribute);
        match(concepts)
                .when(r -> Objects.nonNull(r) && r.length == 0)
                .then(() -> warning.accept(WARNING_EMPTY_VALUE));
    }
}
