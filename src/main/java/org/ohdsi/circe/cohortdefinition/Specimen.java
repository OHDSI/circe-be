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
public class Specimen extends Criteria {
  
  @JsonProperty("CodesetId")  
  public Integer codesetId;

  @JsonProperty("First")
  public Boolean first;

  @JsonProperty("OccurrenceStartDate")  
  public DateRange occurrenceStartDate;
  
  @JsonProperty("SpecimenType")  
  public Concept[] specimenType;
	
  @JsonProperty("SpecimenTypeExclude")
  public boolean specimenTypeExclude = false;

  @JsonProperty("Quantity")  
  public NumericRange quantity;

  @JsonProperty("Unit")  
  public Concept[] unit;

  @JsonProperty("AnatomicSite")  
  public Concept[] anatomicSite;

  @JsonProperty("DiseaseStatus")  
  public Concept[] diseaseStatus;
  
  @JsonProperty("SourceId")  
  public TextFilter sourceId;
  
  @JsonProperty("SpecimenSourceConcept")  
  public Integer specimenSourceConcept;

  @JsonProperty("Age")
  public NumericRange age;
  
  @JsonProperty("Gender")
  public Concept[] gender;

  @Override
  public String accept(IGetCriteriaSqlDispatcher dispatcher, BuilderOptions options)
  {
    return dispatcher.getCriteriaSql(this, options);
  }  
  
  @Override
  public List<ColumnFieldData> getSelectedField(Boolean retainCohortCovariates) {
      List<ColumnFieldData> selectCols = new ArrayList<>();
      
      if (retainCohortCovariates) {
          if (occurrenceStartDate != null) {
              selectCols.add(new ColumnFieldData("specimen_date", ColumnFieldDataType.DATE));
          }
          
          if (specimenType != null && specimenType.length > 0) {
              selectCols.add(new ColumnFieldData("specimen_type_concept_id", ColumnFieldDataType.INTEGER));
          }
          
          if (unit != null && unit.length > 0) {
              selectCols.add(new ColumnFieldData("unit_concept_id", ColumnFieldDataType.INTEGER));
          }
          
          if (quantity != null) {
              selectCols.add(new ColumnFieldData("quantity", ColumnFieldDataType.INTEGER));
          }
          
          if (anatomicSite != null && anatomicSite.length > 0) {
              selectCols.add(new ColumnFieldData("anatomic_site_concept_id", ColumnFieldDataType.INTEGER));
          }
          
          if (diseaseStatus != null && diseaseStatus.length > 0) {
              selectCols.add(new ColumnFieldData("disease_status_concept_id", ColumnFieldDataType.INTEGER));
          }
          
          if (sourceId != null) {
              selectCols.add(new ColumnFieldData("specimen_source_id", ColumnFieldDataType.VARCHAR));
          }
      }
      return selectCols;
  }
  
  @Override
  public String embedWindowedCriteriaQuery(String query, Map<String, ColumnFieldData> mapDistinctField) {
      List<String> selectCols = new ArrayList<>();
      List<String> groupCols = new ArrayList<>();
      
      for (Entry<String, ColumnFieldData> entry : mapDistinctField.entrySet()) {
          if (entry.getKey().equals("specimen_date") && occurrenceStartDate != null) {
              selectCols.add(", cc.specimen_date");
              groupCols.add(", cc.specimen_date");
          } else if (entry.getKey().equals("specimen_type_concept_id") && specimenType != null && specimenType.length > 0) {
              selectCols.add(", cc.specimen_type_concept_id");
              groupCols.add(", cc.specimen_type_concept_id");
          } else if (entry.getKey().equals("unit_concept_id") && unit != null && unit.length > 0) {
              selectCols.add(", cc.unit_concept_id");
              groupCols.add(", cc.unit_concept_id");
          } else if (entry.getKey().equals("quantity") && quantity != null) {
              selectCols.add(", cc.quantity");
              groupCols.add(", cc.quantity");
          } else if (entry.getKey().equals("anatomic_site_concept_id") && anatomicSite != null && anatomicSite.length > 0) {
              selectCols.add(", cc.anatomic_site_concept_id");
              groupCols.add(", cc.anatomic_site_concept_id");
          } else if (entry.getKey().equals("disease_status_concept_id") && diseaseStatus != null && diseaseStatus.length > 0) {
              selectCols.add(", cc.disease_status_concept_id");
              groupCols.add(", cc.disease_status_concept_id");
          } else if (entry.getKey().equals("specimen_source_id") && sourceId != null) {
              selectCols.add(", cc.specimen_source_id");
              groupCols.add(", cc.specimen_source_id");
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
      
      if (occurrenceStartDate != null) {
          selectColsA.add(", A.specimen_date");
      }
      
      if (specimenType != null && specimenType.length > 0) {
          selectColsA.add(", A.specimen_type_concept_id");
      }
      
      if (unit != null && unit.length > 0) {
          selectColsA.add(", A.unit_concept_id");
      }
      
      if (quantity != null) {
          selectColsA.add(", A.quantity");
      }
      
      if (anatomicSite != null && anatomicSite.length > 0) {
          selectColsA.add(", A.anatomic_site_concept_id");
      }
      
      if (diseaseStatus != null && diseaseStatus.length > 0) {
          selectColsA.add(", A.disease_status_concept_id");
      }
      
      if (sourceId != null) {
          selectColsA.add(", A.specimen_source_id");
      }
      
      query = StringUtils.replace(query, "@p.additionColumns", StringUtils.join(selectColsA, ""));
      return query;
  }
  
  @Override
  public String embedWrapCriteriaQuery(String query, List<String> selectColsPE, BuilderOptions options) {
      ArrayList<String> selectCols = new ArrayList<>();
      
      if(!options.isPrimaryCriteria()){
        if (occurrenceStartDate != null) {
            selectCols.add(", Q.specimen_date");
            selectColsPE.add(", PE.specimen_date");
        }
        
        if (specimenType != null && specimenType.length > 0) {
            selectCols.add(", Q.specimen_type_concept_id");
            selectColsPE.add(", PE.specimen_type_concept_id");
        }
        
        if (unit != null && unit.length > 0) {
            selectCols.add(", Q.unit_concept_id");
            selectColsPE.add(", PE.unit_concept_id");
        }
        
        if (quantity != null) {
            selectCols.add(", Q.quantity");
            selectColsPE.add(", PE.quantity");
        }
        
        if (anatomicSite != null && anatomicSite.length > 0) {
            selectCols.add(", Q.anatomic_site_concept_id");
            selectColsPE.add(", PE.anatomic_site_concept_id");
        }
        
        if (diseaseStatus != null && diseaseStatus.length > 0) {
            selectCols.add(", Q.disease_status_concept_id");
            selectColsPE.add(", PE.disease_status_concept_id");
        }
        
        if (sourceId != null) {
            selectCols.add(", Q.specimen_source_id");
            selectColsPE.add(", PE.specimen_source_id");
        }
      }
      query = StringUtils.replace(query, "@QAdditionalColumnsInclusionN", StringUtils.join(selectCols, ""));
      return query;
  }
}
