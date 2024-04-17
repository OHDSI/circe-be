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

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.builders.BuilderOptions;
import org.ohdsi.circe.cohortdefinition.builders.ColumnFieldData;
import org.ohdsi.circe.cohortdefinition.builders.ColumnFieldDataType;
import org.ohdsi.circe.vocabulary.Concept;

/**
 *
 * @author cknoll1
 */
public class ConditionEra extends Criteria {

  @JsonProperty("CodesetId")  
  public Integer codesetId;

  @JsonProperty("First")  
  public Boolean first;
  
  @JsonProperty("EraStartDate")  
  public DateRange eraStartDate;

  @JsonProperty("EraEndDate")  
  public DateRange eraEndDate;

  @JsonProperty("OccurrenceCount")  
  public NumericRange occurrenceCount;

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
  public List<ColumnFieldData> getSelectedField(BuilderOptions options) {
      List<ColumnFieldData> selectCols = new ArrayList<>();
      
      if (occurrenceCount != null) {
          selectCols.add(new ColumnFieldData("condition_occurrence_count", ColumnFieldDataType.INTEGER));
      }
      
      return selectCols;
  }
  
  @Override
  public String embedCriteriaGroup(String query) {
      ArrayList<String> selectColsCQ = new ArrayList<>();
      ArrayList<String> selectColsG = new ArrayList<>();
      
      if (occurrenceCount != null) {
          selectColsCQ.add(", CQ.condition_occurrence_count");
          selectColsG.add(", G.condition_occurrence_count");
      }
      
      query = StringUtils.replace(query, "@e.additonColumns", StringUtils.join(selectColsCQ, ""));
      query = StringUtils.replace(query, "@additonColumnsGroup", StringUtils.join(selectColsG, ""));
      return query;
  }
  
  @Override
  public String embedWindowedCriteriaQuery(String query) {
      ArrayList<String> selectCols = new ArrayList<>();
      
      if (occurrenceCount != null) {
          selectCols.add(", cc.condition_occurrence_count");
      }
      
      query = StringUtils.replace(query, "@additionColumnscc", StringUtils.join(selectCols, ""));
      return query;
  }
  
  @Override
  public String embedWindowedCriteriaQueryP(String query) {
      ArrayList<String> selectColsA = new ArrayList<>();
      
      if (occurrenceCount != null) {
          selectColsA.add(", A.condition_occurrence_count");
      }
      
      query = StringUtils.replace(query, "@p.additionColumns", StringUtils.join(selectColsA, ""));
      return query;
  }
  
  @Override
  public String embedWrapCriteriaQuery(String query, List<String> selectColsPE) {
      ArrayList<String> selectCols = new ArrayList<>();
      
      if (occurrenceCount != null) {
          selectCols.add(", Q.condition_occurrence_count");
          selectColsPE.add(", PE.condition_occurrence_count");
      }
      
      query = StringUtils.replace(query, "@QAdditionalColumnsInclusionN", StringUtils.join(selectCols, ""));
      return query;
  }
}
