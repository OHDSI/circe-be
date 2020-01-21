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
import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;
import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.CriteriaGroup;
import org.ohdsi.circe.cohortdefinition.DemographicCriteria;
import org.ohdsi.circe.cohortdefinition.InclusionRule;
import org.ohdsi.circe.cohortdefinition.PrimaryCriteria;

import java.util.Arrays;
import java.util.Objects;

public abstract class BaseValueCheck extends BaseCheck {
    protected static final String INCLUSION_CRITERIA = "Inclusion criteria ";
    protected static final String PRIMARY_CRITERIA = "Primary criteria";
    protected static final String ADDITIONAL_CRITERIA = "Additional criteria";
    protected static final String CENSORING_CRITERIA = "Censoring events";

    @Override
    protected void check(final CohortExpression expression, WarningReporter reporter) {
        checkPrimaryCriteria(expression.primaryCriteria, reporter);
        checkAdditionalCriteria(expression.additionalCriteria, reporter);
        checkInclusionRules(expression, reporter);
        checkCensoringCriteria(expression, reporter);
    }

    protected void checkPrimaryCriteria(PrimaryCriteria primaryCriteria, WarningReporter reporter) {
        if (Objects.nonNull(primaryCriteria)) {
            Arrays.stream(primaryCriteria.criteriaList)
                    .forEach(criteria -> checkCriteria(criteria, reporter, PRIMARY_CRITERIA));
        }
    }

    protected void checkAdditionalCriteria(CriteriaGroup criteriaGroup, WarningReporter reporter) {
        if (Objects.nonNull(criteriaGroup)) {
            Arrays.stream(criteriaGroup.criteriaList)
                    .forEach(criteria -> checkCriteria(criteria, reporter, ADDITIONAL_CRITERIA));
            Arrays.stream(criteriaGroup.demographicCriteriaList)
                    .forEach(criteria -> checkCriteria(criteria, reporter, ADDITIONAL_CRITERIA));
            Arrays.stream(criteriaGroup.groups)
                    .forEach(criteria -> checkAdditionalCriteria(criteria, reporter));
        }
    }

    protected void checkCensoringCriteria(final CohortExpression expression, WarningReporter reporter) {
        if (Objects.nonNull(expression.censoringCriteria)) {
            Arrays.stream(expression.censoringCriteria)
                    .forEach(criteria -> checkCriteria(criteria, reporter, CENSORING_CRITERIA));
        }
    }

    protected void checkInclusionRules(final CohortExpression expression, WarningReporter reporter) {
        for (final InclusionRule rule : expression.inclusionRules) {
            if (Objects.nonNull(rule.expression)) {
                for (CorelatedCriteria criteria : rule.expression.criteriaList) {
                    checkCriteria(criteria, reporter, INCLUSION_CRITERIA + "\"" + rule.name + "\"");
                }
                for (DemographicCriteria criteria : rule.expression.demographicCriteriaList) {
                    checkCriteria(criteria, reporter, INCLUSION_CRITERIA + "\"" + rule.name + "\"");
                }
            }
        }
    }

    protected void checkCriteria(CorelatedCriteria criteria, WarningReporter reporter, String name) {
        if (Objects.nonNull(criteria.criteria)) {
            checkCriteria(criteria.criteria, reporter, name);
        }
    }

    protected void checkCriteria(CriteriaGroup criteriaGroup, WarningReporter reporter, String name) {
        for (DemographicCriteria criteria : criteriaGroup.demographicCriteriaList) {
            checkCriteria(criteria, reporter, name);
        }
        for (CorelatedCriteria criteria : criteriaGroup.criteriaList) {
            checkCriteria(criteria, reporter, name);
        }
        for (CriteriaGroup group : criteriaGroup.groups) {
            checkCriteria(group, reporter, name);
        }
    }

    protected void checkCriteria(DemographicCriteria criteria, WarningReporter reporter, String name) {
        getFactory(reporter, name).check(criteria);
    }

    protected void checkCriteria(Criteria criteria, WarningReporter reporter, String name) {
        if (Objects.nonNull(criteria.CorrelatedCriteria)) {
            checkCriteria(criteria.CorrelatedCriteria, reporter, name);
        }
        getFactory(reporter, name).check(criteria);
    }

    protected abstract BaseCheckerFactory getFactory(WarningReporter reporter, String name);
}
