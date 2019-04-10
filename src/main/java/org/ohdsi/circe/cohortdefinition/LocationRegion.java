package org.ohdsi.circe.cohortdefinition;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("LocationRegion")
public class LocationRegion extends GeoCriteria {

    @JsonProperty("CodesetId")
    public Integer codesetId;

    @Override
    public String accept(IGetCriteriaSqlDispatcher dispatcher)
    {
        return dispatcher.getCriteriaSql(this);
    }
}
