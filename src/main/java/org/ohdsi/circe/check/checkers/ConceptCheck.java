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

import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.DemographicCriteria;

public class ConceptCheck extends BaseValueCheck {
    @Override
    protected void check(final CohortExpression expression, WarningReporter reporter) {
        checkPrimaryCriteria(expression.primaryCriteria, reporter);
        checkAdditionalCriteria(expression.additionalCriteria, reporter);
        checkInclusionRules(expression, reporter);
        checkCensoringCriteria(expression, reporter);
    }

    @Override
    protected void checkCriteria(DemographicCriteria criteria, WarningReporter reporter, String name) {
        ConceptCheckerFactory.getFactory(reporter, name).check(criteria);
    }

    @Override
    protected void checkCriteria(Criteria criteria, WarningReporter reporter, String name) {
        ConceptCheckerFactory.getFactory(reporter, name).check(criteria);
        checkCriteria(criteria.CorrelatedCriteria, reporter, name);
    }
}
