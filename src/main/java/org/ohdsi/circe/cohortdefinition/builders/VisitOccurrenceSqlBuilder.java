package org.ohdsi.circe.cohortdefinition.builders;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.VisitOccurrence;
import org.ohdsi.circe.helper.ResourceHelper;

import java.util.ArrayList;
import java.util.List;

public class VisitOccurrenceSqlBuilder<T extends VisitOccurrence> extends BaseCriteriaSqlBuilder<T> {

    private final static String VISIT_OCCURRENCE_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/visitOccurrence.sql");

    @Override
    protected String getQueryTemplate() {
        return VISIT_OCCURRENCE_TEMPLATE;
    }

    @Override
    protected String embedCodesetClause(String query, T criteria) {

        return StringUtils.replace(query, "@codesetClause",
                BuilderUtils.getCodesetJoinExpression(criteria.codesetId,
                        "vo.visit_concept_id",
                        criteria.visitSourceConcept,
                        "vo.visit_source_concept_id")
        );
    }

    @Override
    protected String embedOrdinalExpression(String query, T criteria, List<String> whereClauses) {
        // first
        if (criteria.first != null && criteria.first == true) {
            whereClauses.add("C.ordinal = 1");
            query = StringUtils.replace(query, "@ordinalExpression", ", row_number() over (PARTITION BY vo.person_id ORDER BY vo.visit_start_date, vo.visit_occurrence_id) as ordinal");
        } else {
            query = StringUtils.replace(query, "@ordinalExpression", "");
        }
        return query;
    }

    @Override
    protected List<String> resolveJoinClauses(T criteria) {

        List<String> joinClauses = new ArrayList<>();

        if (criteria.age != null || (criteria.gender != null && criteria.gender.length > 0)) // join to PERSON
            joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
        if ((criteria.placeOfService != null && criteria.placeOfService.length > 0) || criteria.placeOfServiceLocation != null)
            joinClauses.add("JOIN @cdm_database_schema.CARE_SITE CS on C.care_site_id = CS.care_site_id");
        if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0)
            joinClauses.add("LEFT JOIN @cdm_database_schema.PROVIDER PR on C.provider_id = PR.provider_id");

        if (criteria.placeOfServiceLocation != null) {
            addFilteringByCareSiteLocationRegion(joinClauses, criteria.placeOfServiceLocation);
        }

        return joinClauses;
    }

    @Override
    protected List<String> resolveWhereClauses(T criteria) {

        List<String> whereClauses = new ArrayList<>();

        // occurrenceStartDate
        if (criteria.occurrenceStartDate != null) {
            whereClauses.add(BuilderUtils.buildDateRangeClause("C.visit_start_date", criteria.occurrenceStartDate));
        }

        // occurrenceEndDate
        if (criteria.occurrenceEndDate != null) {
            whereClauses.add(BuilderUtils.buildDateRangeClause("C.visit_end_date", criteria.occurrenceEndDate));
        }

        // visitType
        if (criteria.visitType != null && criteria.visitType.length > 0) {
            ArrayList<Long> conceptIds = BuilderUtils.getConceptIdsFromConcepts(criteria.visitType);
            whereClauses.add(String.format("C.visit_type_concept_id %s in (%s)", (criteria.visitTypeExclude ? "not" : ""), StringUtils.join(conceptIds, ",")));
        }

        // visitLength
        if (criteria.visitLength != null) {
            whereClauses.add(BuilderUtils.buildNumericRangeClause("DATEDIFF(d,C.visit_start_date, C.visit_end_date)", criteria.visitLength));
        }

        // age
        if (criteria.age != null) {
            whereClauses.add(BuilderUtils.buildNumericRangeClause("YEAR(C.visit_start_date) - P.year_of_birth", criteria.age));
        }

        // gender
        if (criteria.gender != null && criteria.gender.length > 0) {
            whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(BuilderUtils.getConceptIdsFromConcepts(criteria.gender), ",")));
        }

        // providerSpecialty
        if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0) {
            whereClauses.add(String.format("PR.specialty_concept_id in (%s)", StringUtils.join(BuilderUtils.getConceptIdsFromConcepts(criteria.providerSpecialty), ",")));
        }

        // placeOfService
        if (criteria.placeOfService != null && criteria.placeOfService.length > 0) {
            whereClauses.add(String.format("CS.place_of_service_concept_id in (%s)", StringUtils.join(BuilderUtils.getConceptIdsFromConcepts(criteria.placeOfService), ",")));
        }

        return whereClauses;
    }

    protected void addFilteringByCareSiteLocationRegion(List<String> joinClauses, Integer codesetId) {

        joinClauses.add(getLocationHistoryJoin("LH", "CARE_SITE", "C.care_site_id"));
        joinClauses.add("JOIN @cdm_database_schema.LOCATION LOC on LOC.location_id = LH.location_id");
        joinClauses.add(
                BuilderUtils.getCodesetJoinExpression(
                        codesetId,
                        "LOC.region_concept_id",
                        null,
                        null
                )
        );
    }

    protected String getLocationHistoryJoin(String alias, String domain, String entityIdField) {

        return "JOIN @cdm_database_schema.LOCATION_HISTORY " + alias + " " +
                "on " + alias + ".entity_id = " + entityIdField + " " +
                "AND " + alias + ".domain_id = '" + domain + "' " +
                "AND C.visit_start_date >= " + alias + ".start_date " +
                "AND C.visit_end_date <= ISNULL(" + alias + ".end_date, CAST('2099-12-31' AS DATE))";
    }
}
