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

import static org.ohdsi.circe.check.WarningSeverity.WARNING;
import static org.ohdsi.circe.check.operations.Operations.match;
import static org.ohdsi.circe.cohortdefinition.DateOffsetStrategy.DateField.StartDate;

import java.util.List;
import java.util.Objects;
import org.ohdsi.circe.check.Warning;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CustomEraStrategy;
import org.ohdsi.circe.cohortdefinition.DateOffsetStrategy;

public class ExitCriteriaCheck extends BaseCheck {

    private static final String DRUG_CONCEPT_EMPTY_ERROR = "Drug concept set must be selected at Exit Criteria.";

    @Override
    protected void check(CohortExpression expression, WarningReporter reporter) {

        match(expression.endStrategy)
                .isA(CustomEraStrategy.class)
                .then(s -> match((CustomEraStrategy)s)
                        .when(customEraStrategy -> Objects.isNull(customEraStrategy.drugCodesetId))
                        .then(() -> reporter.add(DRUG_CONCEPT_EMPTY_ERROR)));
    }
}
