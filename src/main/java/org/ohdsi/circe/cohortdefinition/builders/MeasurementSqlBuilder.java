package org.ohdsi.circe.cohortdefinition.builders;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.Measurement;
import org.ohdsi.circe.helper.ResourceHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildDateRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildNumericRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.getCodesetJoinExpression;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.getConceptIdsFromConcepts;

public class MeasurementSqlBuilder<T extends Measurement> extends BaseCriteriaSqlBuilder<T> {

    private final static String MEASUREMENT_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/measurement.sql");

    @Override
    protected String getQueryTemplate() {

        return MEASUREMENT_TEMPLATE;
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
    protected List<String> resolveWhereClauses(T criteria, Map<String, String> additionalVariables) {

        List<String> whereClauses = new ArrayList<>();

        // occurrenceStartDate
        if (criteria.occurrenceStartDate != null) {
            whereClauses.add(buildDateRangeClause("C.measurement_date", criteria.occurrenceStartDate));
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
            whereClauses.add(buildNumericRangeClause("YEAR(C.measurement_date) - P.year_of_birth", criteria.age));
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