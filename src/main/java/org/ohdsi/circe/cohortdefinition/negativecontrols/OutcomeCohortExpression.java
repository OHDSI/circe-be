package org.ohdsi.circe.cohortdefinition.negativecontrols;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown=true)
public class OutcomeCohortExpression {
 private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
 
 @JsonProperty("occurrenceType")
 public OccurrenceType occurrenceType;
 
 @JsonProperty("detectOnDescendants")
 public Boolean detectOnDescendants = true;
 
 @JsonProperty("domains")
 public List<String> domains = new ArrayList<>(); 
 
 public static OutcomeCohortExpression fromJson(String json) {
   try {
        JSON_MAPPER.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        OutcomeCohortExpression expression = JSON_MAPPER.readValue(json, OutcomeCohortExpression.class);
        return expression;
    } catch (Exception e) {
        throw new RuntimeException("Error parsing cohort expression", e);
    }
  }
}
