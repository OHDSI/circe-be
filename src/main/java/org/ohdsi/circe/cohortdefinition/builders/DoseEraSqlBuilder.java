package org.ohdsi.circe.cohortdefinition.builders;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.DoseEra;
import org.ohdsi.circe.helper.ResourceHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildDateRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildNumericRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.getConceptIdsFromConcepts;

public class DoseEraSqlBuilder<T extends DoseEra> extends CriteriaSqlBuilder<T> {

    private final static String DOSE_ERA_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/doseEra.sql");

    @Override
    protected String getQueryTemplate() {

        return DOSE_ERA_TEMPLATE;
    }

    @Override
    protected String embedCodesetClause(String query, T criteria) {

        String codesetClause = "";
        if (criteria.codesetId != null) {
            codesetClause = String.format("where de.drug_concept_id in (SELECT concept_id from  #Codesets where codeset_id = %d)", criteria.codesetId);
        }
        return StringUtils.replace(query, "@codesetClause", codesetClause);
    }

    @Override
    protected String embedOrdinalExpression(String query, T criteria, List<String> whereClauses) {

        // first
        if (criteria.first != null && criteria.first) {
            whereClauses.add("C.ordinal = 1");
            query = StringUtils.replace(query, "@ordinalExpression", ", row_number() over (PARTITION BY de.person_id ORDER BY de.dose_era_start_date, de.dose_era_id) as ordinal");
        } else {
            query = StringUtils.replace(query, "@ordinalExpression", "");
        }

        return query;
    }

    @Override
    protected List<String> resolveJoinClauses(T criteria) {

        List<String> joinClauses = new ArrayList<>();

        // join to PERSON
        if (criteria.ageAtStart != null || criteria.ageAtEnd != null || (criteria.gender != null && criteria.gender.length > 0)) {
            joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
        }

        return joinClauses;
    }

    @Override
    protected List<String> resolveWhereClauses(T criteria, Map<String, String> additionalVariables) {

        List<String> whereClauses = new ArrayList<>();

        // eraStartDate
        if (criteria.eraStartDate != null) {
            whereClauses.add(buildDateRangeClause("C.dose_era_start_date", criteria.eraStartDate));
        }

        // eraEndDate
        if (criteria.eraEndDate != null) {
            whereClauses.add(buildDateRangeClause("C.dose_era_end_date", criteria.eraEndDate));
        }

        // unit
        if (criteria.unit != null && criteria.unit.length > 0) {
            whereClauses.add(String.format("c.unit_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.unit), ",")));
        }

        // doseValue
        if (criteria.doseValue != null) {
            whereClauses.add(buildNumericRangeClause("c.dose_value", criteria.doseValue, ".4f"));
        }

        // eraLength
        if (criteria.eraLength != null) {
            whereClauses.add(buildNumericRangeClause("DATEDIFF(d,C.dose_era_start_date, C.dose_era_end_date)", criteria.eraLength));
        }

        // ageAtStart
        if (criteria.ageAtStart != null) {
            whereClauses.add(buildNumericRangeClause("YEAR(C.dose_era_start_date) - P.year_of_birth", criteria.ageAtStart));
        }

        // ageAtEnd
        if (criteria.ageAtEnd != null) {
            whereClauses.add(buildNumericRangeClause("YEAR(C.dose_era_end_date) - P.year_of_birth", criteria.ageAtEnd));
        }

        // gender
        if (criteria.gender != null && criteria.gender.length > 0) {
            whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.gender), ",")));
        }

        return whereClauses;
    }
}
