package org.ohdsi.circe.cohortdefinition;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class GeoCriteria extends Criteria {

    @JsonProperty("StartDate")
    public DateRange startDate;

    @JsonProperty("EndDate")
    public DateRange endDate;
}
