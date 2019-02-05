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
import org.ohdsi.circe.check.WarningSeverity;
import org.ohdsi.circe.check.utils.CriteriaNameHelper;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;
import org.ohdsi.circe.cohortdefinition.Death;
import org.ohdsi.circe.cohortdefinition.ObservationFilter;

public class DeathTimeWindowCheck extends BaseCorelatedCriteriaCheck {

	private static final String MESSAGE = "%s criteria causes cohort cannot be created since a patient dies before being diagnosed with a condition";
	private ObservationFilter observationFilter;

	@Override
	protected WarningSeverity defineSeverity() {

		return WarningSeverity.WARNING;
	}

	@Override
	protected void beforeCheck(WarningReporter reporter, CohortExpression expression) {

		observationFilter = expression.primaryCriteria.observationWindow;
	}

	@Override
	protected void checkCriteria(CorelatedCriteria criteria, String groupName, WarningReporter reporter) {

		String name = groupName + " " + CriteriaNameHelper.getCriteriaName(criteria.criteria);
		match(criteria.criteria)
						.isA(Death.class)
						.then(death -> match(criteria)
										.when(c -> Objects.nonNull(c.startWindow) && Comparisons.compareTo(observationFilter, c.startWindow) < 0)
										.then(() -> reporter.add(MESSAGE, name))
						);
	}
}
