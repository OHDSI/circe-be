package org.ohdsi.circe.cohortdefinition.builders;

import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.Criteria;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class CriteriaSqlBuilder<T extends Criteria> {

  public String getCriteriaSql(T criteria) {
    return getCriteriaSql(criteria, null);
  }

  public String getCriteriaSql(T criteria, BuilderOptions options) {

    String query = getQueryTemplate();

    query = embedCodesetClause(query, criteria);

    List<String> selectClauses = resolveSelectClauses(criteria);
    List<String> joinClauses = resolveJoinClauses(criteria);
    List<String> whereClauses = resolveWhereClauses(criteria);

    query = embedOrdinalExpression(query, criteria, whereClauses);

    query = embedSelectClauses(query, selectClauses);
    query = embedJoinClauses(query, joinClauses);
    query = embedWhereClauses(query, whereClauses);

    if (options != null) {
      List<CriteriaColumn> filteredColumns = options.additionalColumns.stream()
              .filter((column) -> !this.getDefaultColumns().contains(column))
              .collect(Collectors.toList());
      if (filteredColumns.size() > 0) {
        query = StringUtils.replace(query, "@additionalColumns", ", " + this.getAdditionalColumns(filteredColumns));
      } else {
        query = StringUtils.replace(query, "@additionalColumns", "");
      }
    } else {
      query = StringUtils.replace(query, "@additionalColumns", "");
    }

    return query;
  }

  protected abstract String getTableColumnForCriteriaColumn(CriteriaColumn column);

  protected String getAdditionalColumns(List<CriteriaColumn> columns) {
    String cols = String.join(", ", columns.stream()
            .map((column) -> {
              return String.format("%s as %s", getTableColumnForCriteriaColumn(column), column.columnName());
            }).collect(Collectors.toList()));
    return cols;
  }

  protected abstract Set<CriteriaColumn> getDefaultColumns();

  protected String embedSelectClauses(String query, List<String> selectClauses) {
    return StringUtils.replace(query, "@selectClause", StringUtils.join(selectClauses, ","));
  }

  protected String embedJoinClauses(String query, List<String> joinClauses) {

    return StringUtils.replace(query, "@joinClause", StringUtils.join(joinClauses, "\n"));
  }

  protected String embedWhereClauses(String query, List<String> whereClauses) {

    String whereClause = "";
    if (whereClauses.size() > 0) {
      whereClause = "WHERE " + StringUtils.join(whereClauses, "\nAND ");
    }
    return StringUtils.replace(query, "@whereClause", whereClause);
  }

  protected abstract String getQueryTemplate();

  protected abstract String embedCodesetClause(String query, T criteria);

  protected abstract String embedOrdinalExpression(String query, T criteria, List<String> whereClauses);

  protected List<String> resolveSelectClauses(T criteria) {
    return new ArrayList<String>();
  }

  protected abstract List<String> resolveJoinClauses(T criteria);

  protected List<String> resolveWhereClauses(T criteria) {
    ArrayList<String> whereClauses = new ArrayList<>();
    
    // if date is adjusted, the record should only be included if end >= start
    if (criteria.dateAdjustment != null) {
      whereClauses.add("C.end_date >= C.start_date");
    }
    
    return whereClauses;
  }
}
