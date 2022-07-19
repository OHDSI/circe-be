package org.ohdsi.circe.cohortdefinition.builders;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.ConditionOccurrence;
import org.ohdsi.circe.helper.ResourceHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.ohdsi.circe.cohortdefinition.DateAdjustment;

import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildDateRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildNumericRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildTextFilterClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.getCodesetJoinExpression;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.getConceptIdsFromConcepts;

public class ConditionOccurrenceSqlBuilder<T extends ConditionOccurrence> extends CriteriaSqlBuilder<T> {

  private final static String CONDITION_OCCURRENCE_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/conditionOccurrence.sql");

  // default columns are those that are specified in the template, and dont' need to be added if specifeid in 'additionalColumns'
  private final Set<CriteriaColumn> DEFAULT_COLUMNS = new HashSet<>(Arrays.asList(CriteriaColumn.START_DATE, CriteriaColumn.END_DATE, CriteriaColumn.VISIT_ID));

  // default select columns are the columns that will always be returned from the subquery, but are added to based on the specific criteria
  private final List<String> DEFAULT_SELECT_COLUMNS = new ArrayList<>(Arrays.asList("co.person_id", "co.condition_occurrence_id", "co.condition_concept_id", "co.visit_occurrence_id"));

  @Override
  protected Set<CriteriaColumn> getDefaultColumns() {
    return DEFAULT_COLUMNS;
  }

  @Override
  protected String getQueryTemplate() {

    return CONDITION_OCCURRENCE_TEMPLATE;
  }

  @Override
  protected String getTableColumnForCriteriaColumn(CriteriaColumn column) {
    switch (column) {
      case DOMAIN_CONCEPT:
        return "C.condition_concept_id";
      case DURATION:
        return "(DATEDIFF(d,C.start_date, C.end_date))";
      default:
        throw new IllegalArgumentException("Invalid CriteriaColumn for Condition Occurrence:" + column.toString());
    }
  }

  @Override
  protected String embedCodesetClause(String query, T criteria) {

    ArrayList<String> joinClauses = new ArrayList<>();

    joinClauses.add(getCodesetJoinExpression(criteria.codesetId,
            "co.condition_concept_id",
            criteria.conditionSourceConcept,
            "co.condition_source_concept_id"));
    return StringUtils.replace(query, "@codesetClause", StringUtils.join(joinClauses, "\n"));
  }

  @Override
  protected String embedOrdinalExpression(String query, T criteria, List<String> whereClauses) {

    // first
    if (criteria.first != null && criteria.first == true) {
      whereClauses.add("C.ordinal = 1");
      query = StringUtils.replace(query, "@ordinalExpression", ", row_number() over (PARTITION BY co.person_id ORDER BY co.condition_start_date, co.condition_occurrence_id) as ordinal");
    } else {
      query = StringUtils.replace(query, "@ordinalExpression", "");
    }

    return query;
  }

  @Override
  protected List<String> resolveSelectClauses(T criteria) {
    ArrayList<String> selectCols = new ArrayList<>(DEFAULT_SELECT_COLUMNS);
    // Condition Type
    if (criteria.conditionType != null && criteria.conditionType.length > 0) {
      selectCols.add("co.condition_type_concept_id");
    }
    // Stop Reason
    if (criteria.stopReason != null) {
      selectCols.add("co.stop_reason");
    }
    // providerSpecialty
    if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0) {
      selectCols.add("co.provider_id");
    }
    // conditionStatus
    if (criteria.conditionStatus != null && criteria.conditionStatus.length > 0) {
      selectCols.add("co.condition_status_concept_id");
    }
    // dateAdjustment or default start/end dates
    if (criteria.dateAdjustment != null) {
      selectCols.add(BuilderUtils.getDateAdjustmentExpression(criteria.dateAdjustment,
              criteria.dateAdjustment.startWith == DateAdjustment.DateType.START_DATE ? "co.condition_start_date" : "COALESCE(co.condition_end_date, DATEADD(day,1,co.condition_start_date))",
              criteria.dateAdjustment.endWith == DateAdjustment.DateType.START_DATE ? "co.condition_start_date" : "COALESCE(co.condition_end_date, DATEADD(day,1,co.condition_start_date))"));
    } else {
      selectCols.add("co.condition_start_date as start_date, COALESCE(co.condition_end_date, DATEADD(day,1,co.condition_start_date)) as end_date");
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
    if (criteria.visitType != null && criteria.visitType.length > 0) {
      joinClauses.add("JOIN @cdm_database_schema.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id");
    }
    if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0) {
      joinClauses.add("LEFT JOIN @cdm_database_schema.PROVIDER PR on C.provider_id = PR.provider_id");
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

    // occurrenceEndDate
    if (criteria.occurrenceEndDate != null) {
      whereClauses.add(buildDateRangeClause("C.end_date", criteria.occurrenceEndDate));
    }

    // conditionType
    if (criteria.conditionType != null && criteria.conditionType.length > 0) {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.conditionType);
      whereClauses.add(String.format("C.condition_type_concept_id %s in (%s)", (Optional.ofNullable(criteria.conditionTypeExclude).orElse(false) ? "not" : ""), StringUtils.join(conceptIds, ",")));
    }

    // Stop Reason
    if (criteria.stopReason != null) {
      whereClauses.add(buildTextFilterClause("C.stop_reason", criteria.stopReason));
    }

    // age
    if (criteria.age != null) {
      whereClauses.add(buildNumericRangeClause("YEAR(C.start_date) - P.year_of_birth", criteria.age));
    }

    // gender
    if (criteria.gender != null && criteria.gender.length > 0) {
      whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.gender), ",")));
    }

    // providerSpecialty
    if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0) {
      whereClauses.add(String.format("PR.specialty_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.providerSpecialty), ",")));
    }

    // visitType
    if (criteria.visitType != null && criteria.visitType.length > 0) {
      whereClauses.add(String.format("V.visit_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.visitType), ",")));
    }

    // conditionStatus
    if (criteria.conditionStatus != null && criteria.conditionStatus.length > 0) {
      whereClauses.add(String.format("C.condition_status_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.conditionStatus), ",")));
    }

    return whereClauses;
  }
}
