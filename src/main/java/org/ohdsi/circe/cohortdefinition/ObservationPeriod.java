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
public class ObservationPeriod extends Criteria {
  
  @JsonProperty("First")
  public Boolean first;

  @JsonProperty("PeriodStartDate")
  public DateRange periodStartDate;

  @JsonProperty("PeriodEndDate")
  public DateRange periodEndDate;
  
  @JsonProperty("UserDefinedPeriod")
  public Period userDefinedPeriod;

  @JsonProperty("PeriodType")
  public Concept[] periodType;
  
  @JsonProperty("PeriodLength")
  public NumericRange periodLength;  

  @JsonProperty("AgeAtStart")
  public NumericRange ageAtStart;  

  @JsonProperty("AgeAtEnd")
  public NumericRange ageAtEnd;  
  
  
  @Override
  public String accept(IGetCriteriaSqlDispatcher dispatcher) {
    return dispatcher.getCriteriaSql(this);
  }  
}
