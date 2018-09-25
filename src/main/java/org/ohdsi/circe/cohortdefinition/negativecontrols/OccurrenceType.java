package org.ohdsi.circe.cohortdefinition.negativecontrols;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum OccurrenceType {
    FIRST,
    ALL;
    
    @JsonCreator
    public static OccurrenceType fromString(String key) {
        for(OccurrenceType type : OccurrenceType.values()) {
            if(type.name().equalsIgnoreCase(key)) {
                return type;
            }
        }
        return null;
    }
}
