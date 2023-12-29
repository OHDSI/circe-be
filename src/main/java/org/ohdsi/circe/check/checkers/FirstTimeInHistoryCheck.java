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

import java.util.Objects;
import org.ohdsi.circe.check.WarningSeverity;
import org.ohdsi.circe.check.operations.Execution;
import org.ohdsi.circe.check.utils.CriteriaNameHelper;
import org.ohdsi.circe.cohortdefinition.*;

public class FirstTimeInHistoryCheck extends BaseCorelatedCriteriaCheck {

    private static final String WARNING = "%s didn't specify that it must be first time in patient's history";

    @Override
    protected WarningSeverity defineSeverity() {

        return WarningSeverity.INFO;
    }

    @Override
    protected void checkCriteria(CorelatedCriteria criteria, String groupName, WarningReporter reporter) {

        String name = CriteriaNameHelper.getCriteriaName(criteria.criteria) + " at " + groupName;
        Execution addWarning = () -> reporter.add(WARNING, name);
        match(criteria)
                .when(c -> c.startWindow != null && ((c.startWindow.start != null
                        && c.startWindow.start.days  != null) || (c.startWindow.end != null
                        && c.startWindow.end.days  != null))
                        || ((c.startWindow.start.timeUnitValue != null)
                        && (c.startWindow.end != null)
                        && c.startWindow.end.timeUnitValue != null))
                .then(cc -> match(cc.criteria)
                        .isA(ConditionEra.class)
                        .then(c -> match((ConditionEra)c)
                                .when(conditionEra -> Objects.isNull(conditionEra.first))
                                .then(addWarning))
                        .isA(ConditionOccurrence.class)
                        .then(c -> match((ConditionOccurrence)c)
                                .when(conditionOccurrence -> Objects.isNull(conditionOccurrence.first))
                                .then(addWarning))
                        .isA(DeviceExposure.class)
                        .then(c -> match((DeviceExposure)c)
                                .when(deviceExposure -> Objects.isNull(deviceExposure.first))
                                .then(addWarning))
                        .isA(DoseEra.class)
                        .then(c -> match((DoseEra)c)
                                .when(doseEra -> Objects.isNull(doseEra.first))
                                .then(addWarning))
                        .isA(DrugEra.class)
                        .then(c -> match((DrugEra)c)
                                .when(drugEra -> Objects.isNull(drugEra.first))
                                .then(addWarning))
                        .isA(DrugExposure.class)
                        .then(c -> match((DrugExposure)c)
                                .when(drugExposure -> Objects.isNull(drugExposure.first))
                                .then(addWarning))
                        .isA(Measurement.class)
                        .then(c -> match((Measurement)c)
                                .when(measurement -> Objects.isNull(measurement.first))
                                .then(addWarning))
                        .isA(Observation.class)
                        .then(c -> match((Observation)c)
                                .when(observation -> Objects.isNull(observation.first))
                                .then(addWarning))
                        .isA(ObservationPeriod.class)
                        .then(c -> match((ObservationPeriod)c)
                                .when(observationPeriod -> Objects.isNull(observationPeriod.first))
                                .then(addWarning))
                        .isA(ProcedureOccurrence.class)
                        .then(c -> match((ProcedureOccurrence)c)
                                .when(procedureOccurrence -> Objects.isNull(procedureOccurrence.first))
                                .then(addWarning))
                        .isA(Specimen.class)
                        .then(c -> match((Specimen)c)
                                .when(specimen -> Objects.isNull(specimen.first))
                                .then(addWarning))
                        .isA(VisitOccurrence.class)
                        .then(c -> match((VisitOccurrence)c)
                                .when(visitOccurrence -> Objects.isNull(visitOccurrence.first))
                                .then(addWarning))
                        .isA(VisitDetail.class)
                        .then(c -> match((VisitDetail)c)
                                .when(visitDetail -> Objects.isNull(visitDetail.first))
                                .then(addWarning))
                        .isA(PayerPlanPeriod.class)
                        .then(c -> match((PayerPlanPeriod)c)
                                .when(payerPlanPeriod -> Objects.isNull(payerPlanPeriod.first))
                                .then(addWarning))
                );
    }
}
