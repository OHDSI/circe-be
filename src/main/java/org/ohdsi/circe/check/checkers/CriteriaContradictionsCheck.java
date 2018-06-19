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
import java.util.List;
import org.apache.commons.lang3.Range;
import org.ohdsi.circe.check.WarningSeverity;
import org.ohdsi.circe.check.utils.CriteriaNameHelper;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;
import org.ohdsi.circe.cohortdefinition.Occurrence;

public class CriteriaContradictionsCheck extends BaseCorelatedCriteriaCheck {

    private static final String WARNING = "%s might be contradicted with %s and possibly will lead to 0 records";
    private List<CriteriaInto> criteriaList = new ArrayList<>();

    @Override
    protected WarningSeverity defineSeverity() {

        return WarningSeverity.WARNING;
    }

    @Override
    protected void afterCheck(WarningReporter reporter, CohortExpression expression) {

        if (criteriaList.size() > 1) {
            int size = criteriaList.size();
            for (int i = 0; i < size - 2; i++) {
                CriteriaInto info = criteriaList.get(i);
                criteriaList.subList(i + 1, size).stream()
                        .filter(ci -> Comparisons.compare(info.criteria.criteria, ci.criteria.criteria))
                        .filter(ci -> checkContradiction(info.criteria.occurrence, ci.criteria.occurrence))
                        .forEach(ci -> reporter.add(WARNING, info.name, ci.name));
            }
        }
    }

    private boolean checkContradiction(Occurrence o1, Occurrence o2) {

        Range<Integer> range1 = getOccurrenceRange(o1), range2 = getOccurrenceRange(o2);
        return !range1.isOverlappedBy(range2);
    }

    private Range<Integer> getOccurrenceRange(Occurrence occurrence) {

        Range<Integer> result = null;
        switch (occurrence.type) {
            case 0:
                result = Range.between(occurrence.count, occurrence.count);
                break;
            case 1:
                result = Range.between(Integer.MIN_VALUE, occurrence.count);
                break;
            case 2:
                result = Range.between(occurrence.count, Integer.MAX_VALUE);
                break;
        }
        return result;
    }

    @Override
    protected void checkCriteria(CorelatedCriteria criteria, String groupName, WarningReporter reporter) {

        final String name = groupName + " " + CriteriaNameHelper.getCriteriaName(criteria.criteria);
        criteriaList.add(new CriteriaInto(name, criteria));
    }

    class CriteriaInto {
        private String name;
        private CorelatedCriteria criteria;

        public CriteriaInto(String name, CorelatedCriteria criteria) {

            this.name = name;
            this.criteria = criteria;
        }

        public String getName() {

            return name;
        }

        public CorelatedCriteria getCriteria() {

            return criteria;
        }
    }
}
