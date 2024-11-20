package org.ohdsi.circe.cohortdefinition.builders;

public enum ColumnFieldDataType {
    INTEGER("int"), NUMERIC("numeric"), VARCHAR("varchar"), DATE("date");
    
    private String type;
    
    ColumnFieldDataType(String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
}
