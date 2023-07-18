package org.ohdsi.circe.cohortdefinition.builders;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.PayerPlanPeriod;
import org.ohdsi.circe.cohortdefinition.Period;
import org.ohdsi.circe.helper.ResourceHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.ohdsi.circe.cohortdefinition.DateAdjustment;

import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildDateRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildNumericRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.getConceptIdsFromConcepts;

public class PayerPlanPeriodSqlBuilder<T extends PayerPlanPeriod> extends CriteriaSqlBuilder<T> {

  private final static String PAYER_PLAN_PERIOD_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/payerPlanPeriod.sql");

  // default columns are those that are specified in the template, and dont' need to be added if specifeid in 'additionalColumns'
  private final Set<CriteriaColumn> DEFAULT_COLUMNS = new HashSet<>(Arrays.asList(CriteriaColumn.START_DATE, CriteriaColumn.END_DATE, CriteriaColumn.VISIT_ID));

  // default select columns are the columns that will always be returned from the subquery, but are added to based on the specific criteria
  private final List<String> DEFAULT_SELECT_COLUMNS = new ArrayList<>(Arrays.asList("ppp.person_id", "ppp.payer_plan_period_id"));

  @Override
  protected Set<CriteriaColumn> getDefaultColumns() {
    return DEFAULT_COLUMNS;
  }

  @Override
  public String getCriteriaSql(T criteria, BuilderOptions options) {

    String query = super.getCriteriaSql(criteria, options);
    String startDateExpression = (criteria.userDefinedPeriod != null && criteria.userDefinedPeriod.startDate != null)
            ? BuilderUtils.dateStringToSql(criteria.userDefinedPeriod.startDate)
            : "C.start_date";
    query = StringUtils.replace(query, "@startDateExpression", startDateExpression);

    String endDateExpression = (criteria.userDefinedPeriod != null && criteria.userDefinedPeriod.endDate != null)
            ? BuilderUtils.dateStringToSql(criteria.userDefinedPeriod.endDate)
            : "C.end_date";
    query = StringUtils.replace(query, "@endDateExpression", endDateExpression);
    return query;
  }

  @Override
  protected String getQueryTemplate() {

    return PAYER_PLAN_PERIOD_TEMPLATE;
  }

  @Override
  protected String getTableColumnForCriteriaColumn(CriteriaColumn column) {
    switch (column) {
      case DOMAIN_CONCEPT:
        return "C.payer_concept_id";
      default:
        throw new IllegalArgumentException("Invalid CriteriaColumn for Payer Plan Period:" + column.toString());
    }
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
  protected List<String> resolveSelectClauses(T criteria) {

    ArrayList<String> selectCols = new ArrayList<>(DEFAULT_SELECT_COLUMNS);

    // payer concept
    if (criteria.payerConcept != null) {
      selectCols.add("ppp.payer_concept_id");
    }

    // plan concept
    if (criteria.planConcept != null) {
      selectCols.add("ppp.plan_concept_id");
    }

    // sponsor concept
    if (criteria.sponsorConcept != null) {
      selectCols.add("ppp.sponsor_concept_id");
    }

    // stop reason concept
    if (criteria.stopReasonConcept != null) {
      selectCols.add("ppp.stop_reason_concept_id");
    }

    // payer SourceConcept
    if (criteria.payerSourceConcept != null) {
      selectCols.add("ppp.payer_source_concept_id");
    }

    // plan SourceConcept
    if (criteria.planSourceConcept != null) {
      selectCols.add("ppp.plan_source_concept_id");
    }

    // sponsor SourceConcept
    if (criteria.sponsorSourceConcept != null) {
      selectCols.add("ppp.sponsor_source_concept_id");
    }

    // stop reason SourceConcept
    if (criteria.stopReasonSourceConcept != null) {
      selectCols.add("ppp.stop_reason_source_concept_id");
    }
    // dateAdjustment or default start/end dates
    if (criteria.dateAdjustment != null) {
      selectCols.add(BuilderUtils.getDateAdjustmentExpression(criteria.dateAdjustment,
              criteria.dateAdjustment.startWith == DateAdjustment.DateType.START_DATE ? "ppp.payer_plan_period_start_date" : "ppp.payer_plan_period_end_date",
              criteria.dateAdjustment.endWith == DateAdjustment.DateType.START_DATE ? "ppp.payer_plan_period_start_date" : "ppp.payer_plan_period_end_date"));
    } else {
      selectCols.add("ppp.payer_plan_period_start_date as start_date, ppp.payer_plan_period_end_date as end_date");
    }
    return selectCols;
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
  protected List<String> resolveWhereClauses(T criteria) {

    List<String> whereClauses = super.resolveWhereClauses(criteria);

    //first
    if (criteria.first != null && criteria.first) {
      whereClauses.add("C.ordinal = 1");
    }

    // check for user defined start/end dates
    if (criteria.userDefinedPeriod != null) {
      Period userDefinedPeriod = criteria.userDefinedPeriod;

      if (userDefinedPeriod.startDate != null) {
        String startDateExpression = BuilderUtils.dateStringToSql(userDefinedPeriod.startDate);
        whereClauses.add(String.format("C.start_date <= %s and C.end_date >= %s", startDateExpression, startDateExpression));
      }

      if (userDefinedPeriod.endDate != null) {
        String endDateExpression = BuilderUtils.dateStringToSql(userDefinedPeriod.endDate);
        whereClauses.add(String.format("C.start_date <= %s and C.end_date >= %s", endDateExpression, endDateExpression));
      }
    }

    //periodStartDate
    if (criteria.periodStartDate != null) {
      whereClauses.add(buildDateRangeClause("C.start_date", criteria.periodStartDate));
    }

    //periodEndDate
    if (criteria.periodEndDate != null) {
      whereClauses.add(buildDateRangeClause("C.end_date", criteria.periodEndDate));
    }

    //periodLength
    if (criteria.periodLength != null) {
      whereClauses.add(buildNumericRangeClause("DATEDIFF(d,C.start_date, C.end_date)", criteria.periodLength));
    }

    //ageAtStart
    if (criteria.ageAtStart != null) {
      whereClauses.add(buildNumericRangeClause("YEAR(C.start_date) - P.year_of_birth", criteria.ageAtStart));
    }

    //ageAtEnd
    if (criteria.ageAtEnd != null) {
      whereClauses.add(buildNumericRangeClause("YEAR(C.end_date) - P.year_of_birth", criteria.ageAtEnd));
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
