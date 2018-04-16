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

package org.ohdsi.circe.check;

import java.util.List;
import java.util.Objects;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CustomEraStrategy;

public class ExitCriteriaCheck extends BaseCheck {
    @Override
    protected void check(CohortExpression expression, List<Warning> warnings) {
        if (expression.endStrategy instanceof CustomEraStrategy) {
            CustomEraStrategy eraStrategy = (CustomEraStrategy)expression.endStrategy;
            if (Objects.isNull(eraStrategy.drugCodesetId)) {
                warnings.add(new DefaultWarning(WarningSeverity.CRITICAL, "Cohort Exit Criteria misses a Drug Concept Set"));
            }
        }
    }
}
