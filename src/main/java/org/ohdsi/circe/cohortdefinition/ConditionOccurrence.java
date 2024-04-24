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
  
  @Override
  public List<ColumnFieldData> getSelectedField(Boolean retainCohortCovariates) {
      List<ColumnFieldData> selectCols = new ArrayList<>();
      
      if (retainCohortCovariates) {
          if (conditionType != null && conditionType.length > 0) {
              selectCols.add(new ColumnFieldData("condition_type_concept_id", ColumnFieldDataType.INTEGER));
          }
          
          if (conditionSourceConcept != null) {
              selectCols.add(new ColumnFieldData("condition_source_concept_id", ColumnFieldDataType.INTEGER));
          }
          
          // providerSpecialty
          if (providerSpecialty != null && providerSpecialty.length > 0) {
              selectCols.add(new ColumnFieldData("provider_id", ColumnFieldDataType.INTEGER));
          }
          
          if (conditionStatus != null && conditionStatus.length > 0) {
              selectCols.add(new ColumnFieldData("condition_status_concept_id", ColumnFieldDataType.INTEGER));
          }
      }
      
      return selectCols;
  }
  
  @Override
  public String embedCriteriaGroup(String query) {
      ArrayList<String> selectColsCQ = new ArrayList<>();
      ArrayList<String> selectColsG = new ArrayList<>();
      
      if (conditionType != null && conditionType.length > 0) {
          selectColsCQ.add(", CQ.condition_type_concept_id");
          selectColsG.add(", G.condition_type_concept_id");
      }
      
      if (conditionSourceConcept != null) {
          selectColsCQ.add(", CQ.condition_source_concept_id");
          selectColsG.add(", G.condition_source_concept_id");
      }
      
      // providerSpecialty
      if (providerSpecialty != null && providerSpecialty.length > 0) {
          selectColsCQ.add(", CQ.provider_id");
          selectColsG.add(", G.provider_id");
      }
      
      if (conditionStatus != null && conditionStatus.length > 0) {
          selectColsCQ.add(", CQ.condition_status_concept_id");
          selectColsG.add(", G.condition_status_concept_id");
      }
      
      query = StringUtils.replace(query, "@e.additonColumns", StringUtils.join(selectColsCQ, ""));
      query = StringUtils.replace(query, "@additonColumnsGroup", StringUtils.join(selectColsG, ""));
      return query;
  }
  
  @Override
  public String embedWindowedCriteriaQuery(String query, Map<String, ColumnFieldData> mapDistinctField) {
      List<String> selectCols = new ArrayList<>();
      List<String> groupCols = new ArrayList<>();
      for (Entry<String, ColumnFieldData> entry : mapDistinctField.entrySet()) {
          if (entry.getKey().equals("condition_type_concept_id") && conditionType != null && conditionType.length > 0) {
              selectCols.add(", cc.condition_type_concept_id");
              groupCols.add(", cc.condition_type_concept_id");
          } else if (entry.getKey().equals("condition_source_concept_id") && conditionSourceConcept != null) {
              selectCols.add(", cc.condition_source_concept_id");
              groupCols.add(", cc.condition_source_concept_id");
          } else if (entry.getKey().equals("provider_id") && providerSpecialty != null && providerSpecialty.length > 0) {
              selectCols.add(", cc.provider_id");
              groupCols.add(", cc.provider_id");
          } else if (entry.getKey().equals("condition_status_concept_id") && conditionStatus != null && conditionStatus.length > 0) {
              selectCols.add(", cc.condition_status_concept_id");
              groupCols.add(", cc.condition_status_concept_id");
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
      
      if (conditionType != null && conditionType.length > 0) {
          selectColsA.add(", A.condition_type_concept_id");
      }
      
      if (conditionSourceConcept != null) {
          selectColsA.add(", A.condition_source_concept_id");
      }
      
      // providerSpecialty
      if (providerSpecialty != null && providerSpecialty.length > 0) {
          selectColsA.add(", A.provider_id");
      }
      
      if (conditionStatus != null && conditionStatus.length > 0) {
          selectColsA.add(", A.condition_status_concept_id");
      }
      
      query = StringUtils.replace(query, "@p.additionColumns", StringUtils.join(selectColsA, ""));
      return query;
  }
  
  @Override
  public String embedWrapCriteriaQuery(String query, List<String> selectColsPE, BuilderOptions options) {
      ArrayList<String> selectCols = new ArrayList<>();
      
      if(!options.isPrimaryCriteria()){
        if (conditionType != null && conditionType.length > 0) {
            selectCols.add(", Q.condition_type_concept_id");
            selectColsPE.add(", PE.condition_type_concept_id");
        }
        
        if (conditionSourceConcept != null) {
            selectCols.add(", Q.condition_source_concept_id");
            selectColsPE.add(", PE.condition_source_concept_id");
        }
        
        // providerSpecialty
        if (providerSpecialty != null && providerSpecialty.length > 0) {
            selectCols.add(", Q.provider_id");
            selectColsPE.add(", PE.provider_id");
        }
        
        if (conditionStatus != null && conditionStatus.length > 0) {
            selectCols.add(", Q.condition_status_concept_id");
            selectColsPE.add(", PE.condition_status_concept_id");
        }
      }
      
      query = StringUtils.replace(query, "@QAdditionalColumnsInclusionN", StringUtils.join(selectCols, ""));
      return query;
  }
}
