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

import org.ohdsi.circe.check.Check;
import org.ohdsi.circe.check.Warning;
import org.ohdsi.circe.check.WarningSeverity;
import org.ohdsi.circe.check.warnings.DefaultWarning;
import org.ohdsi.circe.cohortdefinition.CohortExpression;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseCheck implements Check {
    protected static final String INCLUSION_RULE = "inclusion rule ";
    protected static final String ADDITIONAL_RULE = "additional rule";
    protected static final String INITIAL_EVENT = "initial event";

    @Override
    public final List<Warning> check(CohortExpression expression) {

        List<Warning> warnings = new ArrayList<>();
        check(expression, defineReporter(warnings));
        return warnings;
    }

    protected WarningSeverity defineSeverity() {

        return WarningSeverity.CRITICAL;
    }

    protected WarningReporter defineReporter(List<Warning> warnings) {

        return getReporter(defineSeverity(), warnings);
    }

    protected abstract void check(CohortExpression expression, WarningReporter reporter);

    protected WarningReporter getReporter(WarningSeverity severity, List<Warning> warnings) {

        return (template, params) -> warnings.add(new DefaultWarning(severity,
                String.format(template, params)));
    }
}
