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

import static org.ohdsi.circe.check.operations.Operations.match;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.ohdsi.circe.check.WarningSeverity;
import org.ohdsi.circe.check.operations.Execution;
import org.ohdsi.circe.check.utils.CriteriaNameHelper;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.ConditionOccurrence;
import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.Death;
import org.ohdsi.circe.cohortdefinition.DeviceExposure;
import org.ohdsi.circe.cohortdefinition.DrugExposure;
import org.ohdsi.circe.cohortdefinition.Measurement;
import org.ohdsi.circe.cohortdefinition.Observation;
import org.ohdsi.circe.cohortdefinition.ProcedureOccurrence;
import org.ohdsi.circe.cohortdefinition.Specimen;
import org.ohdsi.circe.cohortdefinition.VisitOccurrence;

public class EmptyDomainTypeCheck extends BaseCriteriaCheck {

    private static final String WARNING = "There are an empty domain types in the following criteria: %s";
    private List<String> warnNames = new ArrayList<>();

    @Override
    protected WarningSeverity defineSeverity() {

        return WarningSeverity.WARNING;
    }

    @Override
    protected void checkCriteria(Criteria criteria, String groupName, WarningReporter reporter) {
        final String name = CriteriaNameHelper.getCriteriaName(criteria);
        final Execution addWarning = () -> warnNames.add(name + " at " + groupName);
        match(criteria)
                .isA(ConditionOccurrence.class)
                .then(c -> match((ConditionOccurrence)c)
                        .when(conditionOccurrence -> Objects.nonNull(conditionOccurrence.conditionType) && conditionOccurrence.conditionType.length == 0)
                        .then(addWarning))
                .isA(Death.class)
                .then(c -> match((Death)c)
                        .when(death -> Objects.nonNull(death.deathType) && death.deathType.length == 0)
                        .then(addWarning))
                .isA(DeviceExposure.class)
                .then(c -> match((DeviceExposure)c)
                        .when(deviceExposure -> Objects.nonNull(deviceExposure.deviceType) && deviceExposure.deviceType.length == 0)
                        .then(addWarning))
                .isA(DrugExposure.class)
                .then(c -> match((DrugExposure)c)
                        .when(drugExposure -> Objects.nonNull(drugExposure.drugType) && drugExposure.drugType.length == 0)
                        .then(addWarning))
                .isA(Measurement.class)
                .then(c -> match((Measurement)c)
                        .when(measurement -> Objects.nonNull(measurement.measurementType) && measurement.measurementType.length == 0)
                        .then(addWarning))
                .isA(Observation.class)
                .then(c -> match((Observation)c)
                        .when(observation -> Objects.nonNull(observation.observationType) && observation.observationType.length == 0)
                        .then(addWarning))
                .isA(ProcedureOccurrence.class)
                .then(c -> match((ProcedureOccurrence)c)
                        .when(procedureOccurrence -> Objects.nonNull(procedureOccurrence.procedureType) && procedureOccurrence.procedureType.length == 0)
                        .then(addWarning))
                .isA(Specimen.class)
                .then(c -> match((Specimen)c)
                        .when(specimen -> Objects.nonNull(specimen.specimenType) && specimen.specimenType.length == 0)
                        .then(addWarning))
                .isA(VisitOccurrence.class)
                .then(c -> match((VisitOccurrence)c)
                        .when(visitOccurrence -> Objects.nonNull(visitOccurrence.visitType) && visitOccurrence.visitType.length == 0)
                        .then(addWarning));
    }

    @Override
    protected void afterCheck(WarningReporter reporter, CohortExpression expression) {

        if (!warnNames.isEmpty()) {
            reporter.add(WARNING, warnNames.stream().collect(Collectors.joining(", ")));
        }
    }
}
