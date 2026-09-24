/*
 *
 * Copyright 2017 Observational Health Data Sciences and Informatics
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Authors: Chris Knoll
 *
 */
package org.ohdsi.circe.cohortdefinition.builders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.circe.cohortdefinition.CustomEra;
import org.ohdsi.circe.cohortdefinition.DateAdjustment;
import org.ohdsi.circe.helper.ResourceHelper;

import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildDateRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildNumericRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.getCodesetInExpression;

public class CustomEraSqlBuilder<T extends CustomEra> extends CriteriaSqlBuilder<T> {

  private final static String CUSTOM_ERA_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/customEra.sql");
  private final static CohortExpressionQueryBuilder CUSTOM_ERA_QUERY_BUILDER = new CohortExpressionQueryBuilder();

  private final Set<CriteriaColumn> DEFAULT_COLUMNS = new HashSet<>(Arrays.asList(CriteriaColumn.START_DATE, CriteriaColumn.END_DATE, CriteriaColumn.VISIT_ID));

  @Override
  protected Set<CriteriaColumn> getDefaultColumns() {
    return DEFAULT_COLUMNS;
  }

  @Override
  protected String getQueryTemplate() {
    return CUSTOM_ERA_TEMPLATE;
  }

  @Override
  protected String getTableColumnForCriteriaColumn(CriteriaColumn column) {
    switch (column) {
      case DURATION:
        return "DATEDIFF(d, C.start_date, C.end_date)";
      default:
        throw new IllegalArgumentException("Invalid CriteriaColumn for Custom Era:" + column.toString());
    }
  }

  @Override
  protected String embedCodesetClause(String query, T criteria) {
    return query;
  }

  @Override
  protected String embedOrdinalExpression(String query, T criteria, List<String> whereClauses) {
    if (criteria.first != null && criteria.first) {
      whereClauses.add("C.ordinal = 1");
      query = StringUtils.replace(query, "@ordinalExpression", ", row_number() over (PARTITION BY E.person_id ORDER BY E.start_date, E.end_date) as ordinal");
    } else {
      query = StringUtils.replace(query, "@ordinalExpression", "");
    }
    return query;
  }

  @Override
  protected List<String> resolveSelectClauses(T criteria) {
    ArrayList<String> selectCols = new ArrayList<>();
    selectCols.add("E.person_id");
    selectCols.add("row_number() over (ORDER BY E.person_id, E.start_date, E.end_date) as event_id");

    if (criteria.dateAdjustment != null) {
      selectCols.add(BuilderUtils.getDateAdjustmentExpression(criteria.dateAdjustment,
              criteria.dateAdjustment.startWith == DateAdjustment.DateType.START_DATE ? "E.start_date" : "E.end_date",
              criteria.dateAdjustment.endWith == DateAdjustment.DateType.START_DATE ? "E.start_date" : "E.end_date"));
    } else {
      selectCols.add("E.start_date as start_date, E.end_date as end_date");
    }

    return selectCols;
  }

  @Override
  protected List<String> resolveJoinClauses(T criteria) {
    List<String> joinClauses = new ArrayList<>();
    if (criteria.ageAtStart != null || criteria.genderCS != null) {
      joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
    }
    return joinClauses;
  }

  @Override
  protected List<String> resolveWhereClauses(T criteria) {
    List<String> whereClauses = super.resolveWhereClauses(criteria);

    if (criteria.startDate != null) {
      whereClauses.add(buildDateRangeClause("C.start_date", criteria.startDate));
    }

    if (criteria.endDate != null) {
      whereClauses.add(buildDateRangeClause("C.end_date", criteria.endDate));
    }

    if (criteria.ageAtStart != null) {
      whereClauses.add(buildNumericRangeClause("YEAR(C.start_date) - P.year_of_birth", criteria.ageAtStart));
    }

    if (criteria.genderCS != null) {
      whereClauses.add(getCodesetInExpression("P.gender_concept_id", criteria.genderCS));
    }

    if (criteria.duration != null) {
      whereClauses.add(buildNumericRangeClause("DATEDIFF(d,C.start_date, C.end_date)", criteria.duration));
    }

    return whereClauses;
  }

  private String getCustomEraCriteriaQuery(T criteria, BuilderOptions options) {
    if (criteria.criteriaList == null || criteria.criteriaList.length == 0) {
      throw new RuntimeException("CustomEra.CriteriaList can not be null or empty.");
    }

    ArrayList<String> criteriaQueries = new ArrayList<>();
    for (org.ohdsi.circe.cohortdefinition.Criteria c : criteria.criteriaList) {
      String criteriaQuery = c.accept(CUSTOM_ERA_QUERY_BUILDER, options);
      criteriaQueries.add(String.format("select person_id, start_date, end_date from (%s) C", criteriaQuery));
    }

    return StringUtils.join(criteriaQueries, "\nUNION ALL\n");
  }

  public String getCriteriaSql(T criteria, BuilderOptions options, String criteriaQuery) {
    String query = super.getCriteriaSql(criteria, options);
    query = StringUtils.replace(query, "@eraconstructorpad", Integer.toString(criteria.gapDays == null ? 0 : criteria.gapDays));
    return StringUtils.replace(query, "@criteriaQueries", criteriaQuery);
  }

  @Override
  public String getCriteriaSql(T criteria, BuilderOptions options) {
    return getCriteriaSql(criteria, options, getCustomEraCriteriaQuery(criteria, options));
  }
}