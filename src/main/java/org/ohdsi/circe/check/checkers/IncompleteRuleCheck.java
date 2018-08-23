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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.ohdsi.circe.check.Warning;
import org.ohdsi.circe.check.WarningSeverity;
import org.ohdsi.circe.check.warnings.IncompleteRuleWarning;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;
import org.ohdsi.circe.cohortdefinition.CriteriaGroup;
import org.ohdsi.circe.cohortdefinition.InclusionRule;

public class IncompleteRuleCheck extends BaseCheck {

	@Override
	protected WarningReporter getReporter(WarningSeverity severity, List<Warning> warnings) {
			return (name, params) -> warnings.add(new IncompleteRuleWarning(defineSeverity(), name));
	}

	@Override
	protected void check(CohortExpression expression, WarningReporter reporter) {
		if (Objects.nonNull(expression.inclusionRules)) {
			expression.inclusionRules
				.forEach(rule -> checkInclusionRule(rule, reporter));
		}
	}

	private void checkInclusionRule(InclusionRule rule, WarningReporter reporter) {
		if (rule.expression.isEmpty()) {
			reporter.add(rule.name + " has no criteria");
		}
	}
}
