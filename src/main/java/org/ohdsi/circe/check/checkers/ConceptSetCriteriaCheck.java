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

import java.util.List;
import java.util.Objects;
import org.ohdsi.circe.check.Warning;
import org.ohdsi.circe.check.WarningSeverity;
import org.ohdsi.circe.check.operations.Execution;
import org.ohdsi.circe.check.utils.CriteriaNameHelper;
import org.ohdsi.circe.cohortdefinition.ConditionEra;
import org.ohdsi.circe.cohortdefinition.ConditionOccurrence;
import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.Death;
import org.ohdsi.circe.cohortdefinition.DeviceExposure;
import org.ohdsi.circe.cohortdefinition.DoseEra;
import org.ohdsi.circe.cohortdefinition.DrugEra;
import org.ohdsi.circe.cohortdefinition.DrugExposure;
import org.ohdsi.circe.cohortdefinition.Measurement;
import org.ohdsi.circe.cohortdefinition.Observation;
import org.ohdsi.circe.cohortdefinition.ProcedureOccurrence;
import org.ohdsi.circe.cohortdefinition.Specimen;
import org.ohdsi.circe.cohortdefinition.VisitOccurrence;

public class ConceptSetCriteriaCheck extends BaseCriteriaCheck {

    private static final String NO_CONCEPT_SET_ERROR = "No concept set specified as part of a criteria at %s in %s criteria";

    @Override
    protected WarningSeverity defineSeverity() {

        return WarningSeverity.WARNING;
    }

    @Override
    protected void checkCriteria(Criteria criteria, String groupName, WarningReporter reporter) {

        WarningReporterHelper helper = new WarningReporterHelper(reporter, NO_CONCEPT_SET_ERROR, groupName);
        final String criteriaName = CriteriaNameHelper.getCriteriaName(criteria);
        final Execution addWarning = helper.addWarning(criteriaName);
        match(criteria)
                .isA(ConditionEra.class)
                .then(c -> match(((ConditionEra)c))
                        .when(conditionEra -> Objects.isNull(conditionEra.codesetId))
                        .then(addWarning))
                .isA(ConditionOccurrence.class)
                .then(c -> match(((ConditionOccurrence)c))
                        .when(conditionOccurrence -> Objects.isNull(conditionOccurrence.codesetId))
                        .then(addWarning))
                .isA(Death.class)
                .then(c -> match((Death)c)
                        .when(death -> Objects.isNull(death.codesetId))
                        .then(addWarning))
                .isA(DeviceExposure.class)
                .then(c -> match((DeviceExposure)c)
                        .when(deviceExposure -> Objects.isNull(deviceExposure.codesetId))
                        .then(addWarning))
                .isA(DoseEra.class)
                .then(c -> match((DoseEra)c)
                        .when(doseEra -> Objects.isNull(doseEra.codesetId))
                        .then(addWarning))
                .isA(DrugEra.class)
                .then(c -> match((DrugEra)c)
                        .when(drugEra -> Objects.isNull(drugEra.codesetId))
                        .then(addWarning))
                .isA(DrugExposure.class)
                .then(c -> match(((DrugExposure)c)).when(drugExposure -> Objects
                        .isNull(drugExposure.codesetId))
                        .then(addWarning))
                .isA(Measurement.class)
                .then(c -> match((Measurement)c)
                        .when(measurement -> Objects.isNull(measurement.codesetId))
                        .then(addWarning))
                .isA(Observation.class)
                .then(c -> match((Observation)c)
                        .when(observation -> Objects.isNull(observation.codesetId))
                        .then(addWarning))
                .isA(ProcedureOccurrence.class)
                .then(c -> match((ProcedureOccurrence)c)
                        .when(procedureOccurrence -> Objects.isNull(procedureOccurrence.codesetId))
                        .then(addWarning))
                .isA(Specimen.class)
                .then(c -> match((Specimen)c)
                        .when(specimen -> Objects.isNull(specimen.codesetId))
                        .then(addWarning))
                .isA(VisitOccurrence.class)
                .then(c -> match((VisitOccurrence)c)
                        .when(visitOccurrence -> Objects.isNull(visitOccurrence.codesetId))
                        .then(addWarning));
    }
}
