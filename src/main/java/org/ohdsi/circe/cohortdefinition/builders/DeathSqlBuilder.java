package org.ohdsi.circe.cohortdefinition.builders;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.Death;
import org.ohdsi.circe.helper.ResourceHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildDateRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildNumericRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.getCodesetJoinExpression;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.getConceptIdsFromConcepts;

public class DeathSqlBuilder<T extends Death> extends CriteriaSqlBuilder<T> {

    private final static String DEATH_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/death.sql");

    @Override
    protected String getQueryTemplate() {

        return DEATH_TEMPLATE;
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
    protected List<String> resolveJoinClauses(T criteria) {

        List<String> joinClauses = new ArrayList<>();

        // join to PERSON
        if (criteria.age != null || (criteria.gender != null && criteria.gender.length > 0)) {
            joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
        }

        return joinClauses;
    }

    @Override
    protected List<String> resolveWhereClauses(T criteria, Map<String, String> additionalVariables) {

        ArrayList<String> whereClauses = new ArrayList<>();

        // occurrenceStartDate
        if (criteria.occurrenceStartDate != null) {
            whereClauses.add(buildDateRangeClause("C.death_date", criteria.occurrenceStartDate));
        }

        // deathType
        if (criteria.deathType != null && criteria.deathType.length > 0) {
            ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.deathType);
            whereClauses.add(String.format("C.death_type_concept_id %s in (%s)", (criteria.deathTypeExclude ? "not" : ""), StringUtils.join(conceptIds, ",")));
        }

        // age
        if (criteria.age != null) {
            whereClauses.add(buildNumericRangeClause("YEAR(C.death_date) - P.year_of_birth", criteria.age));
        }

        // gender
        if (criteria.gender != null && criteria.gender.length > 0) {
            whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.gender), ",")));
        }

        return whereClauses;
    }
}
