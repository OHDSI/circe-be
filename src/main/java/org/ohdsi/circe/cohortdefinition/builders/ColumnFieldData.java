package org.ohdsi.circe.cohortdefinition.builders;

public class ColumnFieldData {
    private String name;
    
    private ColumnFieldDataType dataType;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public ColumnFieldDataType getDataType() {
        return dataType;
    }
    
    public void setDataType(ColumnFieldDataType dataType) {
        this.dataType = dataType;
    }
    
    public ColumnFieldData(String name, ColumnFieldDataType dataType) {
        this.name = name;
        this.dataType = dataType;
    }
    
}
