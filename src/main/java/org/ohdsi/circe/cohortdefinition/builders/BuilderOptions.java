package org.ohdsi.circe.cohortdefinition.builders;

import java.util.ArrayList;
import java.util.List;

public class BuilderOptions {

  public List<CriteriaColumn> additionalColumns = new ArrayList<>();
  
  private boolean useDatetime;

  public boolean isUseDatetime() {
    return useDatetime;
  }

  public void setUseDatetime(Boolean useDatetime) {
    this.useDatetime = useDatetime == null ? false: useDatetime;
  }
}
