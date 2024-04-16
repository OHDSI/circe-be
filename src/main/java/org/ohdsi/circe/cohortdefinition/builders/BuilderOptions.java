package org.ohdsi.circe.cohortdefinition.builders;

import java.util.ArrayList;
import java.util.List;

public class BuilderOptions {

  public List<CriteriaColumn> additionalColumns = new ArrayList<>();
  
  private boolean useDatetime;
  private boolean retainCohortCovariates;

  public boolean isUseDatetime() {
    return useDatetime;
  }

  public boolean isRetainCohortCovariates() {
    return retainCohortCovariates;
  }

  public void setUseDatetime(Boolean useDatetime) {
    this.useDatetime = useDatetime == null ? false: useDatetime;
  }

  public void setRetainCohortCovariates(Boolean retainCohortCovariates) {
    this.retainCohortCovariates = retainCohortCovariates;
  }
}
