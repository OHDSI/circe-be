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

import java.util.List;
import java.util.Objects;
import org.ohdsi.circe.check.Warning;
import org.ohdsi.circe.check.WarningSeverity;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.ResultLimit;

public class EventsProgressionCheck extends BaseCheck {

    private static final String WARNING = "%s limit may not have intended effect since it breaks all/latest/earliest progression";

    @Override
    protected WarningSeverity defineSeverity() {

        return WarningSeverity.WARNING;
    }

    @Override
    protected void check(CohortExpression expression, WarningReporter reporter) {

        int initialWeight = getWeight(expression.primaryCriteria.primaryLimit);
        int cohortInitialWeight = getWeight(expression.qualifiedLimit);
        // qualifying limit is ignored when no additionalCriteria specified
				int qualifyingWeight = (expression.additionalCriteria != null) ? 
								getWeight(expression.expressionLimit) 
								: LimitType.NONE.getWeigt();
        if (initialWeight - cohortInitialWeight < 0) {
            reporter.add(WARNING, "Cohort of initial events");
        }
        if (cohortInitialWeight - qualifyingWeight < 0 || initialWeight - qualifyingWeight < 0) {
            reporter.add(WARNING, "Qualifying cohort");
        }
    }

    private int getWeight(ResultLimit limit) {
        return Objects.nonNull(limit) && Objects.nonNull(limit.type) ?
                LimitType.fromName(limit.type).getWeigt() : LimitType.NONE.getWeigt();
    }

    enum LimitType {
        NONE(0, "null"), EARLIEST(0, "First"), LATEST(1, "Last"), ALL(2, "All");
        int weigt;
        String name;

        LimitType(int weigt, String name) {

            this.weigt = weigt;
            this.name = name;
        }

        public int getWeigt() {

            return weigt;
        }

        public String getName() {

            return name;
        }

        static LimitType fromName(String name) {

            for(LimitType type : values()) {
                if (Objects.equals(type.name, name)) {
                    return type;
                }
            }
            return NONE;
        }
    }
}
