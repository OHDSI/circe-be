package org.ohdsi.circe.cohortdefinition.builders;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.Death;
import org.ohdsi.circe.helper.ResourceHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.ohdsi.circe.cohortdefinition.DateAdjustment;

import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildDateRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildNumericRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.getCodesetJoinExpression;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.getConceptIdsFromConcepts;

public class DeathSqlBuilder<T extends Death> extends CriteriaSqlBuilder<T> {

  private final static String DEATH_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/death.sql");

  // default columns are those that are specified in the template, and dont' need to be added if specifeid in 'additionalColumns'
  private final Set<CriteriaColumn> DEFAULT_COLUMNS = new HashSet<>(Arrays.asList(CriteriaColumn.START_DATE, CriteriaColumn.END_DATE, CriteriaColumn.VISIT_ID));

  // default select columns are the columns that will always be returned from the subquery, but are added to based on the specific criteria
  private final List<String> DEFAULT_SELECT_COLUMNS = new ArrayList<>(Arrays.asList("d.person_id", "d.cause_concept_id"));

  @Override
  protected Set<CriteriaColumn> getDefaultColumns() {
    return DEFAULT_COLUMNS;
  }

  @Override
  protected String getQueryTemplate() {

    return DEATH_TEMPLATE;
  }

  @Override
  protected String getTableColumnForCriteriaColumn(CriteriaColumn column) {
    switch (column) {
      case DOMAIN_CONCEPT:
        return "coalesce(C.cause_concept_id,0)";
      case DURATION:
        return "CAST(1 as int)";
      default:
        throw new IllegalArgumentException("Invalid CriteriaColumn for Death:" + column.toString());
    }
  }

  @Override
  protected String embedCodesetClause(String query, T criteria) {

    return StringUtils.replace(query, "@codesetClause",
            getCodesetJoinExpression(criteria.codesetId,
                    "d.cause_concept_id",
                    criteria.deathSourceConcept,
                    "d.cause_source_concept_id")
    );
  }

  @Override
  protected String embedOrdinalExpression(String query, T criteria, List<String> whereClauses) {

    return query;
  }

  @Override
  protected List<String> resolveSelectClauses(T criteria) {
    ArrayList<String> selectCols = new ArrayList<>(DEFAULT_SELECT_COLUMNS);
    // Condition Type
    if (criteria.deathType != null && criteria.deathType.length > 0) {
      selectCols.add("d.death_type_concept_id");
    }
 
    // dateAdjustment or default start/end dates
    if (criteria.dateAdjustment != null) {
      selectCols.add(BuilderUtils.getDateAdjustmentExpression(criteria.dateAdjustment, "d.death_date", "DATEADD(day,1,d.death_date)"));
    } else {
      selectCols.add("d.death_date as start_date, DATEADD(day,1,d.death_date) as end_date");
    }
    return selectCols;
  }

  @Override
  protected List<String> resolveJoinClauses(T criteria) {

    List<String> joinClauses = new ArrayList<>();

    // join to PERSON
    if (criteria.age != null || (criteria.gender != null && criteria.gender.length > 0)) {
      joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
    }

    return joinClauses;
  }

  @Override
  protected List<String> resolveWhereClauses(T criteria) {

    List<String> whereClauses = super.resolveWhereClauses(criteria);

    // occurrenceStartDate
    if (criteria.occurrenceStartDate != null) {
      whereClauses.add(buildDateRangeClause("C.start_date", criteria.occurrenceStartDate));
    }

    // deathType
    if (criteria.deathType != null && criteria.deathType.length > 0) {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.deathType);
      whereClauses.add(String.format("C.death_type_concept_id %s in (%s)", (criteria.deathTypeExclude ? "not" : ""), StringUtils.join(conceptIds, ",")));
    }

    // age
    if (criteria.age != null) {
      whereClauses.add(buildNumericRangeClause("YEAR(C.start_date) - P.year_of_birth", criteria.age));
    }

    // gender
    if (criteria.gender != null && criteria.gender.length > 0) {
      whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.gender), ",")));
    }

    return whereClauses;
  }
}
