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
import java.util.stream.Collectors;
import org.ohdsi.circe.check.WarningSeverity;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.ConceptSet;

public class DuplicatesConceptSetCheck extends BaseCheck {

    private static final String DUPLICATES_WARNING = "Concept set %s contains the same concepts like %s";

    @Override
    protected WarningSeverity defineSeverity() {

        return WarningSeverity.WARNING;
    }

    @Override
    protected void check(CohortExpression expression, WarningReporter reporter) {

        if (expression.conceptSets.length > 1) {
            int size = expression.conceptSets.length;
            for(int i = 0; i <= size - 2; i++) {
                ConceptSet conceptSet = expression.conceptSets[i];
                List<ConceptSet> duplicates = Arrays.asList(expression.conceptSets).subList(i + 1, size).stream()
                        .filter(Comparisons.compare(conceptSet)).collect(Collectors.toList());
                if (!duplicates.isEmpty()) {
                    String names = duplicates.stream().map(cs -> cs.name).collect(Collectors.joining(", "));
                    reporter.add(DUPLICATES_WARNING, conceptSet.name, names);
                }
            }
        }
    }
}
