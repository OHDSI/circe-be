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
import org.ohdsi.analysis.versioning.CdmVersion;
import org.ohdsi.circe.cohortdefinition.builders.BuilderOptions;
import org.ohdsi.circe.vocabulary.Concept;

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
  
  
}
