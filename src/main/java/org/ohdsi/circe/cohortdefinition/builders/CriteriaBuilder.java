package org.ohdsi.circe.cohortdefinition.builders;

import org.ohdsi.circe.cohortdefinition.Criteria;

public interface CriteriaBuilder<T extends Criteria> {

    String getCriteriaSql(T criteria);
}
