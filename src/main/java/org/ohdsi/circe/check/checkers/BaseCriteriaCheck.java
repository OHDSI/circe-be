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

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;
import org.ohdsi.circe.cohortdefinition.Criteria;

public abstract class BaseCriteriaCheck extends BaseIterableCheck {

    @Override
    final protected void internalCheck(CohortExpression expression, WarningReporter reporter) {

        Arrays.stream(expression.primaryCriteria.criteriaList)
                .forEach(criteria -> checkCriteriaGroup(criteria, INITIAL_EVENT, reporter));
        expression.inclusionRules.forEach(
                inclusionRule ->
                        Arrays.stream(inclusionRule.expression.criteriaList)
                                .forEach(criteria -> checkCriteriaGroup(criteria.criteria,
                                        INCLUSION_RULE + inclusionRule.name, reporter))
        );
    }

    final protected void checkCriteriaGroup(Criteria criteria, String groupName, WarningReporter reporter) {

        checkCriteria(criteria, groupName, reporter);
        if (Objects.nonNull(criteria.CorrelatedCriteria)) {
            Consumer<CorelatedCriteria> corelatedCriteriaCheck = corelatedCriteria -> checkCriteriaGroup(corelatedCriteria.criteria, groupName, reporter);
            Arrays.stream(criteria.CorrelatedCriteria.criteriaList).forEach(corelatedCriteriaCheck);
            Arrays.stream(criteria.CorrelatedCriteria.groups).forEach(group ->
                    Arrays.stream(group.criteriaList).forEach(corelatedCriteriaCheck));
        }
    }

    protected abstract void checkCriteria(Criteria criteria, String groupName, WarningReporter reporter);
}
