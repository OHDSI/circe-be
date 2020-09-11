package org.ohdsi.circe.cohortdefinition.builders;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.DeviceExposure;
import org.ohdsi.circe.helper.ResourceHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildDateRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildNumericRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildTextFilterClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.getCodesetJoinExpression;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.getConceptIdsFromConcepts;

public class DeviceExposureSqlBuilder<T extends DeviceExposure> extends CriteriaSqlBuilder<T> {

    private final static String DEVICE_EXPOSURE_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/deviceExposure.sql");

    @Override
    protected String getQueryTemplate() {

        return DEVICE_EXPOSURE_TEMPLATE;
    }

    @Override
    protected String embedCodesetClause(String query, T criteria) {

        return StringUtils.replace(query, "@codesetClause",
                getCodesetJoinExpression(criteria.codesetId,
                        "de.device_concept_id",
                        criteria.deviceSourceConcept,
                        "de.device_source_concept_id")
        );
    }

    @Override
    protected String embedOrdinalExpression(String query, T criteria, List<String> whereClauses) {

        // first
        if (criteria.first != null && criteria.first) {
            whereClauses.add("C.ordinal = 1");
            query = StringUtils.replace(query, "@ordinalExpression", ", row_number() over (PARTITION BY de.person_id ORDER BY de.device_exposure_start_date, de.device_exposure_id) as ordinal");
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
            joinClauses.add("JOIN @cdm_database_schema.person P on C.person_id = P.person_id");
        }
        if (criteria.visitType != null && criteria.visitType.length > 0) {
            joinClauses.add("JOIN @cdm_database_schema.visit_occurrence V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id");
        }
        if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0) {
            joinClauses.add("LEFT JOIN @cdm_database_schema.provider PR on C.provider_id = PR.provider_id");
        }

        return joinClauses;
    }

    @Override
    protected List<String> resolveWhereClauses(T criteria, Map<String, String> additionalVariables) {

        ArrayList<String> whereClauses = new ArrayList<>();

        // occurrenceStartDate
        if (criteria.occurrenceStartDate != null) {
            whereClauses.add(buildDateRangeClause("C.device_exposure_start_date", criteria.occurrenceStartDate));
        }

        // occurrenceEndDate
        if (criteria.occurrenceEndDate != null) {
            whereClauses.add(buildDateRangeClause("C.device_exposure_end_date", criteria.occurrenceEndDate));
        }

        // deviceType
        if (criteria.deviceType != null && criteria.deviceType.length > 0) {
            ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.deviceType);
            whereClauses.add(String.format("C.device_type_concept_id %s in (%s)", (criteria.deviceTypeExclude ? "not" : ""), StringUtils.join(conceptIds, ",")));
        }

        // uniqueDeviceId
        if (criteria.uniqueDeviceId != null) {
            whereClauses.add(buildTextFilterClause("C.unique_device_id", criteria.uniqueDeviceId));
        }

        // quantity
        if (criteria.quantity != null) {
            whereClauses.add(buildNumericRangeClause("C.quantity", criteria.quantity));
        }

        // age
        if (criteria.age != null) {
            whereClauses.add(buildNumericRangeClause("YEAR(C.device_exposure_start_date) - P.year_of_birth", criteria.age));
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
