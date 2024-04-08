package org.ohdsi.circe.cohortdefinition.builders;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.Measurement;
import org.ohdsi.circe.helper.ResourceHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.DateAdjustment;

import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildDateRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildNumericRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.getCodesetJoinExpression;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.getConceptIdsFromConcepts;
import static org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder.checkColumnTable;

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
      case VALUE_AS_CONCEPT_ID:
        return "C.value_as_concept_id";
      case UNIT:
        return "C.unit_concept_id";

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
  protected String embedOrdinalExpression(String query, T criteria, List<String> whereClauses, BuilderOptions options) {

    // first
    if (criteria.first != null && criteria.first) {
      whereClauses.add("C.ordinal = 1");
      query = StringUtils.replace(query, "@ordinalExpression", ", row_number() over (PARTITION BY m.person_id ORDER BY m.measurement_date, m.measurement_id) as ordinal");
    } else {
      query = StringUtils.replace(query, "@ordinalExpression", "");
    }

    if (options != null && options.isRetainCohortCovariates()) {
      query = StringUtils.replace(query, "@concept_id", ", C.concept_id");
      query = StringUtils.replace(query, "@c_value_as_number", ", C.value_as_number");
      if (criteria.valueAsConcept != null && criteria.valueAsConcept.length > 0) {
        query = StringUtils.replace(query, "@c_value_as_concept_id", ", C.value_as_concept_id");
      }
      // unit
      if (criteria.unit != null && criteria.unit.length > 0) {
        query = StringUtils.replace(query, "@c_unit_concept_id", ", C.unit_concept_id");
      }
      // range_low
      if (criteria.rangeLow != null) {
        query = StringUtils.replace(query, "@c_range_low", ", C.range_low");
      }

      // range_high
      if (criteria.rangeHigh != null) {
        query = StringUtils.replace(query, "@c_range_high", ", C.range_high");
      }

      // providerSpecialty
      if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0) {
        query = StringUtils.replace(query, "@c_provider_id", ", C.provider_id");
      }
      
      // measurementType
      if (criteria.measurementType != null && criteria.measurementType.length > 0) {
          query = StringUtils.replace(query, "@c_measurement_type_concept_id", ", C.measurement_type_concept_id");
      }
      
      // operator
      if (criteria.operator != null && criteria.operator.length > 0) {
          query = StringUtils.replace(query, "@c_operator_concept_id", ", C.operator_concept_id");
      }
    }
    query = StringUtils.replace(query, "@concept_id", "");
    query = StringUtils.replace(query, "@c_value_as_number", "");
    query = StringUtils.replace(query, "@c_value_as_concept_id", "");
    query = StringUtils.replace(query, "@c_unit_concept_id", "");
    query = StringUtils.replace(query, "@c_provider_id", "");
    query = StringUtils.replace(query, "@c_range_low", "");
    query = StringUtils.replace(query, "@c_range_high", "");
    query = StringUtils.replace(query, "@c_measurement_type_concept_id", "");
    query = StringUtils.replace(query, "@c_operator_concept_id", "");
    return query;
  }


  @Override
  protected List<String> resolveSelectClauses(T criteria, BuilderOptions builderOptions) {

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
    // If save covariates is included, add the concept_id column
    if (builderOptions != null && builderOptions.isRetainCohortCovariates()) {
      selectCols.add("m.measurement_concept_id concept_id");
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
  public String embedWindowedCriteriaQuery(String query, Criteria criteria, BuilderOptions options) {
		ArrayList<String> selectCols = new ArrayList<>();
		ArrayList<String> selectGroupCols = new ArrayList<>();
		selectCols.add(", cc.value_as_number");
		selectGroupCols.add(", cc.value_as_number");
		if (checkColumnTable(criteria, "valueAsString") && !selectCols.contains(", cc.value_as_string")) {
			selectCols.add(", cc.value_as_string");
			selectGroupCols.add(", cc.value_as_string");
		} else {
			selectCols.add(", null as value_as_string");
		}
		if (checkColumnTable(criteria, "valueAsConceptId")
				&& !selectCols.contains(", cc.value_as_concept_id")) {
			selectCols.add(", cc.value_as_concept_id");
			selectGroupCols.add(", cc.value_as_concept_id");
		} else {
			selectCols.add(", CAST(null as int) value_as_concept_id");
		}
		if (checkColumnTable(criteria, "unit") && !selectCols.contains(", cc.unit_concept_id")) {
			selectCols.add(", cc.unit_concept_id");
			selectGroupCols.add(", cc.unit_concept_id");
		} else {
			selectCols.add(", CAST(null as int) unit_concept_id");
		}
		if (checkColumnTable(criteria, "providerSpecialty") && !selectCols.contains(", cc.provider_id")) {
			selectCols.add(", cc.provider_id");
			selectGroupCols.add(", cc.provider_id");
		} else {
			selectCols.add(", CAST(null as int) provider_id");
		}
		if (checkColumnTable(criteria, "qualifier") && !selectCols.contains(", cc.qualifier_concept_id")) {
			selectCols.add(", cc.qualifier_concept_id");
			selectGroupCols.add(", cc.qualifier_concept_id");
		} else {
			selectCols.add(", CAST(null as int) qualifier_concept_id");
		}
		if (checkColumnTable(criteria, "observationType")
				&& !selectCols.contains(", cc.observation_type_concept_id")) {
			selectCols.add(", cc.observation_type_concept_id");
			selectGroupCols.add(", cc.observation_type_concept_id");
		} else {
			selectCols.add(", CAST(null as int) observation_type_concept_id");
		}
		if (checkColumnTable(criteria, "rangeLow") && !selectCols.contains(", cc.range_low")) {
			selectCols.add(", cc.range_low");
			selectGroupCols.add(", cc.range_low");
		} else {
			selectCols.add(", CAST(null as numeric) range_low");
		}
		if (checkColumnTable(criteria, "rangeHigh") && !selectCols.contains(", cc.range_high")) {
			selectCols.add(", cc.range_high");
			selectGroupCols.add(", cc.range_high");
		} else {
			selectCols.add(", CAST(null as numeric) range_high");
		}
		query = StringUtils.replace(query, "@additionColumnscc", StringUtils.join(selectCols, ""));
		query = StringUtils.replace(query, "@additionColumnGroupscc", StringUtils.join(selectGroupCols, ""));
		return query;
	}

	public String embedWindowedCriteriaQueryP(String query, Criteria criteria, BuilderOptions options) {
		ArrayList<String> selectColsA = new ArrayList<>();
		selectColsA.add(", A.value_as_number");
		if (checkColumnTable(criteria, "valueAsString") && !selectColsA.contains(", A.value_as_string")) {
			selectColsA.add(", A.value_as_string");
		} else {
			selectColsA.add(", null as value_as_string");
		}
		if (checkColumnTable(criteria, "valueAsConceptId") && !selectColsA.contains(", A.value_as_concept_id")) {
			selectColsA.add(", A.value_as_concept_id");
		} else {
			selectColsA.add(", CAST(null as int) value_as_concept_id");
		}
		if (checkColumnTable(criteria, "unit") && !selectColsA.contains(", A.unit_concept_id")) {
			selectColsA.add(", A.unit_concept_id");
		} else {
			selectColsA.add(", CAST(null as int)  unit_concept_id");
		}
		if (checkColumnTable(criteria, "providerSpecialty") && !selectColsA.contains(", A.provider_id")) {
			selectColsA.add(", A.provider_id");
		} else {
			selectColsA.add(", CAST(null as int)  provider_id");
		}
		if (checkColumnTable(criteria, "qualifier") && !selectColsA.contains(", A.qualifier_concept_id")) {
			selectColsA.add(", A.qualifier_concept_id");
		} else {
			selectColsA.add(", CAST(null as int) qualifier_concept_id");
		}
		if (checkColumnTable(criteria, "observationType") && !selectColsA.contains(", A.observation_type_concept_id")) {
			selectColsA.add(", A.observation_type_concept_id");
		} else {
			selectColsA.add(", CAST(null as int) observation_type_concept_id");
		}
		if (checkColumnTable(criteria, "rangeLow") && !selectColsA.contains(", A.range_low")) {
			selectColsA.add(", A.range_low");
		} else {
			selectColsA.add(", CAST(null as numeric) range_low");
		}
		if (checkColumnTable(criteria, "rangeHigh") && !selectColsA.contains(", A.range_high")) {
			selectColsA.add(", A.range_high");
		} else {
			selectColsA.add(", CAST(null as numeric) range_high");
		}
		query = StringUtils.replace(query, "@p.additionColumns", StringUtils.join(selectColsA, ""));
		return query;
	}

	public String embedCriteriaGroup(String query, Criteria criteria) {
		ArrayList<String> selectColsCQ = new ArrayList<>();
		ArrayList<String> selectColsG = new ArrayList<>();
		selectColsCQ.add(", CQ.value_as_number");
		selectColsG.add(", G.value_as_number");
		selectColsCQ.add(", CQ.value_as_string");
		selectColsG.add(", G.value_as_string");
		selectColsCQ.add(", CQ.value_as_concept_id");
		selectColsG.add(", G.value_as_concept_id");
		selectColsCQ.add(", CQ.unit_concept_id");
		selectColsG.add(", G.unit_concept_id");
		selectColsCQ.add(", CQ.provider_id");
		selectColsG.add(", G.provider_id");
		selectColsCQ.add(", CQ.qualifier_concept_id");
		selectColsG.add(", G.qualifier_concept_id");
		selectColsCQ.add(", CQ.observation_type_concept_id");
		selectColsG.add(", G.observation_type_concept_id");
		selectColsCQ.add(", CQ.range_low");
		selectColsG.add(", G.range_low");
		selectColsCQ.add(", CQ.range_high");
		selectColsG.add(", G.range_high");
		query = StringUtils.replace(query, "@e.additonColumns", StringUtils.join(selectColsCQ, ""));
    query = StringUtils.replace(query, "@e.additonGroupColumns", StringUtils.join(selectColsCQ, ""));
		query = StringUtils.replace(query, "@additonColumnsGroup", StringUtils.join(selectColsG, ""));
		return query;
	}

	public String embedWrapCriteriaQuery(String query, Criteria criteria, List<String> selectColsPE) {
		ArrayList<String> selectCols = new ArrayList<>();
		if (checkColumnTable(criteria, "valueAsNumber")) {
			selectCols.add(", Q.value_as_number");
			selectColsPE.add(", AC.value_as_number");
		}
		if (checkColumnTable(criteria, "valueAsString") && !selectCols.contains(", Q.value_as_string")) {
			selectCols.add(", Q.value_as_string");
			selectColsPE.add(", AC.value_as_string");
		}
		if (checkColumnTable(criteria, "valueAsConceptId") && !selectCols.contains(", Q.value_as_concept_id")) {
			selectCols.add(", Q.value_as_concept_id");
			selectColsPE.add(", AC.value_as_concept_id");
		}
		if (checkColumnTable(criteria, "unit") && !selectCols.contains(", Q.unit_concept_id")) {
			selectCols.add(", Q.unit_concept_id");
			selectColsPE.add(", AC.unit_concept_id");
		}
		if (checkColumnTable(criteria, "providerSpecialty") && !selectCols.contains(", Q.provider_id")) {
			selectCols.add(", Q.provider_id");
			selectColsPE.add(", AC.provider_id");
		}
		if (checkColumnTable(criteria, "qualifier") && !selectCols.contains(", Q.qualifier_concept_id")) {
			selectCols.add(", Q.qualifier_concept_id");
			selectColsPE.add(", AC.qualifier_concept_id");
		}
		if (checkColumnTable(criteria, "observationType") && !selectCols.contains(", Q.observation_type_concept_id")) {
			selectCols.add(", Q.observation_type_concept_id");
			selectColsPE.add(", AC.observation_type_concept_id");
		}
		if (checkColumnTable(criteria, "rangeLow") && !selectCols.contains(", Q.range_low")) {
			selectCols.add(", Q.range_low");
			selectColsPE.add(", AC.range_low");
		}
		if (checkColumnTable(criteria, "rangeHigh") && !selectCols.contains(", Q.range_high")) {
			selectCols.add(", Q.range_high");
			selectColsPE.add(", AC.range_high");
		}
		query = StringUtils.replace(query, "@QAdditionalColumnsInclusionN", StringUtils.join(selectCols, ""));
		return query;
	}
}
