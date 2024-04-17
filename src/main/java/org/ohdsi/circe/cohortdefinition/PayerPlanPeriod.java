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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.analysis.versioning.CdmVersion;
import org.ohdsi.circe.cohortdefinition.builders.BuilderOptions;
import org.ohdsi.circe.cohortdefinition.builders.ColumnFieldData;
import org.ohdsi.circe.cohortdefinition.builders.ColumnFieldDataType;
import org.ohdsi.circe.vocabulary.Concept;

@CdmVersion(range = ">=5.3")
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
	public String accept(IGetCriteriaSqlDispatcher dispatcher, BuilderOptions options) {
	  return dispatcher.getCriteriaSql(this, options);
	}
    
    @Override
    public List<ColumnFieldData> getSelectedField(BuilderOptions options) {
        List<ColumnFieldData> selectCols = new ArrayList<>();
        
        if (periodStartDate != null) {
            selectCols.add(new ColumnFieldData("payer_plan_period_start_date", ColumnFieldDataType.DATE));
        }
        
        if (periodEndDate != null) {
            selectCols.add(new ColumnFieldData("payer_plan_period_end_date", ColumnFieldDataType.DATE));
        }
        
        if (payerConcept != null) {
            selectCols.add(new ColumnFieldData("payer_concept_id", ColumnFieldDataType.INTEGER));
        }
        
        if (planConcept != null) {
            selectCols.add(new ColumnFieldData("plan_concept_id", ColumnFieldDataType.INTEGER));
        }
        
        if (sponsorConcept != null) {
            selectCols.add(new ColumnFieldData("sponsor_concept_id", ColumnFieldDataType.INTEGER));
        }
        
        if (stopReasonConcept != null) {
            selectCols.add(new ColumnFieldData("stop_reason_concept_id", ColumnFieldDataType.INTEGER));
        }
        
        if (stopReasonConcept != null) {
            selectCols.add(new ColumnFieldData("stop_reason_concept_id", ColumnFieldDataType.INTEGER));
        }
        
        if (payerSourceConcept != null) {
            selectCols.add(new ColumnFieldData("payer_source_concept_id", ColumnFieldDataType.INTEGER));
        }
        
        if (planSourceConcept != null) {
            selectCols.add(new ColumnFieldData("plan_source_concept_id", ColumnFieldDataType.INTEGER));
        }
        
        if (sponsorSourceConcept != null) {
            selectCols.add(new ColumnFieldData("sponsor_source_concept_id", ColumnFieldDataType.INTEGER));
        }
        
        if (stopReasonSourceConcept != null) {
            selectCols.add(new ColumnFieldData("stop_reason_source_concept_id", ColumnFieldDataType.INTEGER));
        }
        
        return selectCols;
    }
    
    @Override
    public String embedCriteriaGroup(String query) {
        ArrayList<String> selectColsCQ = new ArrayList<>();
        ArrayList<String> selectColsG = new ArrayList<>();
        
        if (periodStartDate != null) {
            selectColsCQ.add(", CQ.payer_plan_period_start_date");
            selectColsG.add(", G.payer_plan_period_start_date");
        }
        
        if (periodEndDate != null) {
            selectColsCQ.add(", CQ.payer_plan_period_end_date");
            selectColsG.add(", G.payer_plan_period_end_date");
        }
        
        if (payerConcept != null) {
            selectColsCQ.add(", CQ.payer_concept_id");
            selectColsG.add(", G.payer_concept_id");
        }
        
        if (planConcept != null) {
            selectColsCQ.add(", CQ.plan_concept_id");
            selectColsG.add(", G.plan_concept_id");
        }
        
        if (sponsorConcept != null) {
            selectColsCQ.add(", CQ.sponsor_concept_id");
            selectColsG.add(", G.sponsor_concept_id");
        }
        
        if (stopReasonConcept != null) {
            selectColsCQ.add(", CQ.stop_reason_concept_id");
            selectColsG.add(", G.stop_reason_concept_id");
        }
        
        if (stopReasonConcept != null) {
            selectColsCQ.add(", CQ.stop_reason_concept_id");
            selectColsG.add(", G.stop_reason_concept_id");
        }
        
        if (payerSourceConcept != null) {
            selectColsCQ.add(", CQ.payer_source_concept_id");
            selectColsG.add(", G.payer_source_concept_id");
        }
        
        if (planSourceConcept != null) {
            selectColsCQ.add(", CQ.plan_source_concept_id");
            selectColsG.add(", G.plan_source_concept_id");
        }
        
        if (sponsorSourceConcept != null) {
            selectColsCQ.add(", CQ.sponsor_source_concept_id");
            selectColsG.add(", G.sponsor_source_concept_id");
        }
        
        if (stopReasonSourceConcept != null) {
            selectColsCQ.add(", CQ.stop_reason_source_concept_id");
            selectColsG.add(", G.stop_reason_source_concept_id");
        }
        
        query = StringUtils.replace(query, "@e.additonColumns", StringUtils.join(selectColsCQ, ""));
        query = StringUtils.replace(query, "@additonColumnsGroup", StringUtils.join(selectColsG, ""));
        return query;
    }
    
    @Override
    public String embedWindowedCriteriaQuery(String query) {
        ArrayList<String> selectCols = new ArrayList<>();
        
        if (periodStartDate != null) {
            selectCols.add(", cc.payer_plan_period_start_date");
        }
        
        if (periodEndDate != null) {
            selectCols.add(", cc.payer_plan_period_end_date");
        }
        
        if (payerConcept != null) {
            selectCols.add(", cc.payer_concept_id");
        }
        
        if (planConcept != null) {
            selectCols.add(", cc.plan_concept_id");
        }
        
        if (sponsorConcept != null) {
            selectCols.add(", cc.sponsor_concept_id");
        }
        
        if (stopReasonConcept != null) {
            selectCols.add(", cc.stop_reason_concept_id");
        }
        
        if (stopReasonConcept != null) {
            selectCols.add(", cc.stop_reason_concept_id");
        }
        
        if (payerSourceConcept != null) {
            selectCols.add(", cc.payer_source_concept_id");
        }
        
        if (planSourceConcept != null) {
            selectCols.add(", cc.plan_source_concept_id");
        }
        
        if (sponsorSourceConcept != null) {
            selectCols.add(", cc.sponsor_source_concept_id");
        }
        
        if (stopReasonSourceConcept != null) {
            selectCols.add(", cc.stop_reason_source_concept_id");
        }
        
        query = StringUtils.replace(query, "@additionColumnscc", StringUtils.join(selectCols, ""));
        return query;
    }
    
    @Override
    public String embedWindowedCriteriaQueryP(String query) {
        ArrayList<String> selectColsA = new ArrayList<>();
        
        if (periodStartDate != null) {
            selectColsA.add(", A.payer_plan_period_start_date");
        }
        
        if (periodEndDate != null) {
            selectColsA.add(", A.payer_plan_period_end_date");
        }
        
        if (payerConcept != null) {
            selectColsA.add(", A.payer_concept_id");
        }
        
        if (planConcept != null) {
            selectColsA.add(", A.plan_concept_id");
        }
        
        if (sponsorConcept != null) {
            selectColsA.add(", A.sponsor_concept_id");
        }
        
        if (stopReasonConcept != null) {
            selectColsA.add(", A.stop_reason_concept_id");
        }
        
        if (stopReasonConcept != null) {
            selectColsA.add(", A.stop_reason_concept_id");
        }
        
        if (payerSourceConcept != null) {
            selectColsA.add(", A.payer_source_concept_id");
        }
        
        if (planSourceConcept != null) {
            selectColsA.add(", A.plan_source_concept_id");
        }
        
        if (sponsorSourceConcept != null) {
            selectColsA.add(", A.sponsor_source_concept_id");
        }
        
        if (stopReasonSourceConcept != null) {
            selectColsA.add(", A.stop_reason_source_concept_id");
        }
        
        query = StringUtils.replace(query, "@p.additionColumns", StringUtils.join(selectColsA, ""));
        return query;
    }
    
    @Override
    public String embedWrapCriteriaQuery(String query, List<String> selectColsPE) {
        ArrayList<String> selectCols = new ArrayList<>();
        
        if (periodStartDate != null) {
            selectCols.add(", Q.payer_plan_period_start_date");
            selectColsPE.add(", PE.payer_plan_period_start_date");
        }
        
        if (periodEndDate != null) {
            selectCols.add(", Q.payer_plan_period_end_date");
            selectColsPE.add(", PE.payer_plan_period_end_date");
        }
        
        if (payerConcept != null) {
            selectCols.add(", Q.payer_concept_id");
            selectColsPE.add(", PE.payer_concept_id");
        }
        
        if (planConcept != null) {
            selectCols.add(", Q.plan_concept_id");
            selectColsPE.add(", PE.plan_concept_id");
        }
        
        if (sponsorConcept != null) {
            selectCols.add(", Q.sponsor_concept_id");
            selectColsPE.add(", PE.sponsor_concept_id");
        }
        
        if (stopReasonConcept != null) {
            selectCols.add(", Q.stop_reason_concept_id");
            selectColsPE.add(", PE.stop_reason_concept_id");
        }
        
        if (stopReasonConcept != null) {
            selectCols.add(", Q.stop_reason_concept_id");
            selectColsPE.add(", PE.stop_reason_concept_id");
        }
        
        if (payerSourceConcept != null) {
            selectCols.add(", Q.payer_source_concept_id");
            selectColsPE.add(", PE.payer_source_concept_id");
        }
        
        if (planSourceConcept != null) {
            selectCols.add(", Q.plan_source_concept_id");
            selectColsPE.add(", PE.plan_source_concept_id");
        }
        
        if (sponsorSourceConcept != null) {
            selectCols.add(", Q.sponsor_source_concept_id");
            selectColsPE.add(", PE.sponsor_source_concept_id");
        }
        
        if (stopReasonSourceConcept != null) {
            selectCols.add(", Q.stop_reason_source_concept_id");
            selectColsPE.add(", PE.stop_reason_source_concept_id");
        }
        
        query = StringUtils.replace(query, "@QAdditionalColumnsInclusionN", StringUtils.join(selectCols, ""));
        return query;
    }
}
