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

package org.ohdsi.circe.check.utils;

import org.ohdsi.circe.check.Constants;
import org.ohdsi.circe.check.operations.Operations;
import org.ohdsi.circe.cohortdefinition.ConditionEra;
import org.ohdsi.circe.cohortdefinition.ConditionOccurrence;
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

public class CriteriaNameHelper {

    public static String getCriteriaName(org.ohdsi.circe.cohortdefinition.Criteria criteria) {

        return Operations.<org.ohdsi.circe.cohortdefinition.Criteria, String>match(criteria)
                .isA(ConditionEra.class).thenReturn(c -> Constants.Criteria.CONDITION_ERA)
                .isA(ConditionOccurrence.class).thenReturn(c -> Constants.Criteria.CONDITION_OCCURRENCE)
                .isA(Death.class).thenReturn(c -> Constants.Criteria.DEATH)
                .isA(DeviceExposure.class).thenReturn(c -> Constants.Criteria.DEVICE_EXPOSURE)
                .isA(DoseEra.class).thenReturn(c -> Constants.Criteria.DOSE_ERA)
                .isA(DrugEra.class).thenReturn(c -> Constants.Criteria.DRUG_ERA)
                .isA(DrugExposure.class).thenReturn(c -> Constants.Criteria.DRUG_EXPOSURE)
                .isA(Measurement.class).thenReturn(c -> Constants.Criteria.MEASUREMENT)
                .isA(Observation.class).thenReturn(c -> Constants.Criteria.OBSERVATION)
                .isA(ObservationPeriod.class).thenReturn(c -> Constants.Criteria.OBSERVATION_PERIOD)
                .isA(ProcedureOccurrence.class).thenReturn(c -> Constants.Criteria.PROCEDURE_OCCURRENCE)
                .isA(Specimen.class).thenReturn(c -> Constants.Criteria.SPECIMEN)
                .isA(VisitOccurrence.class).thenReturn(c -> Constants.Criteria.VISIT_OCCURRENCE)
                .isA(PayerPlanPeriod.class).thenReturn(c -> Constants.Criteria.PAYER_PLAN_PERIOD)
                .value();
    }
}
