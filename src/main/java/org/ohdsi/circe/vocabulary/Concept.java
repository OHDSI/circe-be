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
package org.ohdsi.circe.vocabulary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 *
 * @author fdefalco
 */
@JsonInclude(Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({
        "CONCEPT_ID", "CONCEPT_NAME", "STANDARD_CONCEPT",
        "STANDARD_CONCEPT_CAPTION", "INVALID_REASON", "INVALID_REASON_CAPTION",
        "CONCEPT_CODE", "DOMAIN_ID", "VOCABULARY_ID", "CONCEPT_CLASS_ID"
})
public class Concept {
    @JsonProperty("CONCEPT_ID")
    public Long conceptId;
    
    @JsonProperty("CONCEPT_NAME")
    public String conceptName;
    
    @JsonProperty("STANDARD_CONCEPT_CAPTION")
    public String GetStandardConcept() {
      if (standardConcept == null)
        return "Unknown";
      
      switch (standardConcept) {
        case "N" : 
          return "Non-Standard";
        case "S" :
          return "Standard";
        case "C" :
          return "Classification";
        default:
          return "Unknown";
      }
    }
    
    @JsonProperty("STANDARD_CONCEPT")
    public String standardConcept;
    
    @JsonProperty("INVALID_REASON_CAPTION")
    public String GetInvalidReason() {
      if (invalidReason == null)
        return "Unknown";
      
      switch (invalidReason) {
        case "V" : 
          return "Valid";
        case "D" :
          return "Invalid";
        case "U" :
            return "Invalid";
        default:
          return "Unknown";
      }
    }
    
    @JsonProperty("INVALID_REASON")
    public String invalidReason;
    
    @JsonProperty("CONCEPT_CODE")
    public String conceptCode;
    
    @JsonProperty("DOMAIN_ID")
    public String domainId;
    
    @JsonProperty("VOCABULARY_ID")
    public String vocabularyId;
    
    @JsonProperty("CONCEPT_CLASS_ID")
    public String conceptClassId;
}
