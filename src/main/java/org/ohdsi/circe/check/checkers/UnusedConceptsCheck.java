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
import org.ohdsi.circe.cohortdefinition.InclusionRule;

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

        for(final ConceptSet conceptSet : expression.conceptSets) {
            boolean hasUsed;
            if (!(hasUsed = isConceptSetUsed(conceptSet, Arrays.asList(expression.primaryCriteria.criteriaList)))) {
                List<Criteria> additionalCriterion = Objects.nonNull(expression.additionalCriteria) ?
                        toCriteriaList(expression.additionalCriteria.criteriaList) : Collections.emptyList();
                if (!(hasUsed = isConceptSetUsed(conceptSet, additionalCriterion))) {
                    for(InclusionRule rule : expression.inclusionRules){
                        if (hasUsed = isConceptSetUsed(conceptSet, rule.expression)){
                            break;
                        }
                    }
                    if (!hasUsed && expression.endStrategy instanceof CustomEraStrategy) {
                        hasUsed = Objects.equals(((CustomEraStrategy) expression.endStrategy).drugCodesetId,
                                conceptSet.id);
                    }
                    if (!hasUsed) {
                        hasUsed = isConceptSetUsed(conceptSet, Arrays.asList(expression.censoringCriteria));
                    }
                }
            }
            if (!hasUsed) {
                reporter.add("Concept Set \"%s\" is not used", conceptSet);
            }
        }
    }

    private boolean isConceptSetUsed(ConceptSet concept, List<Criteria> criteriaList) {

        CriteriaCheckerFactory factory = CriteriaCheckerFactory.getFactory(concept);
        return criteriaList.stream().anyMatch(criteria -> factory.getCriteriaChecker(criteria).apply(criteria));
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
}
