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
import org.ohdsi.circe.cohortdefinition.DateAdjustment;

import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildDateRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildNumericRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.getConceptIdsFromConcepts;

public class ObservationPeriodSqlBuilder<T extends ObservationPeriod> extends CriteriaSqlBuilder<T> {

  private final static String OBSERVATION_PERIOD_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/observationPeriod.sql");

  // default columns are those that are specified in the template, and dont' need to be added if specifeid in 'additionalColumns'
  private final Set<CriteriaColumn> DEFAULT_COLUMNS = new HashSet<>(Arrays.asList(CriteriaColumn.START_DATE, CriteriaColumn.END_DATE, CriteriaColumn.VISIT_ID));

  // default select columns are the columns that will always be returned from the subquery, but are added to based on the specific criteria
  private final List<String> DEFAULT_SELECT_COLUMNS = new ArrayList<>(Arrays.asList("op.person_id", "op.observation_period_id", "op.period_type_concept_id"));

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
            : "C.start_date";
    query = StringUtils.replace(query, "@startDateExpression", startDateExpression);

    String endDateExpression = (criteria.userDefinedPeriod != null && criteria.userDefinedPeriod.endDate != null)
            ? BuilderUtils.dateStringToSql(criteria.userDefinedPeriod.endDate)
            : "C.end_date";
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
      case DURATION:
        return "DATEDIFF(d, @startDateExpression, @endDateExpression)";
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
  protected List<String> resolveSelectClauses(T criteria) {

    ArrayList<String> selectCols = new ArrayList<>(DEFAULT_SELECT_COLUMNS);

    // dateAdjustment or default start/end dates
    if (criteria.dateAdjustment != null) {
      selectCols.add(BuilderUtils.getDateAdjustmentExpression(criteria.dateAdjustment,
              criteria.dateAdjustment.startWith == DateAdjustment.DateType.START_DATE ? "op.observation_period_start_date" : "op.observation_period_end_date",
              criteria.dateAdjustment.endWith == DateAdjustment.DateType.START_DATE ? "op.observation_period_start_date" : "op.observation_period_end_date"));
    } else {
      selectCols.add("op.observation_period_start_date as start_date, op.observation_period_end_date as end_date");
    }
    return selectCols;
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

    List<String> whereClauses = super.resolveWhereClauses(criteria);

    if (criteria.first != null && criteria.first == true) {
      whereClauses.add("C.ordinal = 1");
    }

    // check for user defined start/end dates
    if (criteria.userDefinedPeriod != null) {
      Period userDefinedPeriod = criteria.userDefinedPeriod;

      if (userDefinedPeriod.startDate != null) {
        String startDateExpression = BuilderUtils.dateStringToSql(userDefinedPeriod.startDate);
        whereClauses.add(String.format("C.start_date <= %s and C.end_date >= %s", startDateExpression, startDateExpression));
      }

      if (userDefinedPeriod.endDate != null) {
        String endDateExpression = BuilderUtils.dateStringToSql(userDefinedPeriod.endDate);
        whereClauses.add(String.format("C.start_date <= %s and C.end_date >= %s", endDateExpression, endDateExpression));
      }
    }

    // periodStartDate
    if (criteria.periodStartDate != null) {
      whereClauses.add(buildDateRangeClause("C.start_date", criteria.periodStartDate));
    }

    // periodEndDate
    if (criteria.periodEndDate != null) {
      whereClauses.add(buildDateRangeClause("C.end_date", criteria.periodEndDate));
    }

    // periodType
    if (criteria.periodType != null && criteria.periodType.length > 0) {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.periodType);
      whereClauses.add(String.format("C.period_type_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
    }

    // periodLength
    if (criteria.periodLength != null) {
      whereClauses.add(buildNumericRangeClause("DATEDIFF(d,C.start_date, C.end_date)", criteria.periodLength));
    }

    // ageAtStart
    if (criteria.ageAtStart != null) {
      whereClauses.add(buildNumericRangeClause("YEAR(C.start_date) - P.year_of_birth", criteria.ageAtStart));
    }

    // ageAtEnd
    if (criteria.ageAtEnd != null) {
      whereClauses.add(buildNumericRangeClause("YEAR(C.end_date) - P.year_of_birth", criteria.ageAtEnd));
    }

    return whereClauses;
  }
}
