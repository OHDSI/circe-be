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

/**
 *
 * @author cknoll1
 */
public interface IGetCriteriaSqlDispatcher {
  String getCriteriaSql(ConditionEra conditionEraCriteria);
  String getCriteriaSql(ConditionOccurrence conditionOccurrenceCriteria);
  String getCriteriaSql(Death deathCriteria);
  String getCriteriaSql(DeviceExposure deviceExposureCriteria);
  String getCriteriaSql(DoseEra doseEraCriteria);
  String getCriteriaSql(DrugEra drugEraCriteria);
  String getCriteriaSql(DrugExposure drugExposureCriteria);
  String getCriteriaSql(Measurement measurementCriteria);
  String getCriteriaSql(Observation observationCriteria);
  String getCriteriaSql(ObservationPeriod observationPeriodCriteria);
  String getCriteriaSql(ProcedureOccurrence procedureOccurrenceCriteria);
  String getCriteriaSql(Specimen specimenCriteria);
  String getCriteriaSql(VisitOccurrence specimenCriteria);
}
