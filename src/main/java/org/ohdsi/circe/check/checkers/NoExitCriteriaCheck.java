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
import org.ohdsi.circe.cohortdefinition.CohortExpression;

public class NoExitCriteriaCheck extends BaseCheck {

    private static final String NO_EXIT_CRITERIA_WARNING = " \"all events\" are selected and cohort exit criteria has not been specified";

    @Override
    protected void check(CohortExpression expression, WarningReporter reporter) {

        match(expression)
                .when(e -> Objects.equals("All", e.primaryCriteria.primaryLimit.type) && Objects.isNull(e.endStrategy))
                .then(() -> reporter.add(NO_EXIT_CRITERIA_WARNING));
    }
}
