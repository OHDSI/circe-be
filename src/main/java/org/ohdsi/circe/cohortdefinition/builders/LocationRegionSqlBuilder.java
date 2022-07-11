package org.ohdsi.circe.cohortdefinition.builders;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.LocationRegion;
import org.ohdsi.circe.helper.ResourceHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.getCodesetJoinExpression;

public class LocationRegionSqlBuilder<T extends LocationRegion> extends CriteriaSqlBuilder<T> {

  private final static String LOCATION_REGION_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/locationRegion.sql");

  // default columns are those that are specified in the template, and dont' need to be added if specifeid in 'additionalColumns'
  private final Set<CriteriaColumn> DEFAULT_COLUMNS = new HashSet<>(Arrays.asList(CriteriaColumn.START_DATE, CriteriaColumn.END_DATE, CriteriaColumn.VISIT_ID));

  @Override
  protected Set<CriteriaColumn> getDefaultColumns() {
    return DEFAULT_COLUMNS;
  }

  @Override
  protected String getQueryTemplate() {
    return LOCATION_REGION_TEMPLATE;
  }

  @Override
  protected String getTableColumnForCriteriaColumn(CriteriaColumn column) {
    switch (column) {
      case DOMAIN_CONCEPT:
        return "C.region_concept_id";
      default:
        throw new IllegalArgumentException("Invalid CriteriaColumn for Location Region:" + column.toString());
    }
  }

  @Override
  protected String embedCodesetClause(String query, T criteria) {

    return StringUtils.replace(query, "@codesetClause",
            getCodesetJoinExpression(criteria.codesetId,
                    "l.region_concept_id",
                    null,
                    null)
    );
  }

  @Override
  protected String embedOrdinalExpression(String query, T criteria, List<String> whereClauses) {

    return query;
  }

  @Override
  protected List<String> resolveJoinClauses(T criteria) {

    return new ArrayList<>();
  }

  @Override
  protected List<String> resolveWhereClauses(T criteria) {

    return new ArrayList<>();
  }
}
