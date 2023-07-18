package org.ohdsi.circe.cohortdefinition.builders;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.DrugEra;
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

public class DrugEraSqlBuilder<T extends DrugEra> extends CriteriaSqlBuilder<T> {

  private final static String DRUG_ERA_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/drugEra.sql");

  // default columns are those that are specified in the template, and dont' need to be added if specifeid in 'additionalColumns'
  private final Set<CriteriaColumn> DEFAULT_COLUMNS = new HashSet<>(Arrays.asList(CriteriaColumn.START_DATE, CriteriaColumn.END_DATE, CriteriaColumn.VISIT_ID));

  // default select columns are the columns that will always be returned from the subquery, but are added to based on the specific criteria
  private final List<String> DEFAULT_SELECT_COLUMNS = new ArrayList<>(Arrays.asList("de.person_id", "de.drug_era_id", "de.drug_concept_id", 
          "de.drug_exposure_count", "de.gap_days"));

  @Override
  protected Set<CriteriaColumn> getDefaultColumns() {
    return DEFAULT_COLUMNS;
  }

  @Override
  protected String getQueryTemplate() {

    return DRUG_ERA_TEMPLATE;
  }

  @Override
  protected String getTableColumnForCriteriaColumn(CriteriaColumn column) {
    switch (column) {
      case DOMAIN_CONCEPT:
        return "C.drug_concept_id";
      case ERA_OCCURRENCES:
        return "C.drug_exposure_count";
      case GAP_DAYS:
        return "C.gap_days";
      case DURATION:
        return "DATEDIFF(d,C.start_date, C.end_date)";
      default:
        throw new IllegalArgumentException("Invalid CriteriaColumn for Drug Era:" + column.toString());
    }
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
    if (criteria.first != null && criteria.first == true) {
      whereClauses.add("C.ordinal = 1");
      query = StringUtils.replace(query, "@ordinalExpression", ", row_number() over (PARTITION BY de.person_id ORDER BY de.drug_era_start_date, de.drug_era_id) as ordinal");
    } else {
      query = StringUtils.replace(query, "@ordinalExpression", "");
    }
    return query;
  }

  @Override
  protected List<String> resolveSelectClauses(T criteria) {

    ArrayList<String> selectCols = new ArrayList<>(DEFAULT_SELECT_COLUMNS);

    // gap_days and drug_exposure_count are included by default so we do not need to add here

    // dateAdjustment or default start/end dates
    if (criteria.dateAdjustment != null) {
      selectCols.add(BuilderUtils.getDateAdjustmentExpression(criteria.dateAdjustment,
              criteria.dateAdjustment.startWith == DateAdjustment.DateType.START_DATE ? "de.drug_era_start_date" : "de.drug_era_end_date",
              criteria.dateAdjustment.endWith == DateAdjustment.DateType.START_DATE ? "de.drug_era_start_date" : "de.drug_era_end_date"));
    } else {
      selectCols.add("de.drug_era_start_date as start_date, de.drug_era_end_date as end_date");
    }

    return selectCols;
  }

  @Override
  protected List<String> resolveJoinClauses(T criteria) {

    ArrayList<String> joinClauses = new ArrayList<>();

    // join to PERSON
    if (criteria.ageAtStart != null || criteria.ageAtEnd != null || (criteria.gender != null && criteria.gender.length > 0)) {
      joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
    }

    return joinClauses;
  }

  @Override
  protected List<String> resolveWhereClauses(T criteria) {

    List<String> whereClauses = super.resolveWhereClauses(criteria);

    // eraStartDate
    if (criteria.eraStartDate != null) {
      whereClauses.add(buildDateRangeClause("C.start_date", criteria.eraStartDate));
    }

    // eraEndDate
    if (criteria.eraEndDate != null) {
      whereClauses.add(buildDateRangeClause("C.end_date", criteria.eraEndDate));
    }

    // occurrenceCount
    if (criteria.occurrenceCount != null) {
      whereClauses.add(buildNumericRangeClause("C.drug_exposure_count", criteria.occurrenceCount));
    }

    // eraLength
    if (criteria.eraLength != null) {
      whereClauses.add(buildNumericRangeClause("DATEDIFF(d,C.start_date, C.end_date)", criteria.eraLength));
    }

    // gapDays
    if (criteria.gapDays != null) {
      whereClauses.add(buildNumericRangeClause("C.gap_days", criteria.eraLength));
    }

    // ageAtStart
    if (criteria.ageAtStart != null) {
      whereClauses.add(buildNumericRangeClause("YEAR(C.start_date) - P.year_of_birth", criteria.ageAtStart));
    }

    // ageAtEnd
    if (criteria.ageAtEnd != null) {
      whereClauses.add(buildNumericRangeClause("YEAR(C.end_date) - P.year_of_birth", criteria.ageAtEnd));
    }

    // gender
    if (criteria.gender != null && criteria.gender.length > 0) {
      whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.gender), ",")));
    }

    return whereClauses;
  }
}
