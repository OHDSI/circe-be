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
import org.ohdsi.circe.vocabulary.Concept;

/**
 *
 * @author cknoll1
 */
public class Measurement extends Criteria {
  
  @JsonProperty("CodesetId")  
  public Integer codesetId;
  
  @JsonProperty("First")
  public Boolean first;
  
  @JsonProperty("OccurrenceStartDate")
  public DateRange occurrenceStartDate;

  @JsonProperty("MeasurementType")
  public Concept[] measurementType;

  @JsonProperty("MeasurementTypeExclude")
  public boolean measurementTypeExclude = false;
	
  @JsonProperty("Operator")
  public Concept[] operator;

  @JsonProperty("ValueAsNumber")
  public NumericRange valueAsNumber;

  @JsonProperty("ValueAsConcept")
  public Concept[] valueAsConcept;
  
  @JsonProperty("Unit")
  public Concept[] unit;
  
  @JsonProperty("RangeLow")
  public NumericRange rangeLow;

  @JsonProperty("RangeHigh")
  public NumericRange rangeHigh;

  @JsonProperty("RangeLowRatio")
  public NumericRange rangeLowRatio;

  @JsonProperty("RangeHighRatio")
  public NumericRange rangeHighRatio;

  @JsonProperty("Abnormal")
  public Boolean abnormal;
  
  @JsonProperty("MeasurementSourceConcept")
  public Integer measurementSourceConcept;
  
  @JsonProperty("Age")
  public NumericRange age;
  
  @JsonProperty("Gender")
  public Concept[] gender;
  
  @JsonProperty("ProviderSpecialty")
  public Concept[] providerSpecialty;

  @JsonProperty("VisitType")
  public Concept[] visitType;

  @Override
  public String accept(IGetCriteriaSqlDispatcher dispatcher)
  {
    return dispatcher.getCriteriaSql(this);
  }
  
  
}
