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
 *   Authors: Vitaly Koulakov, Sergey Suvorov
 *
 */

package org.ohdsi.circe.check.checkers;

import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;
import org.ohdsi.circe.cohortdefinition.InclusionRule;
import org.ohdsi.circe.cohortdefinition.ObservationFilter;
import org.ohdsi.circe.cohortdefinition.Window;

import java.util.Objects;
import java.util.Optional;

public class RangeCheck extends BaseValueCheck {
    private static final String NEGATIVE_VALUE_ERROR = "Time window in criteria \"%s\" has negative value %d at %s";

    @Override
    protected void check(final CohortExpression expression, WarningReporter reporter) {
        super.check(expression, reporter);
        RangeCheckerFactory.getFactory(reporter, PRIMARY_CRITERIA).check(expression);
        if (Objects.nonNull(expression.primaryCriteria)) {
            checkObservationFilter(expression.primaryCriteria.observationWindow, reporter, "observation window");
        }
        RangeCheckerFactory.getFactory(reporter, PRIMARY_CRITERIA).checkRange(expression.censorWindow, "cohort", "censor window");
    }

    protected void checkInclusionRules(final CohortExpression expression, WarningReporter reporter) {
        super.checkInclusionRules(expression, reporter);
        for(final InclusionRule rule : expression.inclusionRules) {
            if (Objects.nonNull(rule.expression)) {
                for (CorelatedCriteria criteria : rule.expression.criteriaList) {
                    checkWindow(criteria.startWindow, reporter, rule.name);
                    checkWindow(criteria.endWindow, reporter, rule.name);
                }
            }
        }
    }

    private void checkWindow(Window window, WarningReporter reporter, String name) {
        if (Objects.isNull(window)) {
            return;
        }
        checkAndReportIfNegative(window.start, reporter, name, "start");
        checkAndReportIfNegative(window.end, reporter, name, "end");

    }

    private void checkAndReportIfNegative(Window.Endpoint windowDetails, WarningReporter reporter, String name, String type) {
        if (Objects.nonNull(windowDetails) && Objects.nonNull(windowDetails.days) && windowDetails.days < 0) {
            reporter.add(NEGATIVE_VALUE_ERROR, name, windowDetails.days, type);
        } else if (Objects.nonNull(windowDetails) && Objects.nonNull(windowDetails.timeUnitValue) && windowDetails.timeUnitValue < 0) {
            reporter.add(NEGATIVE_VALUE_ERROR, name, windowDetails.timeUnitValue, type);
        }
    }


    private void checkObservationFilter(ObservationFilter filter, WarningReporter reporter, String name) {
        if (Objects.nonNull(filter)) {
            if (filter.priorDays < 0) {
                reporter.add(NEGATIVE_VALUE_ERROR, name, filter.priorDays, "prior days");
            }
            if (filter.postDays < 0) {
                reporter.add(NEGATIVE_VALUE_ERROR, name, filter.postDays, "post days");
            }
        }
    }

    protected void checkCriteria(CorelatedCriteria criteria, WarningReporter reporter, String name) {
        super.checkCriteria(criteria, reporter, name);
        checkWindow(criteria.startWindow, reporter, name);
        checkWindow(criteria.endWindow, reporter, name);
    }

    @Override
    protected BaseCheckerFactory getFactory(WarningReporter reporter, String name) {
        return RangeCheckerFactory.getFactory(reporter, name);
    }
}
