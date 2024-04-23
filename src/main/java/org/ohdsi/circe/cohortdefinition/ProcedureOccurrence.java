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
public class ProcedureOccurrence extends Criteria {
  
  @JsonProperty("CodesetId")  
  public Integer codesetId;
  
  @JsonProperty("First")
  public Boolean first;
  
  @JsonProperty("OccurrenceStartDate")
  public DateRange occurrenceStartDate;

  @JsonProperty("ProcedureType")
  public Concept[] procedureType;
	
  @JsonProperty("ProcedureTypeExclude")
  public boolean procedureTypeExclude = false;

  @JsonProperty("Modifier")
  public Concept[] modifier;

  @JsonProperty("Quantity")
  public NumericRange quantity;
  
  @JsonProperty("ProcedureSourceConcept")
  public Integer procedureSourceConcept;
  
  @JsonProperty("Age")
  public NumericRange age;
  
  @JsonProperty("Gender")
  public Concept[] gender;
  
  @JsonProperty("ProviderSpecialty")
  public Concept[] providerSpecialty;

  @JsonProperty("VisitType")
  public Concept[] visitType;

  @Override
  public String accept(IGetCriteriaSqlDispatcher dispatcher, BuilderOptions options) {
    return dispatcher.getCriteriaSql(this, options);
  }

  @Override
  public List<ColumnFieldData> getSelectedField(Boolean retainCohortCovariates) {
      List<ColumnFieldData> selectCols = new ArrayList<>();
      
      if (retainCohortCovariates) {
          if (procedureType != null && procedureType.length > 0) {
              selectCols.add(new ColumnFieldData("procedure_type_concept_id", ColumnFieldDataType.INTEGER));
          }
          
          if (modifier != null && modifier.length > 0) {
              selectCols.add(new ColumnFieldData("modifier_concept_id", ColumnFieldDataType.INTEGER));
          }
          
          if (quantity != null) {
              selectCols.add(new ColumnFieldData("quantity", ColumnFieldDataType.INTEGER));
          }
          
          if (procedureSourceConcept != null) {
              selectCols.add(new ColumnFieldData("procedure_source_concept_id", ColumnFieldDataType.INTEGER));
          }
          
          // providerSpecialty
          if (providerSpecialty != null && providerSpecialty.length > 0) {
              selectCols.add(new ColumnFieldData("provider_id", ColumnFieldDataType.INTEGER));
          }
      }
      
      return selectCols;
  }
  
  @Override
  public String embedCriteriaGroup(String query) {
      ArrayList<String> selectColsCQ = new ArrayList<>();
      ArrayList<String> selectColsG = new ArrayList<>();
      
      if (procedureType != null && procedureType.length > 0) {
          selectColsCQ.add(", CQ.procedure_type_concept_id");
          selectColsG.add(", G.procedure_type_concept_id");
      }
      
      if (modifier != null && modifier.length > 0) {
          selectColsCQ.add(", CQ.modifier_concept_id");
          selectColsG.add(", G.modifier_concept_id");
      }
      
      if (quantity != null) {
          selectColsCQ.add(", CQ.quantity");
          selectColsG.add(", G.quantity");
      }
      
      if (procedureSourceConcept != null) {
          selectColsCQ.add(", CQ.procedure_source_concept_id");
          selectColsG.add(", G.procedure_source_concept_id");
      }
      
      // providerSpecialty
      if (providerSpecialty != null && providerSpecialty.length > 0) {
          selectColsCQ.add(", CQ.provider_id");
          selectColsG.add(", G.provider_id");
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
          if (entry.getKey().equals("procedure_type_concept_id") && procedureType != null && procedureType.length > 0) {
              selectCols.add(", cc.procedure_type_concept_id");
              groupCols.add(", cc.procedure_type_concept_id");
          } else if (entry.getKey().equals("modifier_concept_id") && modifier != null && modifier.length > 0) {
              selectCols.add(", cc.modifier_concept_id");
              groupCols.add(", cc.modifier_concept_id");
          } else if (entry.getKey().equals("quantity") && quantity != null) {
              selectCols.add(", cc.quantity");
              groupCols.add(", cc.quantity");
          } else if (entry.getKey().equals("procedure_source_concept_id") && procedureSourceConcept != null) {
              selectCols.add(", cc.procedure_source_concept_id");
              groupCols.add(", cc.procedure_source_concept_id");
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
      
      if (procedureType != null && procedureType.length > 0) {
          selectColsA.add(", A.procedure_type_concept_id");
      }
      
      if (modifier != null && modifier.length > 0) {
          selectColsA.add(", A.modifier_concept_id");
      }
      
      if (quantity != null) {
          selectColsA.add(", A.quantity");
      }
      
      if (procedureSourceConcept != null) {
          selectColsA.add(", A.procedure_source_concept_id");
      }
      
      // providerSpecialty
      if (providerSpecialty != null && providerSpecialty.length > 0) {
          selectColsA.add(", A.provider_id");
      }
      
      query = StringUtils.replace(query, "@p.additionColumns", StringUtils.join(selectColsA, ""));
      return query;
  }
  
  @Override
  public String embedWrapCriteriaQuery(String query, List<String> selectColsPE) {
      ArrayList<String> selectCols = new ArrayList<>();

      if (procedureType != null && procedureType.length > 0) {
          selectCols.add(", Q.procedure_type_concept_id");
          selectColsPE.add(", PE.procedure_type_concept_id");
      }
      
      if (modifier != null && modifier.length > 0) {
          selectCols.add(", Q.modifier_concept_id");
          selectColsPE.add(", PE.modifier_concept_id");
      }
      
      if (quantity != null) {
          selectCols.add(", Q.quantity");
          selectColsPE.add(", PE.quantity");
      }
      
      if (procedureSourceConcept != null) {
          selectCols.add(", Q.procedure_source_concept_id");
          selectColsPE.add(", PE.procedure_source_concept_id");
      }
      
      // providerSpecialty
      if (providerSpecialty != null && providerSpecialty.length > 0) {
          selectCols.add(", Q.provider_id");
          selectColsPE.add(", PE.provider_id");
      }
      
      query = StringUtils.replace(query, "@QAdditionalColumnsInclusionN", StringUtils.join(selectCols, ""));
      return query;
  }
}
