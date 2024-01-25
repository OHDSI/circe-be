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
import static org.ohdsi.circe.cohortdefinition.DateOffsetStrategy.DateField.StartDate;

import java.util.Objects;
import org.ohdsi.circe.check.WarningSeverity;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.DateOffsetStrategy;

public class ExitCriteriaDaysOffsetCheck extends BaseCheck {

    private static final String DAYS_OFFSET_WARNING = "Cohort Exit criteria: %ss offset from start date should be greater than 0";

    @Override
    protected WarningSeverity defineSeverity() {

        return WarningSeverity.WARNING;
    }

    @Override
    protected void check(CohortExpression expression, WarningReporter reporter) {

        match(expression.endStrategy)
            .isA(DateOffsetStrategy.class)
            .then(s -> match((DateOffsetStrategy)s)
                .when(dateOffsetStrategy -> Objects.equals(StartDate, dateOffsetStrategy.dateField) &&  0 == dateOffsetStrategy.offsetUnitValue)
                .then(dateOffsetStrategy -> reporter.add(String.format(DAYS_OFFSET_WARNING, dateOffsetStrategy.offsetUnit))));
    }
}
