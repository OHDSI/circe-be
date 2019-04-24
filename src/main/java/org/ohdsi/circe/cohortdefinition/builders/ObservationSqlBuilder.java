package org.ohdsi.circe.cohortdefinition.builders;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.Observation;
import org.ohdsi.circe.helper.ResourceHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildDateRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildNumericRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildTextFilterClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.getCodesetJoinExpression;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.getConceptIdsFromConcepts;

public class ObservationSqlBuilder<T extends Observation> extends BaseCriteriaSqlBuilder<T> {

    private final static String OBSERVATION_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/observation.sql");

    @Override
    protected String getQueryTemplate() {

        return OBSERVATION_TEMPLATE;
    }

    @Override
    protected String embedCodesetClause(String query, T criteria) {

        return StringUtils.replace(query, "@codesetClause",
                getCodesetJoinExpression(criteria.codesetId,
                        "o.observation_concept_id",
                        criteria.observationSourceConcept,
                        "o.observation_source_concept_id")
        );
    }

    @Override
    protected String embedOrdinalExpression(String query, T criteria, List<String> whereClauses) {

        // first
        if (criteria.first != null && criteria.first) {
            whereClauses.add("C.ordinal = 1");
            query = StringUtils.replace(query, "@ordinalExpression", ", row_number() over (PARTITION BY o.person_id ORDER BY o.observation_date, o.observation_id) as ordinal");
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
            whereClauses.add(buildDateRangeClause("C.observation_date", criteria.occurrenceStartDate));
        }

        // measurementType
        if (criteria.observationType != null && criteria.observationType.length > 0) {
            ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.observationType);
            whereClauses.add(String.format("C.observation_type_concept_id %s in (%s)", (criteria.observationTypeExclude ? "not" : ""), StringUtils.join(conceptIds, ",")));
        }

        // valueAsNumber
        if (criteria.valueAsNumber != null) {
            whereClauses.add(buildNumericRangeClause("C.value_as_number", criteria.valueAsNumber, ".4f"));
        }

        // valueAsString
        if (criteria.valueAsString != null) {
            whereClauses.add(buildTextFilterClause("C.value_as_string", criteria.valueAsString));
        }

        // valueAsConcept
        if (criteria.valueAsConcept != null && criteria.valueAsConcept.length > 0) {
            ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.valueAsConcept);
            whereClauses.add(String.format("C.value_as_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
        }

        // qualifier
        if (criteria.qualifier != null && criteria.qualifier.length > 0) {
            ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.qualifier);
            whereClauses.add(String.format("C.qualifier_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
        }

        // unit
        if (criteria.unit != null && criteria.unit.length > 0) {
            ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.unit);
            whereClauses.add(String.format("C.unit_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
        }

        // age
        if (criteria.age != null) {
            whereClauses.add(buildNumericRangeClause("YEAR(C.observation_date) - P.year_of_birth", criteria.age));
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
