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
import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.Measurement;

public class MeasurementCheck extends BaseCriteriaCheck {

    private static final String EMPTY_VALUE_ERROR = "Measurement of %s contains empty %s";

    @Override
    protected void checkCriteria(Criteria criteria, String groupName, WarningReporter reporter) {

        WarningReporterHelper helper = new WarningReporterHelper(reporter, EMPTY_VALUE_ERROR, groupName);
        match(criteria)
                .isA(Measurement.class)
                .then(c -> match((Measurement)c)
                        .when(measurement -> Objects.nonNull(measurement.operator) && measurement.operator.length == 0)
                        .then(helper.addWarning("operator"))
                        .when(measurement -> Objects.nonNull(measurement.valueAsConcept) && measurement.valueAsConcept.length == 0)
                        .then(helper.addWarning("value as concept"))
                        .when(measurement -> Objects.nonNull(measurement.unit) && measurement.unit.length == 0)
                        .then(helper.addWarning("unit")));
    }
}
