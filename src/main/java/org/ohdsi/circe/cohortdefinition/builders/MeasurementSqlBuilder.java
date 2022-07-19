package org.ohdsi.circe.cohortdefinition.builders;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.Measurement;
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

public class MeasurementSqlBuilder<T extends Measurement> extends CriteriaSqlBuilder<T> {

  private final static String MEASUREMENT_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/measurement.sql");

  // default columns are those that are specified in the template, and dont' need to be added if specifeid in 'additionalColumns'
  private final Set<CriteriaColumn> DEFAULT_COLUMNS = new HashSet<>(Arrays.asList(CriteriaColumn.START_DATE, CriteriaColumn.END_DATE, CriteriaColumn.VISIT_ID));

  // default select columns are the columns that will always be returned from the subquery, but are added to based on the specific criteria
  private final List<String> DEFAULT_SELECT_COLUMNS = new ArrayList<>(Arrays.asList("m.person_id", "m.measurement_id", "m.measurement_concept_id", "m.visit_occurrence_id",
          "m.value_as_number", "m.range_high", "m.range_low"));

  @Override
  protected Set<CriteriaColumn> getDefaultColumns() {
    return DEFAULT_COLUMNS;
  }

  @Override
  protected String getQueryTemplate() {

    return MEASUREMENT_TEMPLATE;
  }

  @Override
  protected String getTableColumnForCriteriaColumn(CriteriaColumn column) {
    switch (column) {
      case DOMAIN_CONCEPT:
        return "C.measurement_concept_id";
      case DURATION:
        return "CAST(1 as int)";
      case VALUE_AS_NUMBER:
        return "C.value_as_number";
      case RANGE_HIGH:
        return "C.range_high";
      case RANGE_LOW:
        return "C.range_low";
      default:
        throw new IllegalArgumentException("Invalid CriteriaColumn for Measurement:" + column.toString());
    }
  }

  @Override
  protected String embedCodesetClause(String query, T criteria) {

    return StringUtils.replace(query, "@codesetClause",
            getCodesetJoinExpression(criteria.codesetId,
                    "m.measurement_concept_id",
                    criteria.measurementSourceConcept,
                    "m.measurement_source_concept_id")
    );
  }

  @Override
  protected String embedOrdinalExpression(String query, T criteria, List<String> whereClauses) {

    // first
    if (criteria.first != null && criteria.first) {
      whereClauses.add("C.ordinal = 1");
      query = StringUtils.replace(query, "@ordinalExpression", ", row_number() over (PARTITION BY m.person_id ORDER BY m.measurement_date, m.measurement_id) as ordinal");
    } else {
      query = StringUtils.replace(query, "@ordinalExpression", "");
    }

    return query;
  }


  @Override
  protected List<String> resolveSelectClauses(T criteria) {

    ArrayList<String> selectCols = new ArrayList<>(DEFAULT_SELECT_COLUMNS);

    // measurementType
    if (criteria.measurementType != null && criteria.measurementType.length > 0) {
      selectCols.add("m.measurement_type_concept_id");
    }

    // operator
    if (criteria.operator != null && criteria.operator.length > 0) {
      selectCols.add("m.operator_concept_id");
    }

    // valueAsConcept
    if (criteria.valueAsConcept != null && criteria.valueAsConcept.length > 0) {
      selectCols.add("m.value_as_concept_id");
    }

    // unit
    if (criteria.unit != null && criteria.unit.length > 0) {
      selectCols.add("m.unit_concept_id");
    }

    // providerSpecialty
    if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0) {
      selectCols.add("m.provider_id");
    }

    // dateAdjustment or default start/end dates
    if (criteria.dateAdjustment != null) {
      selectCols.add(BuilderUtils.getDateAdjustmentExpression(criteria.dateAdjustment,
              criteria.dateAdjustment.startWith == DateAdjustment.DateType.START_DATE ? "m.measurement_date" : "DATEADD(day,1,m.measurement_date)",
              criteria.dateAdjustment.endWith == DateAdjustment.DateType.START_DATE ? "m.measurement_date" : "DATEADD(day,1,m.measurement_date)"));
    } else {
      selectCols.add("m.measurement_date as start_date, DATEADD(day,1,m.measurement_date) as end_date");
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

    // measurementType
    if (criteria.measurementType != null && criteria.measurementType.length > 0) {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.measurementType);
      whereClauses.add(String.format("C.measurement_type_concept_id %s in (%s)", (criteria.measurementTypeExclude ? "not" : ""), StringUtils.join(conceptIds, ",")));
    }

    // operator
    if (criteria.operator != null && criteria.operator.length > 0) {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.operator);
      whereClauses.add(String.format("C.operator_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
    }

    // valueAsNumber
    if (criteria.valueAsNumber != null) {
      whereClauses.add(buildNumericRangeClause("C.value_as_number", criteria.valueAsNumber, ".4f"));
    }

    // valueAsConcept
    if (criteria.valueAsConcept != null && criteria.valueAsConcept.length > 0) {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.valueAsConcept);
      whereClauses.add(String.format("C.value_as_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
    }

    // unit
    if (criteria.unit != null && criteria.unit.length > 0) {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.unit);
      whereClauses.add(String.format("C.unit_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
    }

    // rangeLow
    if (criteria.rangeLow != null) {
      whereClauses.add(buildNumericRangeClause("C.range_low", criteria.rangeLow, ".4f"));
    }

    // rangeHigh
    if (criteria.rangeHigh != null) {
      whereClauses.add(buildNumericRangeClause("C.range_high", criteria.rangeHigh, ".4f"));
    }

    // rangeLowRatio
    if (criteria.rangeLowRatio != null) {
      whereClauses.add(buildNumericRangeClause("(C.value_as_number / NULLIF(C.range_low, 0))", criteria.rangeLowRatio, ".4f"));
    }

    // rangeHighRatio
    if (criteria.rangeHighRatio != null) {
      whereClauses.add(buildNumericRangeClause("(C.value_as_number / NULLIF(C.range_high, 0))", criteria.rangeHighRatio, ".4f"));
    }

    // abnormal
    if (criteria.abnormal != null && criteria.abnormal) {
      whereClauses.add("(C.value_as_number < C.range_low or C.value_as_number > C.range_high or C.value_as_concept_id in (4155142, 4155143))");
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
