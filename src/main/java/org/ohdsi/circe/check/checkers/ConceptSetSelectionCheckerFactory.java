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
import org.ohdsi.circe.cohortdefinition.ConceptSetSelection;
import org.ohdsi.circe.cohortdefinition.ConditionEra;
import org.ohdsi.circe.cohortdefinition.ConditionOccurrence;
import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.Death;
import org.ohdsi.circe.cohortdefinition.DemographicCriteria;
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
import org.ohdsi.circe.cohortdefinition.VisitDetail;
import org.ohdsi.circe.cohortdefinition.VisitOccurrence;
import org.ohdsi.circe.vocabulary.Concept;

import java.util.Objects;
import java.util.function.Consumer;

import static org.ohdsi.circe.check.Constants.Attributes.UNIT_ATTR;
import static org.ohdsi.circe.check.operations.Operations.match;

public class ConceptSetSelectionCheckerFactory extends BaseCheckerFactory{
    private static final String WARNING_EMPTY_VALUE = "%s in the %s has empty %s value";

    private ConceptSetSelectionCheckerFactory(WarningReporter reporter, String groupName) {
        super(reporter, groupName);
    }

    public static ConceptSetSelectionCheckerFactory getFactory(WarningReporter reporter, String groupName) {
        return new ConceptSetSelectionCheckerFactory(reporter, groupName);
    }

    @Override
    protected Consumer<Criteria> getCheck(Criteria criteria) {
        Consumer<Criteria> result = c -> { };
        if (criteria instanceof VisitDetail) {
            result = c -> {
                VisitDetail vd = (VisitDetail) c;
                checkConcept(vd.visitDetailTypeCS, Constants.Criteria.VISIT_DETAIL, Constants.Attributes.VISIT_DETAIL_TYPE_ATTR);
                checkConcept(vd.genderCS, Constants.Criteria.VISIT_DETAIL, Constants.Attributes.GENDER_ATTR);
                checkConcept(vd.providerSpecialtyCS, Constants.Criteria.VISIT_DETAIL, Constants.Attributes.PROVIDER_SPECIALITY_ATTR);
                checkConcept(vd.placeOfServiceCS, Constants.Criteria.VISIT_DETAIL, Constants.Attributes.PLACE_OF_SERVICE_ATTR);
            };
        }
        return result;
    }

    @Override
    protected Consumer<DemographicCriteria> getCheck(DemographicCriteria criteria) {
        return c -> {};
    }

    private void checkConcept(ConceptSetSelection conceptSetSelection, String criteriaName, String attribute) {
        Consumer<String> warning = (t) -> reporter.add(t, groupName, criteriaName, attribute);
        match(conceptSetSelection)
                .when(r -> Objects.nonNull(r) && Objects.isNull(r.codesetId))
                .then(() -> warning.accept(WARNING_EMPTY_VALUE));
    }
}
