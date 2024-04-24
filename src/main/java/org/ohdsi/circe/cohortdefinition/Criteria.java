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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.builders.BuilderOptions;
import org.ohdsi.circe.cohortdefinition.builders.ColumnFieldData;
import org.ohdsi.circe.cohortdefinition.builders.ColumnFieldDataType;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.WRAPPER_OBJECT)
@JsonSubTypes({
  @JsonSubTypes.Type(value = ConditionEra.class, name = "ConditionEra"),
  @JsonSubTypes.Type(value = ConditionOccurrence.class, name = "ConditionOccurrence"),
  @JsonSubTypes.Type(value = Death.class, name = "Death"),
  @JsonSubTypes.Type(value = DeviceExposure.class, name = "DeviceExposure"),
  @JsonSubTypes.Type(value = DoseEra.class, name = "DoseEra"),
  @JsonSubTypes.Type(value = DrugEra.class, name = "DrugEra"),
  @JsonSubTypes.Type(value = DrugExposure.class, name = "DrugExposure"),
  @JsonSubTypes.Type(value = LocationRegion.class, name = "LocationRegion"),
  @JsonSubTypes.Type(value = Measurement.class, name = "Measurement"),
  @JsonSubTypes.Type(value = Observation.class, name = "Observation"),
  @JsonSubTypes.Type(value = ObservationPeriod.class, name = "ObservationPeriod"),
  @JsonSubTypes.Type(value = ProcedureOccurrence.class, name = "ProcedureOccurrence"),
  @JsonSubTypes.Type(value = Specimen.class, name = "Specimen"),
  @JsonSubTypes.Type(value = VisitOccurrence.class, name = "VisitOccurrence"),
  @JsonSubTypes.Type(value = VisitDetail.class, name = "VisitDetail"),
  @JsonSubTypes.Type(value = PayerPlanPeriod.class, name = "PayerPlanPeriod")
})
public abstract class Criteria {

  public String accept(IGetCriteriaSqlDispatcher dispatcher) {
    return this.accept(dispatcher, null);
  }

  public abstract String accept(IGetCriteriaSqlDispatcher dispatcher, BuilderOptions options);
  
  public List<ColumnFieldData> getSelectedField(Boolean retainCohortCovariates) {
      return new ArrayList<>();
  }
  
  public String embedCriteriaGroup(String query) {
      query = StringUtils.replace(query, "@e.additonColumns", "");
      query = StringUtils.replace(query, "@additonColumnsGroup", "");
      return query;
  }
  
  public String embedWindowedCriteriaQuery(String query, Map<String, ColumnFieldData> mapDistinctField) {
      query = StringUtils.replace(query, "@additionColumnscc", "");
      query = StringUtils.replace(query, "@additionColumnGroupscc", "");
      return query;
  }
  
  public String embedWindowedCriteriaQueryP(String query) {
      query = StringUtils.replace(query, "@p.additionColumns", "");
      return query;
  }
  
  public String embedWrapCriteriaQuery(String query, List<String> selectColsPE, BuilderOptions options) {
      query = StringUtils.replace(query, "@QAdditionalColumnsInclusionN", "");
      return query;
  }

  @JsonProperty("CorrelatedCriteria")
  public CriteriaGroup CorrelatedCriteria;

  @JsonProperty("DateAdjustment")
  public DateAdjustment dateAdjustment;

}
