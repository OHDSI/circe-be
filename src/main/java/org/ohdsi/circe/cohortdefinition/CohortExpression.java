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
 * Authors: Christopher Knoll, Gowtham Rao
 *
 */
package org.ohdsi.circe.cohortdefinition;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import org.ohdsi.analysis.Utils;
import org.ohdsi.analysis.versioning.CdmCompatibilitySpec;
import org.ohdsi.analysis.versioning.CdmVersion;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author cknoll1
 */

@JsonIgnoreProperties(ignoreUnknown=true)
@CdmVersion(range = ">=5.0.0")
public class CohortExpression implements CdmCompatibilitySpec {

  @JsonProperty("cdmVersionRange")
  private String cdmVersionRange = null;

  @JsonProperty("Title")  
  public String title;
  
  @JsonProperty("PrimaryCriteria")
  public PrimaryCriteria primaryCriteria;

  @JsonProperty("AdditionalCriteria")
  public CriteriaGroup additionalCriteria;
  
  @JsonProperty("ConceptSets")
  public ConceptSet[] conceptSets;
  
  @JsonProperty("QualifiedLimit")  
  public ResultLimit qualifiedLimit = new ResultLimit();
  
  @JsonProperty("ExpressionLimit")
  public ResultLimit expressionLimit = new ResultLimit();

  @JsonProperty("InclusionRules")
  public List<InclusionRule> inclusionRules = new ArrayList<>();
  
  @JsonProperty("EndStrategy")
  public EndStrategy endStrategy;
  
  @JsonProperty("CensoringCriteria")
  public Criteria[] censoringCriteria;
  
  @JsonProperty("CollapseSettings")
  public CollapseSettings collapseSettings = new CollapseSettings();

  @JsonProperty("CensorWindow")
  public Period censorWindow;
  
  /**
   * If specified the datetime columns should be used in Criteria instead of date
   */
  @JsonProperty("UseDatetime")
  public Boolean useDatetime;
  

  @Override
  public String getCdmVersionRange() {
    return cdmVersionRange;
  }

  @Override
  public void setCdmVersionRange(String cdmVersionRange) {
    this.cdmVersionRange = cdmVersionRange;
  }

  public static CohortExpression fromJson(String json) {
	return Utils.deserialize(json, new TypeReference<CohortExpression>() {});
  }
}
