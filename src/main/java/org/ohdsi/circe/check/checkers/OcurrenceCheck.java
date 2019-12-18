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

import org.ohdsi.circe.check.WarningSeverity;
import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;

public class OcurrenceCheck extends BaseCorelatedCriteriaCheck {

    private static final String AT_LEAST_0_WARNING = "'at least 0' occurrence is not a real constraint, probably meant 'exactly 0' or 'at least 1'";
    private static final int AT_LEAST = 2;

    @Override
    protected WarningSeverity defineSeverity() {

        return WarningSeverity.WARNING;
    }

    @Override
    protected void checkCriteria(CorelatedCriteria criteria, String groupName, WarningReporter reporter) {

        match(criteria.occurrence)
                .when(o -> AT_LEAST == o.type && 0 == o.count)
                .then(() -> reporter.add(AT_LEAST_0_WARNING));
    }
}
