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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.ohdsi.circe.check.warnings.ConceptSetWarning;
import org.ohdsi.circe.check.Warning;
import org.ohdsi.circe.check.WarningSeverity;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.ConceptSet;
import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;
import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.CriteriaGroup;
import org.ohdsi.circe.cohortdefinition.CustomEraStrategy;

public class UnusedConceptsCheck extends BaseCheck {

    @Override
    protected WarningSeverity defineSeverity() {

        return WarningSeverity.WARNING;
    }

    @Override
    protected WarningReporter getReporter(WarningSeverity severity, List<Warning> warnings) {

        return (template, conceptSet) -> warnings.add(new ConceptSetWarning(severity, template, (ConceptSet) conceptSet[0]));
    }

    @Override
    public void check(CohortExpression expression, WarningReporter reporter) {

        List<Criteria> additionalCriteria = getAdditionalCriteria(expression);

        Arrays.stream(expression.conceptSets)
                .filter(conceptSet -> this.isNotUsed(expression, additionalCriteria, conceptSet))
                .forEach(conceptSet -> reporter.add("Concept Set \"%s\" is not used", conceptSet));

    }

    private List<Criteria> getAdditionalCriteria(CohortExpression expression) {

        if (Objects.isNull(expression.additionalCriteria)) {
            return Collections.emptyList();
        }
        List<Criteria> additionalCriteria = new ArrayList<>(toCriteriaList(expression.additionalCriteria.criteriaList));
        additionalCriteria.addAll(toCriteriaList(expression.additionalCriteria.groups));
        return additionalCriteria;

    }

    private boolean isNotUsed(CohortExpression expression, List<Criteria> additionalCriteria, ConceptSet conceptSet) {
        return !isUsed(expression, additionalCriteria, conceptSet);
    }

    private boolean isUsed(CohortExpression expression, List<Criteria> additionalCriteria, ConceptSet conceptSet) {
        if (isConceptSetUsed(conceptSet, Arrays.asList(expression.primaryCriteria.criteriaList))) {
            return true;
        }
        if (isConceptSetUsed(conceptSet, additionalCriteria)) {
            return true;
        }
        if (expression.inclusionRules.stream().anyMatch(rule -> isConceptSetUsed(conceptSet, rule.expression))) {
            return true;
        }

        if (expression.endStrategy instanceof CustomEraStrategy &&
                Objects.equals(((CustomEraStrategy) expression.endStrategy).drugCodesetId, conceptSet.id)) {
            return true;
        }

        return isConceptSetUsed(conceptSet, Arrays.asList(expression.censoringCriteria));
    }

    private boolean isConceptSetUsed(ConceptSet conceptSet, List<Criteria> criteriaList) {

        CriteriaCheckerFactory factory = CriteriaCheckerFactory.getFactory(conceptSet);
        boolean mainCheck = criteriaList.stream().anyMatch(criteria -> factory.getCriteriaChecker(criteria).apply(criteria));
        return mainCheck || criteriaList.stream().anyMatch(criteria -> {
            if (criteria.CorrelatedCriteria != null) {
                return isConceptSetUsed(conceptSet, criteria.CorrelatedCriteria);
            } else {
                return false;
            }
        });
    }

    private boolean isConceptSetUsed(ConceptSet conceptSet, CriteriaGroup group) {

        List<Criteria> criteriaList = toCriteriaList(group.criteriaList);
        return isConceptSetUsed(conceptSet, criteriaList) ||
                Arrays.stream(group.groups).anyMatch(cg -> isConceptSetUsed(conceptSet, cg));
    }

    private List<Criteria> toCriteriaList(CorelatedCriteria[] criteriaList) {

        return Objects.nonNull(criteriaList) ? Arrays.stream(criteriaList)
                .map(c -> c.criteria).collect(Collectors.toList()) : Collections.emptyList();
    }

    private List<Criteria> toCriteriaList(CriteriaGroup[] groups) {

        List<Criteria> criteria = new ArrayList<>();
        Arrays.stream(groups)
                .map(c -> c.criteriaList)
                .map(this::toCriteriaList)
                .forEach(criteria::addAll);
        return criteria;
    }
}
