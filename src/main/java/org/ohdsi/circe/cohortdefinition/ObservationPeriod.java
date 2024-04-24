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
public class ObservationPeriod extends Criteria {
  
  @JsonProperty("First")
  public Boolean first;

  @JsonProperty("PeriodStartDate")
  public DateRange periodStartDate;

  @JsonProperty("PeriodEndDate")
  public DateRange periodEndDate;
  
  @JsonProperty("UserDefinedPeriod")
  public Period userDefinedPeriod;

  @JsonProperty("PeriodType")
  public Concept[] periodType;
  
  @JsonProperty("PeriodLength")
  public NumericRange periodLength;  

  @JsonProperty("AgeAtStart")
  public NumericRange ageAtStart;  

  @JsonProperty("AgeAtEnd")
  public NumericRange ageAtEnd;  
  
  
  @Override
  public String accept(IGetCriteriaSqlDispatcher dispatcher, BuilderOptions options) {
    return dispatcher.getCriteriaSql(this, options);
  }  
  
  @Override
  public List<ColumnFieldData> getSelectedField(Boolean retainCohortCovariates) {
      List<ColumnFieldData> selectCols = new ArrayList<>();
      
      if (retainCohortCovariates) {
          if (periodType != null && periodType.length > 0) {
              selectCols.add(new ColumnFieldData("period_type_concept_id", ColumnFieldDataType.INTEGER));
          }
      }
      
      return selectCols;
  }
  
  @Override
  public String embedCriteriaGroup(String query) {
      ArrayList<String> selectColsCQ = new ArrayList<>();
      ArrayList<String> selectColsG = new ArrayList<>();
      
      if (periodType != null && periodType.length > 0) {
          selectColsCQ.add(", CQ.period_type_concept_id");
          selectColsG.add(", G.period_type_concept_id");
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
          if (entry.getKey().equals("period_type_concept_id") && periodType != null && periodType.length > 0) {
              selectCols.add(", cc.period_type_concept_id");
              groupCols.add(", cc.period_type_concept_id");
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
      
      if (periodType != null && periodType.length > 0) {
          selectColsA.add(", A.period_type_concept_id");
      }
      
      query = StringUtils.replace(query, "@p.additionColumns", StringUtils.join(selectColsA, ""));
      return query;
  }
  
  @Override
  public String embedWrapCriteriaQuery(String query, List<String> selectColsPE, BuilderOptions options) {
      ArrayList<String> selectCols = new ArrayList<>();
      
      if(!options.isPrimaryCriteria()){
        if (periodType != null && periodType.length > 0) {
            selectCols.add(", Q.period_type_concept_id");
            selectColsPE.add(", PE.period_type_concept_id");
        }
      }
      
      query = StringUtils.replace(query, "@QAdditionalColumnsInclusionN", StringUtils.join(selectCols, ""));
      return query;
  }
}
