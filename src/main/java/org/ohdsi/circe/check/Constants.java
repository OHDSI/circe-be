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
 *   Authors: Vitaly Koulakov, Sergey Suvorov
 *
 */

package org.ohdsi.circe.check;

public interface Constants {

    interface Criteria {

        String CONDITION_ERA = "condition era";
        String CONDITION_OCCURRENCE = "condition occurrence";
        String DEATH = "death";
        String DEVICE_EXPOSURE = "device exposure";
        String DOSE_ERA = "dose era";
        String DRUG_ERA = "drug era";
        String DRUG_EXPOSURE = "drug exposure";
        String MEASUREMENT = "measurement";
        String OBSERVATION = "observation";
        String PROCEDURE_OCCURRENCE = "procedure occurrence";
        String SPECIMEN = "specimen";
        String VISIT_OCCURRENCE = "visit occurrence";
        String PAYER_PLAN_PERIOD = "payer plan period";
        String OBSERVATION_PERIOD = "observation period";
        String LOCATION_REGION = "location region";
        String DEMOGRAPHIC = "demographic";
    }

    interface Attributes {

        String AGE_ATTR = "age";
        String QUANTITY_ATTR = "quantity";
        String OCCURRENCE_START_DATE_ATTR = "occurrence start date";
        String OCCURRENCE_END_DATE_ATTR = "occurrence end date";
        String ERA_START_DATE_ATTR = "era start date";
        String ERA_END_DATE_ATTR = "era end date";
        String DOSE_VALUE_ATTR = "dose value";
        String ERA_LENGTH_ATTR = "era length";
        String AGE_AT_START_ATTR = "age at start";
        String AGE_AT_END_ATTR = "age at end";
        String OCCURRENCE_COUNT_ATTR = "occurrence count";
        String GAP_DAYS_ATTR = "gap days";
        String AGE_AT_ERA_START_ATTR = "age at era start";
        String AGE_AT_ERA_END_ATTR = "age at era end";
        String REFILLS_ATTR = "refills";
        String DAYS_SUPPLY_ATTR = "days supply";
        String EFFECTIVE_DRUG_DOSE_ATTR = "effective drug dose";
        String VALUE_AS_NUMBER_ATTR = "value as number";
        String RANGE_LOW_ATTR = "range low";
        String RANGE_HIGH_ATTR = "range high";
        String RANGE_LOW_RATIO_ATTR = "range low ratio";
        String RANGE_HIGH_RATIO_ATTR = "range high ratio";
        String PERIOD_START_DATE_ATTR = "period start date";
        String PERIOD_END_DATE_ATTR = "period end date";
        String PERIOD_LENGTH_ATTR = "period length";
        String USER_DEFINED_PERIOD_ATTR = "user defined period";
        String VISIT_LENGTH_ATTR = "visit length";
        String CENSOR_WINDOW_ATTR = "censor window";
        String GENDER_ATTR = "gender";
        String RACE_ATTR = "race";
        String ETHNICITY_ATTR = "ethnicity";
        String VISIT_TYPE_ATTR = "visit";
        String PROVIDER_SPECIALITY_ATTR = "provider speciality";
        String CONDITION_TYPE_ATTR = "condition type";
        String DEATH_TYPE_ATTR = "death type";
        String DEVICE_TYPE_ATTR = "device type";
        String UNIT_ATTR = "unit";
        String DRUG_TYPE_ATTR = "drug type";
        String ROUTE_CONCEPT_ATTR = "route concept";
        String DOSE_UNIT_ATTR = "dose unit";
        String MEASUREMENT_TYPE_ATTR = "measurement type";
        String OPERATOR_ATTR = "operator";
        String VALUE_AS_CONCEPT_ATTR = "value as concept";
        String OBSERVATION_TYPE_ATTR = "observation type";
        String QUALIFIER_ATTR = "qualifier";
        String PERIOD_TYPE_ATTR = "period type";
        String PROCEDURE_TYPE_ATTR = "procedure type";
        String MODIFIER_ATTR = "modifier";
        String SPECIMEN_TYPE_ATTR = "specimen type";
        String ANATOMIC_SITE_ATTR = "anatomic site";
        String DISEASE_STATUS_ATTR = "disease status";
        String PLACE_OF_SERVICE_ATTR = "place of service";
        String LOCATION_REGION_START_DATE_ATTR = "location region start date";
        String LOCATION_REGION_END_DATE_ATTR = "location region end date";
        String STOP_REASON_ATTR = "stop reason";
        String UNIQUE_DEVICE_ID_ATTR = "unique device id";
        String LOT_NUMBER_ATTR = "lot number";
        String VALUE_AS_STRING_ATTR = "value as string";
        String SOURCE_ID_ATTR = "source id";
    }
}
