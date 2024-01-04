package org.ohdsi.circe.cohortdefinition;

import jdk.nashorn.internal.objects.annotations.Getter;

public enum IntervalUnit {
    DAY("day"),
    HOUR("hour"),
    MINUTE("minute"),
    SECOND("second");

    private final String name;

    IntervalUnit(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
