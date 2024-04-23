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
import org.ohdsi.analysis.versioning.CdmVersion;
import org.ohdsi.circe.cohortdefinition.builders.BuilderOptions;
import org.ohdsi.circe.cohortdefinition.builders.ColumnFieldData;
import org.ohdsi.circe.cohortdefinition.builders.ColumnFieldDataType;

/**
 *
 * @author cknoll1
 */
public class VisitDetail extends Criteria {
  
  @JsonProperty("CodesetId")  
  public Integer codesetId;
  
  @JsonProperty("First")
  public Boolean first;

  @JsonProperty("VisitDetailStartDate")
  public DateRange visitDetailStartDate;

  @JsonProperty("VisitDetailEndDate")
  public DateRange visitDetailEndDate;

  @JsonProperty("VisitDetailTypeCS")
  public ConceptSetSelection  visitDetailTypeCS;

  @JsonProperty("VisitDetailSourceConcept")
  public Integer visitDetailSourceConcept;

  @JsonProperty("VisitDetailLength")
  public NumericRange visitDetailLength;
  
  @JsonProperty("Age")
  public NumericRange age;
  
  @JsonProperty("GenderCS")
  public ConceptSetSelection genderCS;
  
  @JsonProperty("ProviderSpecialtyCS")
  public ConceptSetSelection providerSpecialtyCS;

  @JsonProperty("PlaceOfServiceCS")
  public ConceptSetSelection placeOfServiceCS;

  /**
   * ID of Codeset which defines Geo concepts.
   * The care site's location.region_concept_id should match one of those.
   */

  @CdmVersion(range = ">=6.1")
  @JsonProperty("PlaceOfServiceLocation")
  public Integer placeOfServiceLocation;
  
  @Override
  public String accept(IGetCriteriaSqlDispatcher dispatcher, BuilderOptions options)
  {
    return dispatcher.getCriteriaSql(this, options);
  }
  
  @Override
  public List<ColumnFieldData> getSelectedField(Boolean retainCohortCovariates) {
      List<ColumnFieldData> selectCols = new ArrayList<>();
      
      if (retainCohortCovariates) {
          if (visitDetailStartDate != null) {
              selectCols.add(new ColumnFieldData("visit_detail_start_date", ColumnFieldDataType.DATE));
          }
          
          if (visitDetailEndDate != null) {
              selectCols.add(new ColumnFieldData("visit_detail_end_date", ColumnFieldDataType.DATE));
          }
          
          if (visitDetailTypeCS != null) {
              selectCols.add(new ColumnFieldData("visit_detail_type_concept_id", ColumnFieldDataType.INTEGER));
          }
          
          if (visitDetailSourceConcept != null) {
              selectCols.add(new ColumnFieldData("visit_detail_source_concept_id", ColumnFieldDataType.INTEGER));
          }
          
          if (providerSpecialtyCS != null) {
              selectCols.add(new ColumnFieldData("provider_id", ColumnFieldDataType.INTEGER));
          }
      }
      
      return selectCols;
  }
  
  @Override
  public String embedCriteriaGroup(String query) {
      ArrayList<String> selectColsCQ = new ArrayList<>();
      ArrayList<String> selectColsG = new ArrayList<>();
      
      if (visitDetailStartDate != null) {
          selectColsCQ.add(", CQ.visit_detail_start_date");
          selectColsG.add(", G.visit_detail_start_date");
      }
      
      if (visitDetailEndDate != null) {
          selectColsCQ.add(", CQ.visit_detail_end_date");
          selectColsG.add(", G.visit_detail_end_date");
      }
      
      if (visitDetailTypeCS != null) {
          selectColsCQ.add(", CQ.visit_detail_type_concept_id");
          selectColsG.add(", G.visit_detail_type_concept_id");
      }
      
      if (visitDetailSourceConcept != null) {
          selectColsCQ.add(", CQ.visit_detail_source_concept_id");
          selectColsG.add(", G.visit_detail_source_concept_id");
      }
      
      if (providerSpecialtyCS != null) {
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
          if (entry.getKey().equals("visit_detail_start_date") && visitDetailStartDate != null) {
              selectCols.add(", cc.visit_detail_start_date");
              groupCols.add(", cc.visit_detail_start_date");
          } else if (entry.getKey().equals("visit_detail_end_date") && visitDetailEndDate != null) {
              selectCols.add(", cc.visit_detail_end_date");
              groupCols.add(", cc.visit_detail_end_date");
          } else if (entry.getKey().equals("visit_detail_type_concept_id") && visitDetailTypeCS != null) {
              selectCols.add(", cc.visit_detail_type_concept_id");
              groupCols.add(", cc.visit_detail_type_concept_id");
          } else if (entry.getKey().equals("visit_detail_source_concept_id") && visitDetailSourceConcept != null) {
              selectCols.add(", cc.visit_detail_source_concept_id");
              groupCols.add(", cc.visit_detail_source_concept_id");
          } else if (entry.getKey().equals("provider_id") && providerSpecialtyCS != null) {
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
      
      if (visitDetailStartDate != null) {
          selectColsA.add(", A.visit_detail_start_date");
      }
      
      if (visitDetailEndDate != null) {
          selectColsA.add(", A.visit_detail_end_date");
      }
      
      if (visitDetailTypeCS != null) {
          selectColsA.add(", A.visit_detail_type_concept_id");
      }
      
      if (visitDetailSourceConcept != null) {
          selectColsA.add(", A.visit_detail_source_concept_id");
      }
      
      if (providerSpecialtyCS != null) {
          selectColsA.add(", A.provider_id");
      }
      
      query = StringUtils.replace(query, "@p.additionColumns", StringUtils.join(selectColsA, ""));
      return query;
  }
  
  @Override
  public String embedWrapCriteriaQuery(String query, List<String> selectColsPE) {
      ArrayList<String> selectCols = new ArrayList<>();
      
      if (visitDetailStartDate != null) {
          selectCols.add(", Q.visit_detail_start_date");
          selectColsPE.add(", PE.visit_detail_start_date");
      }
      
      if (visitDetailEndDate != null) {
          selectCols.add(", Q.visit_detail_end_date");
          selectColsPE.add(", PE.visit_detail_end_date");
      }
      
      if (visitDetailTypeCS != null) {
          selectCols.add(", Q.visit_detail_type_concept_id");
          selectColsPE.add(", PE.visit_detail_type_concept_id");
      }
      
      if (visitDetailSourceConcept != null) {
          selectCols.add(", Q.visit_detail_source_concept_id");
          selectColsPE.add(", PE.visit_detail_source_concept_id");
      }
      
      if (providerSpecialtyCS != null) {
          selectCols.add(", Q.provider_id");
          selectColsPE.add(", PE.provider_id");
      }
      
      query = StringUtils.replace(query, "@QAdditionalColumnsInclusionN", StringUtils.join(selectCols, ""));
      return query;
  }
}
