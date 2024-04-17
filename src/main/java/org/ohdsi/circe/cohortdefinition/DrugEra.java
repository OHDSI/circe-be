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
public class DrugEra extends Criteria {

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

  @JsonProperty("GapDays")  
  public NumericRange gapDays;

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
          selectCols.add(new ColumnFieldData("drug_exposure_count", ColumnFieldDataType.INTEGER));
      }
      
      if (gapDays != null) {
          selectCols.add(new ColumnFieldData("gap_days", ColumnFieldDataType.INTEGER));
      }
      
      return selectCols;
  }
  
  @Override
  public String embedCriteriaGroup(String query) {
      ArrayList<String> selectColsCQ = new ArrayList<>();
      ArrayList<String> selectColsG = new ArrayList<>();
      
      if (occurrenceCount != null) {
          selectColsCQ.add(", CQ.drug_exposure_count");
          selectColsG.add(", G.drug_exposure_count");
      }
      
      if (gapDays != null) {
          selectColsCQ.add(", CQ.gap_days");
          selectColsG.add(", G.gap_days");
      }
      
      query = StringUtils.replace(query, "@e.additonColumns", StringUtils.join(selectColsCQ, ""));
      query = StringUtils.replace(query, "@additonColumnsGroup", StringUtils.join(selectColsG, ""));
      return query;
  }
  
  @Override
  public String embedWindowedCriteriaQuery(String query) {
      ArrayList<String> selectCols = new ArrayList<>();
      
      if (occurrenceCount != null) {
          selectCols.add(", cc.drug_exposure_count");
      }
      
      if (gapDays != null) {
          selectCols.add(", cc.gap_days");
      }
      
      query = StringUtils.replace(query, "@additionColumnscc", StringUtils.join(selectCols, ""));
      return query;
  }
  
  @Override
  public String embedWindowedCriteriaQueryP(String query) {
      ArrayList<String> selectColsA = new ArrayList<>();
      
      if (occurrenceCount != null) {
          selectColsA.add(", A.drug_exposure_count");
      }
      
      if (gapDays != null) {
          selectColsA.add(", A.gap_days");
      }
      
      query = StringUtils.replace(query, "@p.additionColumns", StringUtils.join(selectColsA, ""));
      return query;
  }
  
  @Override
  public String embedWrapCriteriaQuery(String query, List<String> selectColsPE) {
      ArrayList<String> selectCols = new ArrayList<>();
      
      if (occurrenceCount != null) {
          selectCols.add(", Q.drug_exposure_count");
          selectColsPE.add(", PE.drug_exposure_count");
      }
      
      if (gapDays != null) {
          selectCols.add(", Q.gap_days");
          selectColsPE.add(", PE.gap_days");
      }
      
      query = StringUtils.replace(query, "@QAdditionalColumnsInclusionN", StringUtils.join(selectCols, ""));
      return query;
  }
}
