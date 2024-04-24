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
public class Observation extends Criteria {
  
  @JsonProperty("CodesetId")  
  public Integer codesetId;
  
  @JsonProperty("First")
  public Boolean first;
  
  @JsonProperty("OccurrenceStartDate")
  public DateRange occurrenceStartDate;

  @JsonProperty("ObservationType")
  public Concept[] observationType;
	
  @JsonProperty("ObservationTypeExclude")
  public boolean observationTypeExclude = false;

  @JsonProperty("ValueAsNumber")
  public NumericRange valueAsNumber;

  @JsonProperty("ValueAsString")
  public TextFilter valueAsString;

  @JsonProperty("ValueAsConcept")
  public Concept[] valueAsConcept;

  @JsonProperty("Qualifier")
  public Concept[] qualifier;
  
  @JsonProperty("Unit")
  public Concept[] unit;
   
  @JsonProperty("ObservationSourceConcept")
  public Integer observationSourceConcept;
  
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
          selectCols.add(new ColumnFieldData("value_as_number", ColumnFieldDataType.NUMERIC));
          
          // observationType
          if (observationType != null && observationType.length > 0) {
              selectCols.add(new ColumnFieldData("observation_type_concept_id", ColumnFieldDataType.INTEGER));
          }
          
          if (valueAsString != null) {
              selectCols.add(new ColumnFieldData("value_as_string", ColumnFieldDataType.VARCHAR));
          }
          
          if (valueAsConcept != null && valueAsConcept.length > 0) {
              selectCols.add(new ColumnFieldData("value_as_concept_id", ColumnFieldDataType.INTEGER));
          }
          // unit
          if (unit != null && unit.length > 0) {
              selectCols.add(new ColumnFieldData("unit_concept_id", ColumnFieldDataType.INTEGER));
          }
          
          // qualifier
          if (qualifier != null && qualifier.length > 0) {
              selectCols.add(new ColumnFieldData("qualifier_concept_id", ColumnFieldDataType.INTEGER));
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
      
      selectColsCQ.add(", CQ.value_as_number");
      selectColsG.add(", G.value_as_number");
      
      // observationType
      if (observationType != null && observationType.length > 0) {
          selectColsCQ.add(", CQ.observation_type_concept_id");
          selectColsG.add(", G.observation_type_concept_id");
      }
      
      if (valueAsString != null) {
          selectColsCQ.add(", CQ.value_as_string");
          selectColsG.add(", G.value_as_string");
      }
      
      if (valueAsConcept != null && valueAsConcept.length > 0) {
          selectColsCQ.add(", CQ.value_as_concept_id");
          selectColsG.add(", G.value_as_concept_id");
      }
      // unit
      if (unit != null && unit.length > 0) {
          selectColsCQ.add(", CQ.unit_concept_id");
          selectColsG.add(", G.unit_concept_id");
      }
      
      // qualifier
      if (qualifier != null && qualifier.length > 0) {
          selectColsCQ.add(", CQ.qualifier_concept_id");
          selectColsG.add(", G.qualifier_concept_id");
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
      selectGroupCols.add(", cc.value_as_number");
      
      for (Entry<String, ColumnFieldData> entry : mapDistinctField.entrySet()) {
          if (entry.getKey().equals("value_as_number")) {
              selectCols.add(", cc.value_as_number");
              groupCols.add(", cc.value_as_number");
          } else if (entry.getKey().equals("observation_type_concept_id") && observationType != null && observationType.length > 0) {
              selectCols.add(", cc.observation_type_concept_id");
              groupCols.add(", cc.observation_type_concept_id");
          } else if (entry.getKey().equals("value_as_string") && valueAsString != null) {
              selectCols.add(", cc.value_as_string");
              groupCols.add(", cc.value_as_string");
          } else if (entry.getKey().equals("value_as_concept_id") && valueAsConcept != null && valueAsConcept.length > 0) {
              selectCols.add(", cc.value_as_concept_id");
              groupCols.add(", cc.value_as_concept_id");
          } else if (entry.getKey().equals("unit_concept_id") && unit != null && unit.length > 0) {
              selectCols.add(", cc.unit_concept_id");
              groupCols.add(", cc.unit_concept_id");
          } else if (entry.getKey().equals("qualifier_concept_id") && qualifier != null && qualifier.length > 0) {
              selectCols.add(", cc.qualifier_concept_id");
              groupCols.add(", cc.qualifier_concept_id");
          } else if (entry.getKey().equals("provider_id") && providerSpecialty != null && providerSpecialty.length > 0) {
              selectCols.add(", cc.provider_id");
              groupCols.add(", cc.provider_id");
          } else {
              selectCols.add(", CAST(null as " + entry.getValue().getDataType().getType() + ") " + entry.getKey());
          }
          selectGroupCols.add(", cc.observation_type_concept_id");
      }
      
      query = StringUtils.replace(query, "@additionColumnscc", StringUtils.join(selectCols, ""));
      query = StringUtils.replace(query, "@additionGroupColumnscc", StringUtils.join(groupCols, ""));
      return query;
  }
  
  @Override
  public String embedWindowedCriteriaQueryP(String query) {
      ArrayList<String> selectColsA = new ArrayList<>();
      selectColsA.add(", A.value_as_number");
      // observationType
      if (observationType != null && observationType.length > 0) {
          selectColsA.add(", A.observation_type_concept_id");
      }
      
      if (valueAsString != null) {
          selectColsA.add(", A.value_as_string");
      }
      
      if (valueAsConcept != null && valueAsConcept.length > 0) {
          selectColsA.add(", A.value_as_concept_id");
      }
      // unit
      if (unit != null && unit.length > 0) {
          selectColsA.add(", A.unit_concept_id");
      }
      
      // qualifier
      if (qualifier != null && qualifier.length > 0) {
          selectColsA.add(", A.qualifier_concept_id");
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
        selectCols.add(", Q.value_as_number");
        selectColsPE.add(", PE.value_as_number");
        
        // observationType
        if (observationType != null && observationType.length > 0) {
            selectCols.add(", Q.observation_type_concept_id");
            selectColsPE.add(", PE.observation_type_concept_id");
        }
        
        if (valueAsString != null) {
            selectCols.add(", Q.value_as_string");
            selectColsPE.add(", PE.value_as_string");
        }
        
        if (valueAsConcept != null && valueAsConcept.length > 0) {
            selectCols.add(", Q.value_as_concept_id");
            selectColsPE.add(", PE.value_as_concept_id");
        }
        // unit
        if (unit != null && unit.length > 0) {
            selectCols.add(", Q.unit_concept_id");
            selectColsPE.add(", PE.unit_concept_id");
        }
        
        // qualifier
        if (qualifier != null && qualifier.length > 0) {
            selectCols.add(", Q.qualifier_concept_id");
            selectColsPE.add(", PE.qualifier_concept_id");
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
