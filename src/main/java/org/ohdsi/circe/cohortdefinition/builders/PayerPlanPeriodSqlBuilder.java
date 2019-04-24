package org.ohdsi.circe.cohortdefinition.builders;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.PayerPlanPeriod;
import org.ohdsi.circe.cohortdefinition.Period;
import org.ohdsi.circe.helper.ResourceHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildDateRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildNumericRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.getConceptIdsFromConcepts;

public class PayerPlanPeriodSqlBuilder<T extends PayerPlanPeriod> extends CriteriaSqlBuilder<T> {

    private final static String PAYER_PLAN_PERIOD_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/payerPlanPeriod.sql");

    @Override
    protected String getQueryTemplate() {

        return PAYER_PLAN_PERIOD_TEMPLATE;
    }

    @Override
    protected String embedCodesetClause(String query, T criteria) {

        return query;
    }

    @Override
    protected String embedOrdinalExpression(String query, T criteria, List<String> whereClauses) {

        return query;
    }

    @Override
    protected List<String> resolveJoinClauses(T criteria) {

        List<String> joinClauses = new ArrayList<>();

        if (criteria.ageAtStart != null || criteria.ageAtEnd != null || (criteria.gender != null && criteria.gender.length > 0)) {
            joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
        }

        return joinClauses;
    }

    @Override
    protected List<String> resolveWhereClauses(T criteria, Map<String, String> additionalVariables) {

        List<String> whereClauses = new ArrayList<>();

        String startDateExpression = "C.payer_plan_period_start_date";
        String endDateExpression = "C.payer_plan_period_end_date";

        //first
        if (criteria.first != null && criteria.first) {
            whereClauses.add("C.ordinal = 1");
        }

        // check for user defined start/end dates
        if (criteria.userDefinedPeriod != null) {
            Period userDefinedPeriod = criteria.userDefinedPeriod;

            if (userDefinedPeriod.startDate != null) {
                startDateExpression = String.format("CAST('%s' as Date)", userDefinedPeriod.startDate);
                whereClauses.add(String.format("C.PAYER_PLAN_PERIOD_START_DATE <= %s and C.PAYER_PLAN_PERIOD_END_DATE >= %s", startDateExpression, startDateExpression));
            }

            if (userDefinedPeriod.endDate != null) {
                endDateExpression = String.format("CAST('%s' as Date)", userDefinedPeriod.endDate);
                whereClauses.add(String.format("C.PAYER_PLAN_PERIOD_START_DATE <= %s and C.PAYER_PLAN_PERIOD_END_DATE >= %s", endDateExpression, endDateExpression));
            }
        }

        additionalVariables.put("@startDateExpression", startDateExpression);
        additionalVariables.put("@endDateExpression", endDateExpression);

        //periodStartDate
        if (criteria.periodStartDate != null) {
            whereClauses.add(buildDateRangeClause("C.payer_plan_period_start_date", criteria.periodStartDate));
        }

        //periodEndDate
        if (criteria.periodEndDate != null) {
            whereClauses.add(buildDateRangeClause("C.payer_plan_period_end_date", criteria.periodEndDate));
        }

        //periodLength
        if (criteria.periodLength != null) {
            whereClauses.add(buildNumericRangeClause("DATEDIFF(d,C.payer_plan_period_start_date, C.payer_plan_period_end_date)", criteria.periodLength));
        }

        //ageAtStart
        if (criteria.ageAtStart != null) {
            whereClauses.add(buildNumericRangeClause("YEAR(C.payer_plan_period_start_date) - P.year_of_birth", criteria.ageAtStart));
        }

        //ageAtEnd
        if (criteria.ageAtEnd != null) {
            whereClauses.add(buildNumericRangeClause("YEAR(C.payer_plan_period_end_date) - P.year_of_birth", criteria.ageAtEnd));
        }

        //gender
        if (criteria.gender != null && criteria.gender.length > 0) {
            ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.gender);
            whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
        }

        // payer concept
        if (criteria.payerConcept != null) {
            whereClauses.add(String.format("C.payer_concept_id in (SELECT concept_id from #Codesets where codeset_id = %d)", criteria.payerConcept));
        }

        // plan concept
        if (criteria.planConcept != null) {
            whereClauses.add(String.format("C.plan_concept_id in (SELECT concept_id from #Codesets where codeset_id = %d)", criteria.planConcept));
        }

        // sponsor concept
        if (criteria.sponsorConcept != null) {
            whereClauses.add(String.format("C.sponsor_concept_id in (SELECT concept_id from #Codesets where codeset_id = %d)", criteria.sponsorConcept));
        }

        // stop reason concept
        if (criteria.stopReasonConcept != null) {
            whereClauses.add(String.format("C.stop_reason_concept_id in (SELECT concept_id from #Codesets where codeset_id = %d)", criteria.stopReasonConcept));
        }

        // payer SourceConcept
        if (criteria.payerSourceConcept != null) {
            whereClauses.add(String.format("C.payer_source_concept_id in (SELECT concept_id from #Codesets where codeset_id = %d)", criteria.payerSourceConcept));
        }

        // plan SourceConcept
        if (criteria.planSourceConcept != null) {
            whereClauses.add(String.format("C.plan_source_concept_id in (SELECT concept_id from #Codesets where codeset_id = %d)", criteria.planSourceConcept));
        }

        // sponsor SourceConcept
        if (criteria.sponsorSourceConcept != null) {
            whereClauses.add(String.format("C.sponsor_source_concept_id in (SELECT concept_id from #Codesets where codeset_id = %d)", criteria.sponsorSourceConcept));
        }

        // stop reason SourceConcept
        if (criteria.stopReasonSourceConcept != null) {
            whereClauses.add(String.format("C.stop_reason_source_concept_id in (SELECT concept_id from #Codesets where codeset_id = %d)", criteria.stopReasonSourceConcept));
        }

        return whereClauses;
    }
}
