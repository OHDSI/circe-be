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
import com.fasterxml.jackson.core.sym.CharsToNameCanonicalizer;

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
public class DeviceExposure extends Criteria {

  @JsonProperty("CodesetId")
  public Integer codesetId;

  @JsonProperty("First")
  public Boolean first;

  @JsonProperty("OccurrenceStartDate")
  public DateRange occurrenceStartDate;

  @JsonProperty("OccurrenceEndDate")
  public DateRange occurrenceEndDate;

  @JsonProperty("DeviceType")
  public Concept[] deviceType;

  @JsonProperty("DeviceTypeExclude")
  public boolean deviceTypeExclude = false;

	@JsonProperty("UniqueDeviceId")
  public TextFilter uniqueDeviceId;

  @JsonProperty("Quantity")
  public NumericRange quantity;

  @JsonProperty("DeviceSourceConcept")
  public Integer deviceSourceConcept;

  @JsonProperty("Age")
  public NumericRange age;

  @JsonProperty("Gender")
  public Concept[] gender;

  @JsonProperty("ProviderSpecialty")
  public Concept[] providerSpecialty;

  @JsonProperty("VisitType")
  public Concept[] visitType;

  @JsonProperty("UnitConceptId")
  public Concept[] unitConceptId;

  @Override
  public String accept(IGetCriteriaSqlDispatcher dispatcher, BuilderOptions options) {
    return dispatcher.getCriteriaSql(this, options);
  }
  
  @Override
  public List<ColumnFieldData> getSelectedField(BuilderOptions options) {
      List<ColumnFieldData> selectCols = new ArrayList<>();
      
      if (deviceType != null && deviceType.length > 0) {
          selectCols.add(new ColumnFieldData("device_type_concept_id", ColumnFieldDataType.INTEGER));
      }
      
      if (quantity != null) {
          selectCols.add(new ColumnFieldData("quantity", ColumnFieldDataType.INTEGER));
      }
      
      if (uniqueDeviceId != null) {
          selectCols.add(new ColumnFieldData("unique_device_id", ColumnFieldDataType.VARCHAR));
      }
      
      if (deviceSourceConcept != null) {
          selectCols.add(new ColumnFieldData("device_source_concept_id", ColumnFieldDataType.INTEGER));
      }
      
      // providerSpecialty
      if (providerSpecialty != null && providerSpecialty.length > 0) {
          selectCols.add(new ColumnFieldData("provider_id", ColumnFieldDataType.INTEGER));
      }
      
      // unit
      if (unitConceptId != null && unitConceptId.length > 0) {
          selectCols.add(new ColumnFieldData("unit_concept_id", ColumnFieldDataType.INTEGER));
      }
      
      return selectCols;
  }
  
  @Override
  public String embedCriteriaGroup(String query) {
      ArrayList<String> selectColsCQ = new ArrayList<>();
      ArrayList<String> selectColsG = new ArrayList<>();
      
      if (deviceType != null && deviceType.length > 0) {
          selectColsCQ.add(", CQ.device_type_concept_id");
          selectColsG.add(", G.device_type_concept_id");
      }
      
      if (quantity != null) {
          selectColsCQ.add(", CQ.quantity");
          selectColsG.add(", G.quantity");
      }
      
      if (uniqueDeviceId != null) {
          selectColsCQ.add(", CQ.unique_device_id");
          selectColsG.add(", G.unique_device_id");
      }
      
      if (deviceSourceConcept != null) {
          selectColsCQ.add(", CQ.device_source_concept_id");
          selectColsG.add(", G.device_source_concept_id");
      }
      
      // providerSpecialty
      if (providerSpecialty != null && providerSpecialty.length > 0) {
          selectColsCQ.add(", CQ.provider_id");
          selectColsG.add(", G.provider_id");
      }
      
      // unit
      if (unitConceptId != null && unitConceptId.length > 0) {
          selectColsCQ.add(", CQ.unit_concept_id");
          selectColsG.add(", G.unit_concept_id");
      }
      
      query = StringUtils.replace(query, "@e.additonColumns", StringUtils.join(selectColsCQ, ""));
      query = StringUtils.replace(query, "@additonColumnsGroup", StringUtils.join(selectColsG, ""));
      return query;
  }
  
  @Override
  public String embedWindowedCriteriaQuery(String query) {
      ArrayList<String> selectCols = new ArrayList<>();
      
      if (deviceType != null && deviceType.length > 0) {
          selectCols.add(", cc.device_type_concept_id");
      }
      
      if (quantity != null) {
          selectCols.add(", cc.quantity");
      }
      
      if (uniqueDeviceId != null) {
          selectCols.add(", cc.unique_device_id");
      }
      
      if (deviceSourceConcept != null) {
          selectCols.add(", cc.device_source_concept_id");
      }
      
      // providerSpecialty
      if (providerSpecialty != null && providerSpecialty.length > 0) {
          selectCols.add(", cc.provider_id");
      }
      
      // unit
      if (unitConceptId != null && unitConceptId.length > 0) {
          selectCols.add(", cc.unit_concept_id");
      }
      
      query = StringUtils.replace(query, "@additionColumnscc", StringUtils.join(selectCols, ""));
      return query;
  }
  
  @Override
  public String embedWindowedCriteriaQueryP(String query) {
      ArrayList<String> selectColsA = new ArrayList<>();
      
      if (deviceType != null && deviceType.length > 0) {
          selectColsA.add(", A.device_type_concept_id");
      }
      
      if (quantity != null) {
          selectColsA.add(", A.quantity");
      }
      
      if (uniqueDeviceId != null) {
          selectColsA.add(", A.unique_device_id");
      }
      
      if (deviceSourceConcept != null) {
          selectColsA.add(", A.device_source_concept_id");
      }
      
      // providerSpecialty
      if (providerSpecialty != null && providerSpecialty.length > 0) {
          selectColsA.add(", A.provider_id");
      }
      
      // unit
      if (unitConceptId != null && unitConceptId.length > 0) {
          selectColsA.add(", A.unit_concept_id");
      }
      
      query = StringUtils.replace(query, "@p.additionColumns", StringUtils.join(selectColsA, ""));
      return query;
  }
  
  @Override
  public String embedWrapCriteriaQuery(String query, List<String> selectColsPE) {
      ArrayList<String> selectCols = new ArrayList<>();
      
      if (deviceType != null && deviceType.length > 0) {
          selectCols.add(", Q.device_type_concept_id");
          selectColsPE.add(", PE.device_type_concept_id");
      }
      
      if (quantity != null) {
          selectCols.add(", Q.quantity");
          selectColsPE.add(", PE.quantity");
      }
      
      if (uniqueDeviceId != null) {
          selectCols.add(", Q.unique_device_id");
          selectColsPE.add(", PE.unique_device_id");
      }
      
      if (deviceSourceConcept != null) {
          selectCols.add(", Q.device_source_concept_id");
          selectColsPE.add(", PE.device_source_concept_id");
      }
      
      // providerSpecialty
      if (providerSpecialty != null && providerSpecialty.length > 0) {
          selectCols.add(", Q.provider_id");
          selectColsPE.add(", PE.provider_id");
      }
      
      // unit
      if (unitConceptId != null && unitConceptId.length > 0) {
          selectCols.add(", Q.unit_concept_id");
          selectColsPE.add(", PE.unit_concept_id");
      }
      
      query = StringUtils.replace(query, "@QAdditionalColumnsInclusionN", StringUtils.join(selectCols, ""));
      return query;
  }
}
