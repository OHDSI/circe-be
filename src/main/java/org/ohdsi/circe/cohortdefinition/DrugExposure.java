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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.builders.BuilderOptions;
import org.ohdsi.circe.cohortdefinition.builders.ColumnFieldData;
import org.ohdsi.circe.cohortdefinition.builders.ColumnFieldDataType;
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

  @JsonProperty("EffectiveDrugDose")
  public NumericRange effectiveDrugDose;  

  @JsonProperty("DoseUnit")
  public Concept[] doseUnit;

  @JsonProperty("LotNumber")
  public TextFilter lotNumber;  

  @JsonProperty("DrugSourceConcept")
  public Integer drugSourceConcept;
  
  @JsonProperty("Age")
  public NumericRange age;
  
  @JsonProperty("Gender")
  public Concept[] gender;
  
  @JsonProperty("ProviderSpecialty")
  public Concept[] providerSpecialty;

  @JsonProperty("VisitType")
  public Concept[] visitType;

  @Override
  public String accept(IGetCriteriaSqlDispatcher dispatcher, BuilderOptions options)
  {
    return dispatcher.getCriteriaSql(this, options);
  }
 
  @Override
  public List<ColumnFieldData> getSelectedField(Boolean retainCohortCovariates) {
      List<ColumnFieldData> selectCols = new ArrayList<>();
      
      if (retainCohortCovariates) {
          if (drugType != null && drugType.length > 0) {
              selectCols.add(new ColumnFieldData("drug_type_concept_id", ColumnFieldDataType.INTEGER));
          }
          
          if (stopReason != null) {
              selectCols.add(new ColumnFieldData("stop_reason", ColumnFieldDataType.VARCHAR));
          }
          
          if (refills != null) {
              selectCols.add(new ColumnFieldData("refills", ColumnFieldDataType.INTEGER));
          }
          
          if (quantity != null) {
              selectCols.add(new ColumnFieldData("quantity", ColumnFieldDataType.INTEGER));
          }
          
          if (daysSupply != null) {
              selectCols.add(new ColumnFieldData("days_supply", ColumnFieldDataType.INTEGER));
          }
          
          if (routeConcept != null && routeConcept.length > 0) {
              selectCols.add(new ColumnFieldData("route_concept_id", ColumnFieldDataType.INTEGER));
          }
          
          if (lotNumber != null) {
              selectCols.add(new ColumnFieldData("lot_number", ColumnFieldDataType.VARCHAR));
          }
          
          if (drugSourceConcept != null) {
              selectCols.add(new ColumnFieldData("drug_source_concept_id", ColumnFieldDataType.INTEGER));
          }
          
          // providerSpecialty
          if (providerSpecialty != null && providerSpecialty.length > 0) {
              selectCols.add(new ColumnFieldData("provider_id", ColumnFieldDataType.INTEGER));
          }
      }
      return selectCols;
  }
  
  @Override
  public String embedWindowedCriteriaQuery(String query, Map<String, ColumnFieldData> mapDistinctField) {
      List<String> selectCols = new ArrayList<>();
      List<String> groupCols = new ArrayList<>();
      
      for (Entry<String, ColumnFieldData> entry : mapDistinctField.entrySet()) {
          if (entry.getKey().equals("drug_type_concept_id") && drugType != null && drugType.length > 0) {
              selectCols.add(", cc.drug_type_concept_id");
              groupCols.add(", cc.drug_type_concept_id");
          } else if (entry.getKey().equals("stop_reason") && stopReason != null) {
              selectCols.add(", cc.stop_reason");
              groupCols.add(", cc.stop_reason");
          } else if (entry.getKey().equals("refills") && refills != null) {
              selectCols.add(", cc.refills");
              groupCols.add(", cc.refills");
          } else if (entry.getKey().equals("quantity") && quantity != null) {
              selectCols.add(", cc.quantity");
              groupCols.add(", cc.quantity");
          } else if (entry.getKey().equals("days_supply") && daysSupply != null) {
              selectCols.add(", cc.days_supply");
              groupCols.add(", cc.days_supply");
          } else if (entry.getKey().equals("route_concept_id") && routeConcept != null && routeConcept.length > 0) {
              selectCols.add(", cc.route_concept_id");
              groupCols.add(", cc.route_concept_id");
          } else if (entry.getKey().equals("lot_number") && lotNumber != null) {
              selectCols.add(", cc.lot_number");
              groupCols.add(", cc.lot_number");
          } else if (entry.getKey().equals("drug_source_concept_id") && drugSourceConcept != null) {
              selectCols.add(", cc.drug_source_concept_id");
              groupCols.add(", cc.drug_source_concept_id");
          } else if (entry.getKey().equals("provider_id") && providerSpecialty != null && providerSpecialty.length > 0) {
              selectCols.add(", cc.provider_id");
              groupCols.add(", cc.provider_id");
          } else {
              selectCols.add(", CAST(null as " + entry.getValue().getDataType().getType() + ") " + entry.getKey());
          }
      }
      
      query = StringUtils.replace(query, "@additionColumnscc", StringUtils.join(selectCols, ""));
      query = StringUtils.replace(query, "@additionGroupColumnscc", StringUtils.join(groupCols, ""));
      return query;
  }
  
  @Override
  public String embedWindowedCriteriaQueryP(String query) {
      ArrayList<String> selectColsA = new ArrayList<>();
      
      if (drugType != null && drugType.length > 0) {
          selectColsA.add(", A.drug_type_concept_id");
      }
      
      if (stopReason != null) {
          selectColsA.add(", A.stop_reason");
      }
      
      if (refills != null) {
          selectColsA.add(", A.refills");
      }
      
      if (quantity != null) {
          selectColsA.add(", A.quantity");
      }
      
      if (daysSupply != null) {
          selectColsA.add(", A.days_supply");
      }
      
      if (routeConcept != null && routeConcept.length > 0) {
          selectColsA.add(", A.route_concept_id");
      }
      
      if (lotNumber != null) {
          selectColsA.add(", A.lot_number");
      }
      
      if (drugSourceConcept != null) {
          selectColsA.add(", A.drug_source_concept_id");
      }
      
      // providerSpecialty
      if (providerSpecialty != null && providerSpecialty.length > 0) {
          selectColsA.add(", A.provider_id");
      }
      
      query = StringUtils.replace(query, "@p.additionColumns", StringUtils.join(selectColsA, ""));
      return query;
  }
  
  @Override
  public String embedWrapCriteriaQuery(String query, List<String> selectColsPE, BuilderOptions options) {
      ArrayList<String> selectCols = new ArrayList<>();
      
      if(!options.isPrimaryCriteria()){
        if (drugType != null && drugType.length > 0) {
            selectCols.add(", Q.drug_type_concept_id");
            selectColsPE.add(", PE.drug_type_concept_id");
        }
        
        if (stopReason != null) {
            selectCols.add(", Q.stop_reason");
            selectColsPE.add(", PE.stop_reason");
        }
        
        if (refills != null) {
            selectCols.add(", Q.refills");
            selectColsPE.add(", PE.refills");
        }
        
        if (quantity != null) {
            selectCols.add(", Q.quantity");
            selectColsPE.add(", PE.quantity");
        }
        
        if (daysSupply != null) {
            selectCols.add(", Q.days_supply");
            selectColsPE.add(", PE.days_supply");
        }
        
        if (routeConcept != null && routeConcept.length > 0) {
            selectCols.add(", Q.route_concept_id");
            selectColsPE.add(", PE.route_concept_id");
        }
        
        if (lotNumber != null) {
            selectCols.add(", Q.lot_number");
            selectColsPE.add(", PE.lot_number");
        }
        
        if (drugSourceConcept != null) {
            selectCols.add(", Q.drug_source_concept_id");
            selectColsPE.add(", PE.drug_source_concept_id");
        }
        
        // providerSpecialty
        if (providerSpecialty != null && providerSpecialty.length > 0) {
            selectCols.add(", Q.provider_id");
            selectColsPE.add(", PE.provider_id");
        }
      }
      
      query = StringUtils.replace(query, "@QAdditionalColumnsInclusionN", StringUtils.join(selectCols, ""));
      return query;
  }
}
