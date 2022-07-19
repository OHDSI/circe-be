package org.ohdsi.circe.cohortdefinition.builders;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.ProcedureOccurrence;
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

public class ProcedureOccurrenceSqlBuilder<T extends ProcedureOccurrence> extends CriteriaSqlBuilder<T> {

  private final static String PROCEDURE_OCCURRENCE_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/procedureOccurrence.sql");

  // default columns are those that are specified in the template, and dont' need to be added if specifeid in 'additionalColumns'
  private final Set<CriteriaColumn> DEFAULT_COLUMNS = new HashSet<>(Arrays.asList(CriteriaColumn.START_DATE, CriteriaColumn.END_DATE, CriteriaColumn.VISIT_ID));

  // default select columns are the columns that will always be returned from the subquery, but are added to based on the specific criteria
  private final List<String> DEFAULT_SELECT_COLUMNS = new ArrayList<>(Arrays.asList("po.person_id", "po.procedure_occurrence_id",
          "po.procedure_concept_id", "po.visit_occurrence_id", "po.quantity"));

  @Override
  protected Set<CriteriaColumn> getDefaultColumns() {
    return DEFAULT_COLUMNS;
  }

  @Override
  protected String getQueryTemplate() {

    return PROCEDURE_OCCURRENCE_TEMPLATE;
  }

  @Override
  protected String getTableColumnForCriteriaColumn(CriteriaColumn column) {
    switch (column) {
      case DOMAIN_CONCEPT:
        return "C.procedure_concept_id";
      case DURATION:
        return "CAST(1 as int)";
      case QUANTITY:
        return "C.quantity";
      default:
        throw new IllegalArgumentException("Invalid CriteriaColumn for Procedure Occurrence:" + column.toString());
    }
  }

  @Override
  protected String embedCodesetClause(String query, T criteria) {

    return StringUtils.replace(query, "@codesetClause",
            getCodesetJoinExpression(criteria.codesetId,
                    "po.procedure_concept_id",
                    criteria.procedureSourceConcept,
                    "po.procedure_source_concept_id")
    );
  }

  @Override
  protected String embedOrdinalExpression(String query, T criteria, List<String> whereClauses) {

    // first
    if (criteria.first != null && criteria.first) {
      whereClauses.add("C.ordinal = 1");
      query = StringUtils.replace(query, "@ordinalExpression", ", row_number() over (PARTITION BY po.person_id ORDER BY po.procedure_date, po.procedure_occurrence_id) as ordinal");
    } else {
      query = StringUtils.replace(query, "@ordinalExpression", "");
    }
    return query;
  }

  @Override
  protected List<String> resolveSelectClauses(T criteria) {

    ArrayList<String> selectCols = new ArrayList<>(DEFAULT_SELECT_COLUMNS);

    // procedureType
    if (criteria.procedureType != null && criteria.procedureType.length > 0) {
      selectCols.add("po.procedure_type_concept_id");
    }

    // modifier
    if (criteria.modifier != null && criteria.modifier.length > 0) {
      selectCols.add("po.modifier_concept_id");
    }

    // providerSpecialty
    if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0) {
      selectCols.add("po.provider_id");
    }

    // dateAdjustment or default start/end dates
    if (criteria.dateAdjustment != null) {
      selectCols.add(BuilderUtils.getDateAdjustmentExpression(criteria.dateAdjustment,
              criteria.dateAdjustment.startWith == DateAdjustment.DateType.START_DATE ? "po.procedure_date" : "DATEADD(day,1,po.procedure_date)",
              criteria.dateAdjustment.endWith == DateAdjustment.DateType.START_DATE ? "po.procedure_date" : "DATEADD(day,1,po.procedure_date)"));
    } else {
      selectCols.add("po.procedure_date as start_date, DATEADD(day,1,po.procedure_date) as end_date");
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

    // procedureType
    if (criteria.procedureType != null && criteria.procedureType.length > 0) {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.procedureType);
      whereClauses.add(String.format("C.procedure_type_concept_id %s in (%s)", (criteria.procedureTypeExclude ? "not" : ""), StringUtils.join(conceptIds, ",")));
    }

    // modifier
    if (criteria.modifier != null && criteria.modifier.length > 0) {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.modifier);
      whereClauses.add(String.format("C.modifier_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
    }

    // quantity
    if (criteria.quantity != null) {
      whereClauses.add(buildNumericRangeClause("C.quantity", criteria.quantity));
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

    return whereClauses;
  }
}
