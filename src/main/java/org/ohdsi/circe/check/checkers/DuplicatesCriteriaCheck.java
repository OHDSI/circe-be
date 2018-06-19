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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ohdsi.circe.check.Warning;
import org.ohdsi.circe.check.WarningSeverity;
import org.ohdsi.circe.check.operations.Operations;
import org.ohdsi.circe.check.utils.CriteriaNameHelper;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.ConditionEra;
import org.ohdsi.circe.cohortdefinition.ConditionOccurrence;
import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.Death;
import org.ohdsi.circe.cohortdefinition.DeviceExposure;
import org.ohdsi.circe.cohortdefinition.DoseEra;
import org.ohdsi.circe.cohortdefinition.DrugEra;
import org.ohdsi.circe.cohortdefinition.DrugExposure;
import org.ohdsi.circe.cohortdefinition.Measurement;
import org.ohdsi.circe.cohortdefinition.Observation;
import org.ohdsi.circe.cohortdefinition.ObservationPeriod;
import org.ohdsi.circe.cohortdefinition.PayerPlanPeriod;
import org.ohdsi.circe.cohortdefinition.ProcedureOccurrence;
import org.ohdsi.circe.cohortdefinition.Specimen;
import org.ohdsi.circe.cohortdefinition.VisitOccurrence;

public class DuplicatesCriteriaCheck extends BaseCriteriaCheck {

    private static final String DUPLICATE_WARNING = "Probably %s duplicates %s";
    private List<Pair<String, Criteria>> criteriaList = new ArrayList<>();

    @Override
    protected void afterCheck(WarningReporter reporter, CohortExpression expression) {

        if (criteriaList.size() > 1) {
            for (int i = 0; i <= criteriaList.size() - 2; i++) {
                Pair<String, Criteria> criteria = criteriaList.get(i);
                List<Pair<String, Criteria>> duplicates = criteriaList.subList(i + 1, criteriaList.size())
                        .stream().filter(pair -> compareCriteria(criteria.getRight(), pair.getRight()))
                        .collect(Collectors.toList());
                if (!duplicates.isEmpty()) {
                    String names = duplicates.stream().map(Pair::getLeft).collect(Collectors.joining(", "));
                    reporter.add(DUPLICATE_WARNING, criteria.getLeft(), names);
                }
            }
        }
    }

    @Override
    protected WarningSeverity defineSeverity() {

        return WarningSeverity.WARNING;
    }

    private boolean compareCriteria(Criteria c1, Criteria c2) {

        if (Objects.equals(c1.getClass(), c2.getClass())) {
            if (c1 instanceof ConditionEra) {
                ConditionEra era1 = (ConditionEra) c1, era2 = (ConditionEra) c2;
                return new EqualsBuilder()
                        .append(era1.codesetId, era2.codesetId)
                        .build();
            } else if (c1 instanceof ConditionOccurrence) {
                ConditionOccurrence co1 = (ConditionOccurrence) c1, co2 = (ConditionOccurrence) c2;
                return new EqualsBuilder()
                        .append(co1.codesetId, co2.codesetId)
                        .append(co1.conditionSourceConcept, co2.conditionSourceConcept).build();
            } else if (c1 instanceof Death) {
                Death death1 = (Death) c1, death2 = (Death) c2;
                return new EqualsBuilder()
                        .append(death1.codesetId, death2.codesetId)
                        .build();
            } else if (c1 instanceof DeviceExposure) {
                DeviceExposure e1 = (DeviceExposure) c1, e2 = (DeviceExposure) c2;
                return new EqualsBuilder()
                        .append(e1.codesetId, e2.codesetId)
                        .build();
            } else if (c1 instanceof DoseEra) {
                DoseEra d1 = (DoseEra) c1, d2 = (DoseEra) c2;
                return new EqualsBuilder()
                        .append(d1.codesetId, d2.codesetId)
                        .build();
            } else if (c1 instanceof DrugEra) {
                DrugEra drug1 = (DrugEra) c1, drug2 = (DrugEra) c2;
                return new EqualsBuilder()
                        .append(drug1.codesetId, drug2.codesetId)
                        .build();
            } else if (c1 instanceof DrugExposure) {
                DrugExposure de1 = (DrugExposure) c1, de2 = (DrugExposure) c2;
                return new EqualsBuilder()
                        .append(de1.codesetId, de2.codesetId)
                        .build();
            } else if (c1 instanceof Measurement) {
                Measurement m1 = (Measurement) c1, m2 = (Measurement) c2;
                return new EqualsBuilder()
                        .append(m1.codesetId, m2.codesetId)
                        .build();
            } else if (c1 instanceof Observation) {
                Observation o1 = (Observation) c1, o2 = (Observation) c2;
                return new EqualsBuilder()
                        .append(o1.codesetId, o2.codesetId)
                        .build();
            } else if (c1 instanceof ObservationPeriod) {
                ObservationPeriod op1 = (ObservationPeriod) c1, op2 = (ObservationPeriod) c2;
                return new EqualsBuilder()
                        .setTestRecursive(true)
                        .append(op1.periodStartDate, op2.periodStartDate)
                        .append(op1.periodEndDate, op2.periodEndDate)
                        .append(op1.periodLength, op2.periodLength)
                        .build();
            } else if (c1 instanceof ProcedureOccurrence) {
                ProcedureOccurrence p1 = (ProcedureOccurrence) c1, p2 = (ProcedureOccurrence) c2;
                return new EqualsBuilder()
                        .append(p1.codesetId, p2.codesetId)
                        .build();
            } else if (c1 instanceof Specimen) {
                Specimen s1 = (Specimen) c1, s2 = (Specimen) c2;
                return new EqualsBuilder()
                        .append(s1.codesetId, s2.codesetId)
                        .build();
            } else if (c1 instanceof VisitOccurrence) {
                VisitOccurrence vo1 = (VisitOccurrence) c1, vo2 = (VisitOccurrence) c2;
                return new EqualsBuilder()
                        .append(vo1.codesetId, vo2.codesetId)
                        .build();
            } else if (c1 instanceof PayerPlanPeriod) {
                PayerPlanPeriod p1 = (PayerPlanPeriod) c1, p2 = (PayerPlanPeriod) c2;
                return new EqualsBuilder()
                        .append(p1.payerConcept, p2.payerConcept)
                        .append(p1.payerSourceConcept, p2.payerSourceConcept)
                        .append(p1.planConcept, p2.planConcept)
                        .append(p1.planSourceConcept, p2.planSourceConcept)
                        .append(p1.sponsorConcept, p2.sponsorConcept)
                        .append(p1.sponsorSourceConcept, p2.sponsorSourceConcept)
                        .append(p1.stopReasonConcept, p2.stopReasonConcept)
                        .append(p1.stopReasonSourceConcept, p2.stopReasonSourceConcept)
                        .build();
            }
            return EqualsBuilder.reflectionEquals(c1, c2, false);
        } else {
            return false;
        }
    }

    @Override
    protected void checkCriteria(Criteria criteria, String groupName, WarningReporter reporter) {

        String criteriaName = CriteriaNameHelper.getCriteriaName(criteria) + " criteria in " + groupName;
        criteriaList.add(new ImmutablePair<>(criteriaName, criteria));
    }
}
