package org.ohdsi.circe.cohortdefinition;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MeasurementOperand {
  @JsonProperty("Measurement")
  public Measurement measurement;
  @JsonProperty("Operator")
  public String operator;
  @JsonProperty("Limit")
  public String limit = "First";
  @JsonProperty("ValueAsNumber")
  public NumericRange valueAsNumber;
  @JsonProperty("SameVisit")
  public Boolean sameVisit = true;
}
