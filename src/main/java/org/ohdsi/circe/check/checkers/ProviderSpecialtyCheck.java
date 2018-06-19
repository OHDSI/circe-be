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
import org.ohdsi.circe.check.operations.Execution;
import org.ohdsi.circe.check.utils.CriteriaNameHelper;
import org.ohdsi.circe.cohortdefinition.ConditionOccurrence;
import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.VisitOccurrence;

public class ProviderSpecialtyCheck extends BaseCriteriaCheck {

    private static final String PROVIDER_SPECIALTY_ERROR = "Provider specialty is empty at %s in %s criteria.";

    @Override
    protected void checkCriteria(Criteria criteria, String groupName, WarningReporter reporter) {

        WarningReporterHelper helper = new WarningReporterHelper(reporter, PROVIDER_SPECIALTY_ERROR, groupName);
        final String criteriaName = CriteriaNameHelper.getCriteriaName(criteria);
        final Execution addWarning = helper.addWarning(criteriaName);
        match(criteria)
                .isA(ConditionOccurrence.class)
                .then(c -> match((ConditionOccurrence)c)
                        .when(conditionOccurrence -> Objects.isNull(conditionOccurrence.providerSpecialty) || conditionOccurrence.providerSpecialty.length == 0)
                        .then(addWarning))
                .isA(VisitOccurrence.class)
                .then(c -> match((VisitOccurrence)c)
                        .when(visitOccurrence -> Objects.isNull(visitOccurrence.providerSpecialty) || visitOccurrence.providerSpecialty.length == 0)
                        .then(addWarning));
    }
}
