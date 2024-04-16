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
public class Measurement extends Criteria {
  
  @JsonProperty("CodesetId")  
  public Integer codesetId;
  
  @JsonProperty("First")
  public Boolean first;
  
  @JsonProperty("OccurrenceStartDate")
  public DateRange occurrenceStartDate;

  @JsonProperty("MeasurementType")
  public Concept[] measurementType;

  @JsonProperty("MeasurementTypeExclude")
  public boolean measurementTypeExclude = false;
	
  @JsonProperty("Operator")
  public Concept[] operator;

  @JsonProperty("ValueAsNumber")
  public NumericRange valueAsNumber;

  @JsonProperty("ValueAsConcept")
  public Concept[] valueAsConcept;
  
  @JsonProperty("Unit")
  public Concept[] unit;
  
  @JsonProperty("RangeLow")
  public NumericRange rangeLow;

  @JsonProperty("RangeHigh")
  public NumericRange rangeHigh;

  @JsonProperty("RangeLowRatio")
  public NumericRange rangeLowRatio;

  @JsonProperty("RangeHighRatio")
  public NumericRange rangeHighRatio;

  @JsonProperty("Abnormal")
  public Boolean abnormal;
  
  @JsonProperty("MeasurementSourceConcept")
  public Integer measurementSourceConcept;
  
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
  public List<ColumnFieldData> getSelectedField(BuilderOptions options) {
      List<ColumnFieldData> selectCols = new ArrayList<>();
      
      selectCols.add(new ColumnFieldData("value_as_number", ColumnFieldDataType.NUMERIC));
      
      if (valueAsConcept != null && valueAsConcept.length > 0) {
          selectCols.add(new ColumnFieldData("value_as_concept_id", ColumnFieldDataType.INTEGER));
      }
      // unit
      if (unit != null && unit.length > 0) {
          selectCols.add(new ColumnFieldData("unit_concept_id", ColumnFieldDataType.INTEGER));
      }
      // range_low
      if (rangeLow != null) {
          selectCols.add(new ColumnFieldData("range_low", ColumnFieldDataType.NUMERIC));
      }
      
      // range_high
      if (rangeHigh != null) {
          selectCols.add(new ColumnFieldData("range_high", ColumnFieldDataType.NUMERIC));
      }
      
      // providerSpecialty
      if (providerSpecialty != null && providerSpecialty.length > 0) {
          selectCols.add(new ColumnFieldData("provider_id", ColumnFieldDataType.INTEGER));
      }
      
      // measurementType
      if (measurementType != null && measurementType.length > 0) {
          selectCols.add(new ColumnFieldData("measurement_type_concept_id", ColumnFieldDataType.INTEGER));
      }
      
      // operator
      if (operator != null && operator.length > 0) {
          selectCols.add(new ColumnFieldData("operator_concept_id", ColumnFieldDataType.INTEGER));
      }
      
      return selectCols;
  }
  
  @Override
  public String embedCriteriaGroup(String query) {
      ArrayList<String> selectColsCQ = new ArrayList<>();
      ArrayList<String> selectColsG = new ArrayList<>();
      
      selectColsCQ.add(", CQ.value_as_number");
      selectColsG.add(", G.value_as_number");
      
      if (valueAsConcept != null && valueAsConcept.length > 0) {
          selectColsCQ.add(", CQ.value_as_concept_id");
          selectColsG.add(", G.value_as_concept_id");
      }
      // unit
      if (unit != null && unit.length > 0) {
          selectColsCQ.add(", CQ.unit_concept_id");
          selectColsG.add(", G.unit_concept_id");
      }
      
      // range_low
      if (rangeLow != null) {
          selectColsCQ.add(", CQ.range_low");
          selectColsG.add(", G.range_low");
      }
      
      // range_high
      if (rangeHigh != null) {
          selectColsCQ.add(", CQ.range_high");
          selectColsG.add(", G.range_high");
      }
      
      // providerSpecialty
      if (providerSpecialty != null && providerSpecialty.length > 0) {
          selectColsCQ.add(", CQ.provider_id");
          selectColsG.add(", G.provider_id");
      }
      
      // measurementType
      if (measurementType != null && measurementType.length > 0) {
          selectColsCQ.add(", CQ.measurement_type_concept_id");
          selectColsG.add(", G.measurement_type_concept_id");
      }
      
      // operator
      if (operator != null && operator.length > 0) {
          selectColsCQ.add(", CQ.operator_concept_id");
          selectColsG.add(", G.operator_concept_id");
      }
      query = StringUtils.replace(query, "@e.additonColumns", StringUtils.join(selectColsCQ, ""));
      query = StringUtils.replace(query, "@additonColumnsGroup", StringUtils.join(selectColsG, ""));
      return query;
  }
  
