/*
 *
 * Copyright 2017 Observational Health Data Sciences and Informatics
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Authors: Christopher Knoll
 *
 */
package org.ohdsi.circe.cohortdefinition;

import org.ohdsi.circe.cohortdefinition.builders.BuilderOptions;

/**
 *
 * @author cknoll1
 */
public interface IGetCriteriaSqlDispatcher {
  String getCriteriaSql(LocationRegion locationRegion, BuilderOptions options);
  String getCriteriaSql(ConditionEra conditionEraCriteria, BuilderOptions options);
  String getCriteriaSql(ConditionOccurrence conditionOccurrenceCriteria, BuilderOptions options);
  String getCriteriaSql(Death deathCriteria, BuilderOptions options);
  String getCriteriaSql(DeviceExposure deviceExposureCriteria, BuilderOptions options);
  String getCriteriaSql(DoseEra doseEraCriteria, BuilderOptions options);
  String getCriteriaSql(DrugEra drugEraCriteria, BuilderOptions options);
  String getCriteriaSql(DrugExposure drugExposureCriteria, BuilderOptions options);
  String getCriteriaSql(Measurement measurementCriteria, BuilderOptions options);
  String getCriteriaSql(Observation observationCriteria, BuilderOptions options);
  String getCriteriaSql(ObservationPeriod observationPeriodCriteria, BuilderOptions options);
  String getCriteriaSql(PayerPlanPeriod payerPlanPeriodCriteria, BuilderOptions options);
  String getCriteriaSql(ProcedureOccurrence procedureOccurrenceCriteria, BuilderOptions options);
  String getCriteriaSql(Specimen specimenCriteria, BuilderOptions options);
  String getCriteriaSql(VisitOccurrence visitOccurrenceCriteria, BuilderOptions options);
}
