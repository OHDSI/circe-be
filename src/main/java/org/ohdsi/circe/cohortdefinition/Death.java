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
public class Death extends Criteria {
  @JsonProperty("CodesetId")
  public Integer codesetId;

  @JsonProperty("OccurrenceStartDate")
  public DateRange occurrenceStartDate;

  @JsonProperty("DeathType")
  public Concept[] deathType;

  @JsonProperty("DeathTypeExclude")
  public boolean deathTypeExclude = false;

  @JsonProperty("DeathSourceConcept")
  public Integer deathSourceConcept;

  @JsonProperty("Age")
  public NumericRange age;
  
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
          if (occurrenceStartDate != null) {
              selectCols.add(new ColumnFieldData("death_date", ColumnFieldDataType.DATE));
          }
          
          if (deathType != null && deathType.length > 0) {
              selectCols.add(new ColumnFieldData("death_type_concept_id", ColumnFieldDataType.INTEGER));
          }
          
          if (deathSourceConcept != null) {
              selectCols.add(new ColumnFieldData("cause_concept_id", ColumnFieldDataType.INTEGER));
          }
      }
      return selectCols;
  }
  
  @Override
  public String embedCriteriaGroup(String query) {
      ArrayList<String> selectColsCQ = new ArrayList<>();
      ArrayList<String> selectColsG = new ArrayList<>();
      
      if (occurrenceStartDate != null) {
          selectColsCQ.add(", CQ.death_date");
          selectColsG.add(", G.death_date");
      }
      
      if (deathType != null && deathType.length > 0) {
          selectColsCQ.add(", CQ.death_type_concept_id");
          selectColsG.add(", G.death_type_concept_id");
      }
      
      if (deathSourceConcept != null) {
          selectColsCQ.add(", CQ.cause_concept_id");
          selectColsG.add(", G.cause_concept_id");
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
          if (entry.getKey().equals("death_date") && occurrenceStartDate != null) {
              selectCols.add(", cc.death_date");
              groupCols.add(", cc.death_date");
          } else if (entry.getKey().equals("death_type_concept_id") && deathType != null && deathType.length > 0) {
              selectCols.add(", cc.death_type_concept_id");
              groupCols.add(", cc.death_type_concept_id");
          } else if (entry.getKey().equals("cause_concept_id") && deathSourceConcept != null) {
              selectCols.add(", cc.cause_concept_id");
              groupCols.add(", cc.cause_concept_id");
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
          selectColsA.add(", A.death_date");
      }
      
      if (deathType != null && deathType.length > 0) {
          selectColsA.add(", A.death_type_concept_id");
      }
      
      if (deathSourceConcept != null) {
          selectColsA.add(", A.cause_concept_id");
      }
      
      query = StringUtils.replace(query, "@p.additionColumns", StringUtils.join(selectColsA, ""));
      return query;
  }
  
  @Override
  public String embedWrapCriteriaQuery(String query, List<String> selectColsPE) {
      ArrayList<String> selectCols = new ArrayList<>();
      
      if (occurrenceStartDate != null) {
          selectCols.add(", Q.death_date");
          selectColsPE.add(", PE.death_date");
      }
      
      if (deathType != null && deathType.length > 0) {
          selectCols.add(", Q.death_type_concept_id");
          selectColsPE.add(", PE.death_type_concept_id");
      }
      
      if (deathSourceConcept != null) {
          selectCols.add(", Q.cause_concept_id");
          selectColsPE.add(", PE.cause_concept_id");
      }
      
      query = StringUtils.replace(query, "@QAdditionalColumnsInclusionN", StringUtils.join(selectCols, ""));
      return query;
  }
}
