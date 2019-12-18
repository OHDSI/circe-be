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
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;
import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.InclusionRule;
import org.ohdsi.circe.cohortdefinition.ObservationFilter;
import org.ohdsi.circe.cohortdefinition.PrimaryCriteria;
import org.ohdsi.circe.cohortdefinition.Window;

public class RangeCheck extends BaseCheck {

    private static final String INCLUSION_CRITERIA = "Inclusion criteria ";
    private static final String PRIMARY_CRITERIA = "Primary criteria";
    private static final String NEGATIVE_VALUE_ERROR = "Time window in criteria \"%s\" has negative value %d at %s";

    @Override
    protected void check(final CohortExpression expression, WarningReporter reporter) {

        checkPrimaryCriteria(expression.primaryCriteria, reporter);
        checkInclusionRules(expression, reporter);
        RangeCheckerFactory.getFactory(reporter, PRIMARY_CRITERIA).check(expression);
        checkObservationFilter(expression.primaryCriteria.observationWindow, reporter, "observation window");
        RangeCheckerFactory.getFactory(reporter, PRIMARY_CRITERIA).checkRange(expression.censorWindow, "cohort", "censor window");
    }

    private void checkPrimaryCriteria(PrimaryCriteria primaryCriteria, WarningReporter reporter) {

        Arrays.stream(primaryCriteria.criteriaList)
                .forEach(criteria -> RangeCheckerFactory.getFactory(reporter, PRIMARY_CRITERIA).check(criteria));
    }

    private void checkInclusionRules(final CohortExpression expression, WarningReporter reporter) {

        for(final InclusionRule rule : expression.inclusionRules) {
            for(CorelatedCriteria criteria : rule.expression.criteriaList) {
                checkWindow(criteria.startWindow, reporter, rule.name);
                checkWindow(criteria.endWindow, reporter, rule.name);
                checkCriteria(criteria, reporter, INCLUSION_CRITERIA + "\"" + rule.name + "\"");
            }
        }
    }

    private void checkWindow(Window window, WarningReporter reporter, String name) {

        if (Objects.nonNull(window)) {
            if (Objects.nonNull(window.start) && Objects.nonNull(window.start.days) && window.start.days < 0) {
                reporter.add(NEGATIVE_VALUE_ERROR, name, window.start.days, "start");
            }
            if (Objects.nonNull(window.end) && Objects.nonNull(window.end.days) && window.end.days < 0) {
                reporter.add(NEGATIVE_VALUE_ERROR, name, window.end.days, "end");
            }
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

    private void checkCriteria(CorelatedCriteria criteria, WarningReporter reporter, String name) {

        checkCriteria(criteria.criteria, reporter, name);
    }

    private void checkCriteria(Criteria criteria, WarningReporter reporter, String name) {
        RangeCheckerFactory.getFactory(reporter, name).check(criteria);
        if (Objects.nonNull(criteria.CorrelatedCriteria) && Objects.nonNull(criteria.CorrelatedCriteria.criteriaList)) {
            for(CorelatedCriteria subCriteria : criteria.CorrelatedCriteria.criteriaList){
                checkWindow(subCriteria.startWindow, reporter, name);
                checkWindow(subCriteria.endWindow, reporter, name);
                checkCriteria(subCriteria, reporter, name);
            }
        }
    }
}
