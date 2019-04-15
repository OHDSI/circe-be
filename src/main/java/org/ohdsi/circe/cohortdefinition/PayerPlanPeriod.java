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
 * 
 *
 */
package org.ohdsi.circe.cohortdefinition;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.analysis.versioning.CdmVersion;
import org.ohdsi.circe.vocabulary.Concept;

@CdmVersion(min = 5.3)
public class PayerPlanPeriod extends Criteria {
	@JsonProperty("First")
	public Boolean first;
	
	@JsonProperty("PeriodStartDate")
	public DateRange periodStartDate;
	
	@JsonProperty("PeriodEndDate")
	public DateRange periodEndDate;
	
	@JsonProperty("UserDefinedPeriod")
	public Period userDefinedPeriod;
	
	@JsonProperty("PeriodLength")
	public NumericRange periodLength;
	
	@JsonProperty("AgeAtStart")
	public NumericRange ageAtStart;
	
	@JsonProperty("AgeAtEnd")
	public NumericRange ageAtEnd;
	
	@JsonProperty("Gender")
	public Concept[] gender;
	
	@JsonProperty("PayerConcept")
	public Integer payerConcept;
	
	@JsonProperty("PlanConcept")
	public Integer planConcept;
	
	@JsonProperty("SponsorConcept")
	public Integer sponsorConcept;
	
	@JsonProperty("StopReasonConcept")
	public Integer stopReasonConcept;
	
	@JsonProperty("PayerSourceConcept")
	public Integer payerSourceConcept;
	
	@JsonProperty("PlanSourceConcept")
	public Integer planSourceConcept;
	
	@JsonProperty("SponsorSourceConcept")
	public Integer sponsorSourceConcept;
	
	@JsonProperty("StopReasonSourceConcept")
	public Integer stopReasonSourceConcept;
	
	@Override
	public String accept(IGetCriteriaSqlDispatcher dispatcher) {
	  return dispatcher.getCriteriaSql(this);
	}
}
