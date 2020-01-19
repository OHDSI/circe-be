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
import org.ohdsi.circe.cohortdefinition.ConditionOccurrence;
import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.DemographicCriteria;
import org.ohdsi.circe.cohortdefinition.DeviceExposure;
import org.ohdsi.circe.cohortdefinition.DrugExposure;
import org.ohdsi.circe.cohortdefinition.Observation;
import org.ohdsi.circe.cohortdefinition.Specimen;
import org.ohdsi.circe.cohortdefinition.TextFilter;

import java.util.Objects;
import java.util.function.Consumer;

import static org.ohdsi.circe.check.operations.Operations.match;

public class TextCheckerFactory extends BaseCheckerFactory{
    private static final String WARNING_EMPTY_VALUE = "%s in the %s has empty %s value";

    private TextCheckerFactory(WarningReporter reporter, String groupName) {
        super(reporter, groupName);
    }

    public static TextCheckerFactory getFactory(WarningReporter reporter, String groupName) {
        return new TextCheckerFactory(reporter, groupName);
    }

    @Override
    protected Consumer<Criteria> getCheck(Criteria criteria) {
        Consumer<Criteria> result = c -> { };
         if (criteria instanceof ConditionOccurrence) {
            result = c -> {
                ConditionOccurrence co = (ConditionOccurrence) c;
                checkText(co.stopReason, Constants.Criteria.CONDITION_OCCURRENCE, Constants.Attributes.STOP_REASON_ATTR);
            };
        } else if (criteria instanceof DeviceExposure) {
            result = c -> {
                DeviceExposure de = (DeviceExposure) c;
                checkText(de.uniqueDeviceId, Constants.Criteria.DEVICE_EXPOSURE, Constants.Attributes.UNIQUE_DEVICE_ID_ATTR);
            };
        } else if (criteria instanceof DrugExposure) {
            result = c -> {
                DrugExposure de = (DrugExposure) c;
                checkText(de.stopReason, Constants.Criteria.DRUG_EXPOSURE, Constants.Attributes.STOP_REASON_ATTR);
                checkText(de.lotNumber, Constants.Criteria.DRUG_EXPOSURE, Constants.Attributes.LOT_NUMBER_ATTR);
            };
        } else if (criteria instanceof Observation) {
            result = c -> {
                Observation o = (Observation) c;
                checkText(o.valueAsString, Constants.Criteria.OBSERVATION, Constants.Attributes.VALUE_AS_STRING_ATTR);
            };
        } else if (criteria instanceof Specimen) {
            result = c -> {
                Specimen specimen = (Specimen) c;
                checkText(specimen.sourceId, Constants.Criteria.SPECIMEN, Constants.Attributes.SOURCE_ID_ATTR);
            };
        }
        return result;
    }

    @Override
    protected Consumer<DemographicCriteria> getCheck(DemographicCriteria criteria) {
        // There are no text filters in demographic
        return c -> {};
    }

    private void checkText(TextFilter text, String criteriaName, String attribute) {
        Consumer<String> warning = (t) -> reporter.add(t, groupName, criteriaName, attribute);
        match(text)
                .when(r -> Objects.nonNull(r) && Objects.isNull(r.text))
                .then(() -> warning.accept(WARNING_EMPTY_VALUE));
    }
}
