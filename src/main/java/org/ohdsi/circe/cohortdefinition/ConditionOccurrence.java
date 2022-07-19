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
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.ohdsi.circe.cohortdefinition.builders.BuilderOptions;
import org.ohdsi.circe.vocabulary.Concept;

/**
 *
 * @author cknoll1
 */

@JsonTypeName("ConditionOccurrence")
public class ConditionOccurrence extends Criteria {

  @JsonProperty("CodesetId")
  public Integer codesetId;

  @JsonProperty("First")
  public Boolean first;

  @JsonProperty("OccurrenceStartDate")
  public DateRange occurrenceStartDate;

  @JsonProperty("OccurrenceEndDate")
  public DateRange occurrenceEndDate;

  @JsonProperty("ConditionType")
  public Concept[] conditionType;

  @JsonProperty("ConditionTypeExclude")
  public Boolean conditionTypeExclude;

  @JsonProperty("StopReason")
  public TextFilter stopReason;

  @JsonProperty("ConditionSourceConcept")
  public Integer conditionSourceConcept;

  @JsonProperty("Age")
  public NumericRange age;

  @JsonProperty("Gender")
  public Concept[] gender;

  @JsonProperty("ProviderSpecialty")
  public Concept[] providerSpecialty;

  @JsonProperty("VisitType")
  public Concept[] visitType;

  @JsonProperty("ConditionStatus")
  public Concept[] conditionStatus;

  @Override
  public String accept(IGetCriteriaSqlDispatcher dispatcher, BuilderOptions options)
  {
    return dispatcher.getCriteriaSql(this, options);
  }
}
