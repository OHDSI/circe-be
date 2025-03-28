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
@JsonTypeName("DrugExposure")
public class DrugExposure extends Criteria {
  @JsonProperty("CodesetId")  
  public Integer codesetId;
  
  @JsonProperty("First")
  public Boolean first;
  
  @JsonProperty("OccurrenceStartDate")
  public DateRange occurrenceStartDate;

  @JsonProperty("OccurrenceEndDate")
  public DateRange occurrenceEndDate;

  @JsonProperty("DrugType")
  public Concept[] drugType;

  @JsonProperty("DrugTypeCS")
  public ConceptSetSelection drugTypeCS;

  @JsonProperty("DrugTypeExclude")
  public boolean drugTypeExclude = false;
	
  @JsonProperty("StopReason")
  public TextFilter stopReason;
  
  @JsonProperty("Refills")
  public NumericRange refills;
  
  @JsonProperty("Quantity")
  public NumericRange quantity;
  
  @JsonProperty("DaysSupply")
  public NumericRange daysSupply;  
  
  @JsonProperty("RouteConcept")
  public Concept[] routeConcept;

  @JsonProperty("RouteConceptCS")
  public ConceptSetSelection routeConceptCS;

  @JsonProperty("EffectiveDrugDose")
  public NumericRange effectiveDrugDose;  

  @JsonProperty("DoseUnit")
  public Concept[] doseUnit;

  @JsonProperty("DoseUnitCS")
  public ConceptSetSelection doseUnitCS;

  @JsonProperty("LotNumber")
  public TextFilter lotNumber;  

  @JsonProperty("DrugSourceConcept")
  public Integer drugSourceConcept;
  
  @JsonProperty("Age")
  public NumericRange age;
  
  @JsonProperty("Gender")
  public Concept[] gender;
  
  @JsonProperty("GenderCS")
  public ConceptSetSelection genderCS;
  
  @JsonProperty("ProviderSpecialty")
  public Concept[] providerSpecialty;

  @JsonProperty("ProviderSpecialtyCS")
  public ConceptSetSelection providerSpecialtyCS;

  @JsonProperty("VisitType")
  public Concept[] visitType;

  @JsonProperty("VisitTypeCS")
  public ConceptSetSelection visitTypeCS;

  @Override
  public String accept(IGetCriteriaSqlDispatcher dispatcher, BuilderOptions options)
  {
    return dispatcher.getCriteriaSql(this, options);
  }  
}
