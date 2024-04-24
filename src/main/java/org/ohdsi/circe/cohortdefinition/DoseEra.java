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
public class DoseEra extends Criteria {
 
  @JsonProperty("CodesetId")  
  public Integer codesetId;

  @JsonProperty("First")  
  public Boolean first;
  
  @JsonProperty("EraStartDate")  
  public DateRange eraStartDate;

  @JsonProperty("EraEndDate")  
  public DateRange eraEndDate;

  @JsonProperty("Unit")  
  public Concept[] unit;

  @JsonProperty("DoseValue")  
  public NumericRange doseValue;

  @JsonProperty("EraLength")  
  public NumericRange eraLength;

  @JsonProperty("AgeAtStart")
  public NumericRange ageAtStart;  

  @JsonProperty("AgeAtEnd")
  public NumericRange ageAtEnd;  

  @JsonProperty("Gender")
  public Concept[] gender;  
  
  @Override
  public String accept(IGetCriteriaSqlDispatcher dispatcher, BuilderOptions options) {
    return dispatcher.getCriteriaSql(this, options);
  }  
  
  @Override
  public List<ColumnFieldData> getSelectedField(Boolean retainCohortCovariates) {
      List<ColumnFieldData> selectCols = new ArrayList<>();
      
      if (retainCohortCovariates) {
          if (eraStartDate != null) {
              selectCols.add(new ColumnFieldData("dose_era_start_date", ColumnFieldDataType.DATE));
          }
          
          if (eraEndDate != null) {
              selectCols.add(new ColumnFieldData("dose_era_end_date", ColumnFieldDataType.DATE));
          }
          
          // unit
          if (unit != null && unit.length > 0) {
              selectCols.add(new ColumnFieldData("unit_concept_id", ColumnFieldDataType.DATE));
          }
          
          if (doseValue != null) {
              selectCols.add(new ColumnFieldData("dose_value", ColumnFieldDataType.NUMERIC));
          }
      }
      return selectCols;
  }
  
  @Override
  public String embedCriteriaGroup(String query) {
      ArrayList<String> selectColsCQ = new ArrayList<>();
      ArrayList<String> selectColsG = new ArrayList<>();
      
      if (eraStartDate != null) {
          selectColsCQ.add(", CQ.dose_era_start_date");
          selectColsG.add(", G.dose_era_start_date");
      }
      
      if (eraEndDate != null) {
          selectColsCQ.add(", CQ.dose_era_end_date");
          selectColsG.add(", G.dose_era_end_date");
      }
      
      // unit
      if (unit != null && unit.length > 0) {
          selectColsCQ.add(", CQ.unit_concept_id");
          selectColsG.add(", G.unit_concept_id");
      }
      
      if (doseValue != null) {
          selectColsCQ.add(", CQ.dose_value");
          selectColsG.add(", G.dose_value");
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
          if (entry.getKey().equals("dose_era_start_date") && eraStartDate != null) {
              selectCols.add(", cc.dose_era_start_date");
              groupCols.add(", cc.dose_era_start_date");
          } else if (entry.getKey().equals("dose_era_end_date") && eraEndDate != null) {
              selectCols.add(", cc.dose_era_end_date");
              groupCols.add(", cc.dose_era_end_date");
          } else if (entry.getKey().equals("unit_concept_id") && unit != null && unit.length > 0) {
              selectCols.add(", cc.unit_concept_id");
              groupCols.add(", cc.unit_concept_id");
          } else if (entry.getKey().equals("dose_value") && doseValue != null) {
              selectCols.add(", cc.dose_value");
              groupCols.add(", cc.dose_value");
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
      
      if (eraStartDate != null) {
          selectColsA.add(", A.dose_era_start_date");
      }
      
      if (eraEndDate != null) {
          selectColsA.add(", A.dose_era_end_date");
      }
      
      // unit
      if (unit != null && unit.length > 0) {
          selectColsA.add(", A.unit_concept_id");
      }
      
      if (doseValue != null) {
          selectColsA.add(", A.dose_value");
      }
      
      query = StringUtils.replace(query, "@p.additionColumns", StringUtils.join(selectColsA, ""));
      return query;
  }
  
  @Override
  public String embedWrapCriteriaQuery(String query, List<String> selectColsPE, BuilderOptions options) {
      ArrayList<String> selectCols = new ArrayList<>();
      
      if(!options.isPrimaryCriteria()){
        if (eraStartDate != null) {
            selectCols.add(", Q.dose_era_start_date");
            selectColsPE.add(", PE.dose_era_start_date");
        }
        
        if (eraEndDate != null) {
            selectCols.add(", Q.dose_era_end_date");
            selectColsPE.add(", PE.dose_era_end_date");
        }
        
        // unit
        if (unit != null && unit.length > 0) {
            selectCols.add(", Q.unit_concept_id");
            selectColsPE.add(", PE.unit_concept_id");
        }
        
        if (doseValue != null) {
            selectCols.add(", Q.dose_value");
            selectColsPE.add(", PE.dose_value");
        }
      }
      
      query = StringUtils.replace(query, "@QAdditionalColumnsInclusionN", StringUtils.join(selectCols, ""));
      return query;
  }
}
