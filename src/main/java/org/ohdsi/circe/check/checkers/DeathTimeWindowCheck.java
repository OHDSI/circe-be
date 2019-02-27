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
import java.util.Objects;
import org.ohdsi.circe.check.WarningSeverity;
import org.ohdsi.circe.check.utils.CriteriaNameHelper;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;
import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.Death;

public class DeathTimeWindowCheck extends BaseCorelatedCriteriaCheck {

	private static final String MESSAGE = "%s attempts to identify death event prior to index event. Events post-death may not be available";

	@Override
	protected WarningSeverity defineSeverity() {

		return WarningSeverity.WARNING;
	}

	protected void checkCriteria(CorelatedCriteria criteria, String groupName, WarningReporter reporter) {

		String name = groupName + " " + CriteriaNameHelper.getCriteriaName(criteria.criteria);
		match(criteria.criteria)
						.isA(Death.class)
						.then(death -> match(criteria)
										.when(c -> Comparisons.isBefore(c.startWindow))
										.then(() -> reporter.add(MESSAGE, name))
						);
	}

	@Override
	protected void internalCheck(CohortExpression expression, WarningReporter reporter) {
		super.internalCheck(expression, reporter);

		checkCriteriaList(expression.additionalCriteria.criteriaList, ADDITIONAL_RULE, reporter);
		checkCriteriaList(expression.primaryCriteria.criteriaList, INITIAL_EVENT, reporter);
	}

	private void checkCriteriaList(Object[] criteriaList, String groupName, WarningReporter reporter) {

		if (Objects.nonNull(criteriaList)) {
			Arrays.stream(criteriaList).forEach(c -> {
				Criteria criteria = null;
				if (c instanceof CorelatedCriteria) {
					criteria = ((CorelatedCriteria)c).criteria;
					checkCriteria((CorelatedCriteria) c, groupName, reporter);
				} else if (c instanceof Criteria) {
					criteria = (Criteria)c;
				}
				if (Objects.nonNull(criteria)) {
					checkCriteriaGroup(criteria, groupName, reporter);
				}
			});
		}
	}

}
