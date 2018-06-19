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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.ohdsi.circe.check.WarningSeverity;
import org.ohdsi.circe.check.operations.Operations;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.ConceptSet;
import org.ohdsi.circe.cohortdefinition.ConditionEra;
import org.ohdsi.circe.cohortdefinition.ConditionOccurrence;
import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.CustomEraStrategy;
import org.ohdsi.circe.cohortdefinition.Death;
import org.ohdsi.circe.cohortdefinition.DeviceExposure;
import org.ohdsi.circe.cohortdefinition.DoseEra;
import org.ohdsi.circe.cohortdefinition.DrugEra;
import org.ohdsi.circe.cohortdefinition.DrugExposure;
import org.ohdsi.circe.cohortdefinition.Measurement;
import org.ohdsi.circe.cohortdefinition.Observation;
import org.ohdsi.circe.cohortdefinition.ProcedureOccurrence;
import org.ohdsi.circe.cohortdefinition.Specimen;
import org.ohdsi.circe.cohortdefinition.VisitOccurrence;

public class DrugDomainCheck extends BaseCheck {

    private static final String MESSAGE = "%s %s used in initial event and not used for cohort exit criteria";
    private CohortExpression expression;

    @Override
    protected WarningSeverity defineSeverity() {

        return WarningSeverity.INFO;
    }

    @Override
    protected void check(CohortExpression expression, WarningReporter reporter) {

        this.expression = expression;
        List<ConceptSet> conceptSets = Arrays.stream(expression.primaryCriteria.criteriaList)
                .map(this::mapCriteria)
                .filter(this::isConceptInDrugDomain)
                .map(this::mapConceptSet)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (expression.endStrategy instanceof CustomEraStrategy) {
            CustomEraStrategy eraStrategy = (CustomEraStrategy)expression.endStrategy;
            conceptSets = conceptSets.stream()
                    .filter(conceptSet -> conceptSet.id != eraStrategy.drugCodesetId)
                    .collect(Collectors.toList());
        }
        if (!conceptSets.isEmpty()) {
            String names = conceptSets.stream().map(conceptSet -> conceptSet.name)
                    .collect(Collectors.joining(", "));
            String title = conceptSets.size() > 1 ? "Concept sets" : "Concept set";
            reporter.add(MESSAGE, title, names);
        }
    }

    private Integer mapCriteria(Criteria criteria) {

        return Operations.<Criteria, Integer>match(criteria)
                .isA(ConditionEra.class).thenReturn(c -> ((ConditionEra)c).codesetId)
                .isA(ConditionOccurrence.class).thenReturn(c -> ((ConditionOccurrence)c).codesetId)
                .isA(Death.class).thenReturn(c -> ((Death)c).codesetId)
                .isA(DeviceExposure.class).thenReturn(c -> ((DeviceExposure)c).codesetId)
                .isA(DoseEra.class).thenReturn(c -> ((DoseEra)c).codesetId)
                .isA(DrugEra.class).thenReturn(c -> ((DrugEra)c).codesetId)
                .isA(DrugExposure.class).thenReturn(c -> ((DrugExposure)c).codesetId)
                .isA(Measurement.class).thenReturn(c -> ((Measurement)c).codesetId)
                .isA(Observation.class).thenReturn(c -> ((Observation)c).codesetId)
                .isA(ProcedureOccurrence.class).thenReturn(c -> ((ProcedureOccurrence)c).codesetId)
                .isA(Specimen.class).thenReturn(c -> ((Specimen)c).codesetId)
                .isA(VisitOccurrence.class).thenReturn(c -> ((VisitOccurrence)c).codesetId)
                .value();
    }

    private boolean isConceptInDrugDomain(Integer codesetId) {

        Optional<ConceptSet> conceptSet = Arrays.stream(expression.conceptSets).filter(getConceptSetIdPredicate(codesetId))
                .findFirst();
        return conceptSet.map(cs ->
                Arrays.stream(cs.expression.items)
                        .anyMatch(item -> "Drug".equalsIgnoreCase(item.concept.domainId)))
                .orElse(false);
    }

    private Predicate<ConceptSet> getConceptSetIdPredicate(Integer codesetId) {

        return cs -> Objects.nonNull(cs) && Objects.equals(cs.id, codesetId);
    }

    private ConceptSet mapConceptSet(Integer codesetId) {

        return Arrays.stream(expression.conceptSets)
                .filter(getConceptSetIdPredicate(codesetId)).findFirst().orElse(null);
    }
}
