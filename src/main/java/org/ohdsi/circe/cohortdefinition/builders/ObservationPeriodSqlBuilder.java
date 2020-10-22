package org.ohdsi.circe.cohortdefinition.builders;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.ObservationPeriod;
import org.ohdsi.circe.cohortdefinition.Period;
import org.ohdsi.circe.helper.ResourceHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildDateRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildNumericRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.getConceptIdsFromConcepts;

public class ObservationPeriodSqlBuilder<T extends ObservationPeriod> extends CriteriaSqlBuilder<T> {

  private final static String OBSERVATION_PERIOD_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/observationPeriod.sql");

  // default columns are those that are specified in the template, and dont' need to be added if specifeid in 'additionalColumns'
  private final Set<CriteriaColumn> DEFAULT_COLUMNS = new HashSet<>(Arrays.asList(CriteriaColumn.START_DATE, CriteriaColumn.END_DATE));

  @Override
  protected Set<CriteriaColumn> getDefaultColumns() {
    return DEFAULT_COLUMNS;
  }

  @Override
  public String getCriteriaSql(T criteria, BuilderOptions options) {

    String query = super.getCriteriaSql(criteria, options);

    // overwrite user defined dates in select
    String startDateExpression = (criteria.userDefinedPeriod != null && criteria.userDefinedPeriod.startDate != null)
            ? BuilderUtils.dateStringToSql(criteria.userDefinedPeriod.startDate)
            : "C.observation_period_start_date";
    query = StringUtils.replace(query, "@startDateExpression", startDateExpression);

    String endDateExpression = (criteria.userDefinedPeriod != null && criteria.userDefinedPeriod.endDate != null)
            ? BuilderUtils.dateStringToSql(criteria.userDefinedPeriod.endDate)
            : "C.observation_period_end_date";
    query = StringUtils.replace(query, "@endDateExpression", endDateExpression);
    return query;
  }

  @Override
  protected String getQueryTemplate() {
    return OBSERVATION_PERIOD_TEMPLATE;
  }

  @Override
  protected String getTableColumnForCriteriaColumn(CriteriaColumn column) {
    switch (column) {
      case DOMAIN_CONCEPT:
        return "C.period_type_concept_id";
      default:
        throw new IllegalArgumentException("Invalid CriteriaColumn for Observation Period:" + column.toString());
    }
  }

  @Override
  protected String embedCodesetClause(String query, T criteria) {

    return query;
  }

  @Override
  protected String embedOrdinalExpression(String query, T criteria, List<String> whereClauses) {

    return query;
  }

  @Override
  protected List<String> resolveJoinClauses(T criteria) {

    List<String> joinClauses = new ArrayList<>();

    // join to PERSON
    if (criteria.ageAtStart != null || criteria.ageAtEnd != null) {
      joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
    }

    return joinClauses;
  }

  @Override
  protected List<String> resolveWhereClauses(T criteria) {

    List<String> whereClauses = new ArrayList<>();

    if (criteria.first != null && criteria.first == true) {
      whereClauses.add("C.ordinal = 1");
    }

    // check for user defined start/end dates
    if (criteria.userDefinedPeriod != null) {
      Period userDefinedPeriod = criteria.userDefinedPeriod;

      if (userDefinedPeriod.startDate != null) {
        String startDateExpression = BuilderUtils.dateStringToSql(userDefinedPeriod.startDate);
        whereClauses.add(String.format("C.OBSERVATION_PERIOD_START_DATE <= %s and C.OBSERVATION_PERIOD_END_DATE >= %s", startDateExpression, startDateExpression));
      }

      if (userDefinedPeriod.endDate != null) {
        String endDateExpression = BuilderUtils.dateStringToSql(userDefinedPeriod.endDate);
        whereClauses.add(String.format("C.OBSERVATION_PERIOD_START_DATE <= %s and C.OBSERVATION_PERIOD_END_DATE >= %s", endDateExpression, endDateExpression));
      }
    }

    // periodStartDate
    if (criteria.periodStartDate != null) {
      whereClauses.add(buildDateRangeClause("C.observation_period_start_date", criteria.periodStartDate));
    }

    // periodEndDate
    if (criteria.periodEndDate != null) {
      whereClauses.add(buildDateRangeClause("C.observation_period_end_date", criteria.periodEndDate));
    }

    // periodType
    if (criteria.periodType != null && criteria.periodType.length > 0) {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.periodType);
      whereClauses.add(String.format("C.period_type_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
    }

    // periodLength
    if (criteria.periodLength != null) {
      whereClauses.add(buildNumericRangeClause("DATEDIFF(d,C.observation_period_start_date, C.observation_period_end_date)", criteria.periodLength));
    }

    // ageAtStart
    if (criteria.ageAtStart != null) {
      whereClauses.add(buildNumericRangeClause("YEAR(C.observation_period_start_date) - P.year_of_birth", criteria.ageAtStart));
    }

    // ageAtEnd
    if (criteria.ageAtEnd != null) {
      whereClauses.add(buildNumericRangeClause("YEAR(C.observation_period_end_date) - P.year_of_birth", criteria.ageAtEnd));
    }

    return whereClauses;
  }
}
