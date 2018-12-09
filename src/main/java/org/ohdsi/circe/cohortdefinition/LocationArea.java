package org.ohdsi.circe.cohortdefinition;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("LocationArea")
public class LocationArea extends GeoCriteria {

    // TODO: Extract to common class

    @JsonProperty("CodesetId")
    public Integer codesetId;

    //

    @Override
    public String accept(IGetCriteriaSqlDispatcher dispatcher)
    {
        return dispatcher.getCriteriaSql(this);
    }
}
