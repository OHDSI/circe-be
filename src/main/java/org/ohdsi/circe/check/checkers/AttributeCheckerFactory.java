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
 *   Authors: Sergey Suvorov
 *
 */

package org.ohdsi.circe.check.checkers;

import org.ohdsi.circe.check.Constants;
import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.DemographicCriteria;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

public class AttributeCheckerFactory extends BaseCheckerFactory{
    private static final String WARNING_EMPTY_VALUE = "%s in the %s does not have attributes";

    private AttributeCheckerFactory(WarningReporter reporter, String groupName) {
        super(reporter, groupName);
    }

    public static AttributeCheckerFactory getFactory(WarningReporter reporter, String groupName) {
        return new AttributeCheckerFactory(reporter, groupName);
    }

    @Override
    protected Consumer<Criteria> getCheck(Criteria criteria) {
        // Non-demographic criteria does not need to be checked,
        // as it always has observation period and occurence
        return c -> { };
    }

    @Override
    protected Consumer<DemographicCriteria> getCheck(DemographicCriteria criteria) {
        Consumer<DemographicCriteria> result = c -> {
            checkAttribute(Constants.Criteria.DEMOGRAPHIC,
                    criteria.age,
                    criteria.gender,
                    criteria.race,
                    criteria.ethnicity,
                    criteria.occurrenceStartDate,
                    criteria.occurrenceEndDate);
        };
        return result;
    }

    private void checkAttribute(String criteriaName, Object ... attributes) {
        Consumer<String> warning = (t) -> reporter.add(t, groupName, criteriaName);
        boolean hasValue = Arrays.stream(attributes).anyMatch(a -> Objects.nonNull(a));
        if (!hasValue) {
            warning.accept(WARNING_EMPTY_VALUE);
        }
    }
}
