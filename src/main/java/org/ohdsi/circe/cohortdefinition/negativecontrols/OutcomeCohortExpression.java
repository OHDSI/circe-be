package org.ohdsi.circe.cohortdefinition.negativecontrols;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
 
 @JsonProperty("domainIds")
 public List<String> domainIds = new ArrayList<>(); 
 
 /*
	public static CohortExpression fromJson(String json) {
		try {
			CohortExpression expression = JSON_MAPPER.readValue(json, CohortExpression.class);
			return expression;
		} catch (Exception e) {
			throw new RuntimeException("Error parsing cohort expression", e);
		}
	}
 */
}