  @Override
  public String embedWindowedCriteriaQuery(String query) {
      ArrayList<String> selectCols = new ArrayList<>();
      ArrayList<String> selectGroupCols = new ArrayList<>();
      selectCols.add(", cc.value_as_number");
      selectGroupCols.add(", cc.value_as_number");
      
      if (valueAsConcept != null && valueAsConcept.length > 0) {
          selectCols.add(", cc.value_as_concept_id");
          selectGroupCols.add(", cc.value_as_concept_id");
      }
      
      // unit
      if (unit != null && unit.length > 0) {
          selectCols.add(", cc.unit_concept_id");
          selectGroupCols.add(", cc.unit_concept_id");
      }
      
      // range_low
      if (rangeLow != null) {
          selectCols.add(", cc.range_low");
          selectGroupCols.add(", cc.range_low");
      }
      
      // range_high
      if (rangeHigh != null) {
          selectCols.add(", cc.range_high");
          selectGroupCols.add(", cc.range_high");
      }
      
      // providerSpecialty
      if (providerSpecialty != null && providerSpecialty.length > 0) {
          selectCols.add(", cc.provider_id");
          selectGroupCols.add(", cc.provider_id");
      }
      
      // measurementType
      if (measurementType != null && measurementType.length > 0) {
          selectCols.add(", cc.measurement_type_concept_id");
          selectGroupCols.add(", cc.measurement_type_concept_id");
      }
      
      // operator
      if (operator != null && operator.length > 0) {
          selectCols.add(", cc.operator_concept_id");
          selectGroupCols.add(", cc.operator_concept_id");
      }
      
      query = StringUtils.replace(query, "@additionColumnscc", StringUtils.join(selectCols, ""));
      query = StringUtils.replace(query, "@additionColumnGroupscc", StringUtils.join(selectGroupCols, ""));
      return query;
  }
  
  @Override
  public String embedWindowedCriteriaQueryP(String query) {
      ArrayList<String> selectColsA = new ArrayList<>();
      selectColsA.add(", A.value_as_number");
      if (valueAsConcept != null && valueAsConcept.length > 0) {
          selectColsA.add(", A.value_as_concept_id");
      }
      
      // unit
      if (unit != null && unit.length > 0) {
          selectColsA.add(", A.unit_concept_id");
      }
      
      // range_low
      if (rangeLow != null) {
          selectColsA.add(", A.range_low");
      }
      
      // range_high
      if (rangeHigh != null) {
          selectColsA.add(", A.range_high");
      }
      
      // providerSpecialty
      if (providerSpecialty != null && providerSpecialty.length > 0) {
          selectColsA.add(", A.provider_id");
      }
      
      // measurementType
      if (measurementType != null && measurementType.length > 0) {
          selectColsA.add(", A.measurement_type_concept_id");
      }
      
      // operator
      if (operator != null && operator.length > 0) {
          selectColsA.add(", A.operator_concept_id");
      }
      query = StringUtils.replace(query, "@p.additionColumns", StringUtils.join(selectColsA, ""));
      return query;
  }
  
  @Override
  public String embedWrapCriteriaQuery(String query, List<String> selectColsPE) {
      ArrayList<String> selectCols = new ArrayList<>();
      selectCols.add(", Q.value_as_number");
      selectColsPE.add(", AC.value_as_number");
      
      if (valueAsConcept != null && valueAsConcept.length > 0) {
          selectCols.add(", Q.value_as_concept_id");
          selectColsPE.add(", AC.value_as_concept_id");
      }
      
      // unit
      if (unit != null && unit.length > 0) {
          selectCols.add(", Q.unit_concept_id");
          selectColsPE.add(", AC.unit_concept_id");
      }
      
      // range_low
      if (rangeLow != null) {
          selectCols.add(", Q.range_low");
          selectColsPE.add(", AC.range_low");
      }
      
      // range_high
      if (rangeHigh != null) {
          selectCols.add(", Q.range_high");
          selectColsPE.add(", AC.range_high");
      }
      
      // providerSpecialty
      if (providerSpecialty != null && providerSpecialty.length > 0) {
          selectCols.add(", Q.provider_id");
          selectColsPE.add(", AC.provider_id");
      }
      
      // measurementType
      if (measurementType != null && measurementType.length > 0) {
          selectCols.add(", Q.measurement_type_concept_id");
          selectColsPE.add(", AC.measurement_type_concept_id");
      }
      
      // operator
      if (operator != null && operator.length > 0) {
          selectCols.add(", Q.operator_concept_id");
          selectColsPE.add(", AC.operator_concept_id");
      }
      query = StringUtils.replace(query, "@QAdditionalColumnsInclusionN", StringUtils.join(selectCols, ""));
      return query;
  }
  
}
