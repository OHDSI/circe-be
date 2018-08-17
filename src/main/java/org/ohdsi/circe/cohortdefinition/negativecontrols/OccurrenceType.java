package org.ohdsi.circe.cohortdefinition.negativecontrols;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum OccurrenceType {
    @JsonProperty("FIRST")
    FIRST,
    @JsonProperty("ALL")
    ALL;
}
