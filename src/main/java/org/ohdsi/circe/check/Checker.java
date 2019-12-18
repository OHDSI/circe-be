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

package org.ohdsi.circe.check;

import java.util.ArrayList;
import java.util.List;
import org.ohdsi.circe.check.checkers.ConceptSetCriteriaCheck;
import org.ohdsi.circe.check.checkers.CriteriaContradictionsCheck;
import org.ohdsi.circe.check.checkers.DeathTimeWindowCheck;
import org.ohdsi.circe.check.checkers.DomainTypeCheck;
import org.ohdsi.circe.check.checkers.DrugDomainCheck;
import org.ohdsi.circe.check.checkers.DrugEraCheck;
import org.ohdsi.circe.check.checkers.DrugExposureCheck;
import org.ohdsi.circe.check.checkers.DuplicatesConceptSetCheck;
import org.ohdsi.circe.check.checkers.DuplicatesCriteriaCheck;
import org.ohdsi.circe.check.checkers.EmptyConceptSetCheck;
import org.ohdsi.circe.check.checkers.EmptyDomainTypeCheck;
import org.ohdsi.circe.check.checkers.EventsProgressionCheck;
import org.ohdsi.circe.check.checkers.ExitCriteriaCheck;
import org.ohdsi.circe.check.checkers.ExitCriteriaDaysOffsetCheck;
import org.ohdsi.circe.check.checkers.GenderCriteriaCheck;
import org.ohdsi.circe.check.checkers.IncompleteRuleCheck;
import org.ohdsi.circe.check.checkers.InitialEventCheck;
import org.ohdsi.circe.check.checkers.MeasurementCheck;
import org.ohdsi.circe.check.checkers.NoExitCriteriaCheck;
import org.ohdsi.circe.check.checkers.OcurrenceCheck;
import org.ohdsi.circe.check.checkers.RangeCheck;
import org.ohdsi.circe.check.checkers.TimePatternCheck;
import org.ohdsi.circe.check.checkers.TimeWindowCheck;
import org.ohdsi.circe.check.checkers.UnusedConceptsCheck;
import org.ohdsi.circe.cohortdefinition.CohortExpression;

public class Checker implements Check {

    private List<Check> getChecks() {
        List<Check> checks = new ArrayList<>();
        checks.add(new UnusedConceptsCheck());
        checks.add(new ExitCriteriaCheck());
        checks.add(new ExitCriteriaDaysOffsetCheck());
        checks.add(new RangeCheck());
        checks.add(new IncompleteRuleCheck());
        checks.add(new InitialEventCheck());
        checks.add(new NoExitCriteriaCheck());
        checks.add(new ConceptSetCriteriaCheck());
        checks.add(new GenderCriteriaCheck());
        checks.add(new DrugExposureCheck());
        checks.add(new MeasurementCheck());
        checks.add(new DrugEraCheck());
        checks.add(new OcurrenceCheck());
        checks.add(new DuplicatesCriteriaCheck());
        checks.add(new DuplicatesConceptSetCheck());
        checks.add(new DrugDomainCheck());
        checks.add(new EmptyConceptSetCheck());
        checks.add(new EventsProgressionCheck());
        checks.add(new TimeWindowCheck());
        checks.add(new TimePatternCheck());
        checks.add(new EmptyDomainTypeCheck());
        checks.add(new DomainTypeCheck());
        checks.add(new CriteriaContradictionsCheck());
        checks.add(new DeathTimeWindowCheck());
        return checks;
    }

    @Override
    public List<Warning> check(final CohortExpression expression) {

        List<Warning> result = new ArrayList<>();
        for(Check check : getChecks()) {
            result.addAll(check.check(expression));
        }
        return result;
    }
}
