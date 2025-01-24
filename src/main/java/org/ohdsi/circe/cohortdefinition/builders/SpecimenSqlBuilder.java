package org.ohdsi.circe.cohortdefinition.builders;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.Specimen;
import org.ohdsi.circe.helper.ResourceHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildDateRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildNumericRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildTextFilterClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.getCodesetInExpression;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.getConceptIdsFromConcepts;

public class SpecimenSqlBuilder<T extends Specimen> extends CriteriaSqlBuilder<T> {

  private final static String SPECIMEN_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/specimen.sql");

  // default columns are those that are specified in the template, and dont' need to be added if specifeid in 'additionalColumns'
  private final Set<CriteriaColumn> DEFAULT_COLUMNS = new HashSet<>(Arrays.asList(CriteriaColumn.START_DATE, CriteriaColumn.END_DATE, CriteriaColumn.VISIT_ID));

  @Override
  protected Set<CriteriaColumn> getDefaultColumns() {
    return DEFAULT_COLUMNS;
  }

  @Override
  protected String getQueryTemplate() {

    return SPECIMEN_TEMPLATE;
  }

  @Override
  protected String getTableColumnForCriteriaColumn(CriteriaColumn column) {
    switch (column) {
      case DOMAIN_CONCEPT:
        return "C.specimen_concept_id";
      case DURATION:
        return "CAST(1 as int)";
      default:
        throw new IllegalArgumentException("Invalid CriteriaColumn for Specimen:" + column.toString());
    }
  }

  @Override
  protected String embedCodesetClause(String query, T criteria) {

    String codesetClause = "";
    if (criteria.codesetId != null) {
      codesetClause = String.format("where s.specimen_concept_id in (SELECT concept_id from  #Codesets where codeset_id = %d)", criteria.codesetId);
    }
    return StringUtils.replace(query, "@codesetClause", codesetClause);
  }

  @Override
  protected String embedOrdinalExpression(String query, T criteria, List<String> whereClauses) {

    // first
    if (criteria.first != null && criteria.first) {
      whereClauses.add("C.ordinal = 1");
      query = StringUtils.replace(query, "@ordinalExpression", ", row_number() over (PARTITION BY s.person_id ORDER BY s.specimen_date, s.specimen_id) as ordinal");
    } else {
      query = StringUtils.replace(query, "@ordinalExpression", "");
    }
    return query;
  }

  @Override
  protected List<String> resolveJoinClauses(T criteria) {

    ArrayList<String> joinClauses = new ArrayList<>();
    // join to PERSON
    if (criteria.age != null ||
      (criteria.gender != null && criteria.gender.length > 0) ||
      criteria.genderCS != null
    ) {
      joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
    }
    return joinClauses;
  }

  @Override
  protected List<String> resolveWhereClauses(T criteria) {

    ArrayList<String> whereClauses = new ArrayList<>();

    // occurrenceStartDate
    if (criteria.occurrenceStartDate != null) {
      whereClauses.add(buildDateRangeClause("C.specimen_date", criteria.occurrenceStartDate));
    }

    // specimenType
    if (criteria.specimenType != null && criteria.specimenType.length > 0) {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.specimenType);
      whereClauses.add(String.format("C.specimen_type_concept_id %s in (%s)", (criteria.specimenTypeExclude ? "not" : ""), StringUtils.join(conceptIds, ",")));
    }

    // specimenTypeCS
    if (criteria.specimenTypeCS != null) {
      whereClauses.add(getCodesetInExpression("C.specimen_type_concept_id", criteria.specimenTypeCS));
    }

    // quantity
    if (criteria.quantity != null) {
      whereClauses.add(buildNumericRangeClause("C.quantity", criteria.quantity, ".4f"));
    }

    // unit
    if (criteria.unit != null && criteria.unit.length > 0) {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.unit);
      whereClauses.add(String.format("C.unit_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
    }

    // unitCS
    if (criteria.unitCS != null) {
      whereClauses.add(getCodesetInExpression("C.unit_concept_id", criteria.unitCS));
    }

    // anatomicSite
    if (criteria.anatomicSite != null && criteria.anatomicSite.length > 0) {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.anatomicSite);
      whereClauses.add(String.format("C.anatomic_site_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
    }

    // anatomicSiteCS
    if (criteria.anatomicSiteCS != null) {
      whereClauses.add(getCodesetInExpression("C.anatomic_site_concept_id", criteria.anatomicSiteCS));
    }

    // diseaseStatus
    if (criteria.diseaseStatus != null && criteria.diseaseStatus.length > 0) {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.diseaseStatus);
      whereClauses.add(String.format("C.disease_status_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
    }

    // diseaseStatusCS
    if (criteria.diseaseStatusCS != null) {
      whereClauses.add(getCodesetInExpression("C.disease_status_concept_id", criteria.diseaseStatusCS));
    }

    // sourceId
    if (criteria.sourceId != null) {
      whereClauses.add(buildTextFilterClause("C.specimen_source_id", criteria.sourceId));
    }

    // age
    if (criteria.age != null) {
      whereClauses.add(buildNumericRangeClause("YEAR(C.specimen_date) - P.year_of_birth", criteria.age));
    }

    // gender
    if (criteria.gender != null && criteria.gender.length > 0) {
      whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.gender), ",")));
    }

    // genderCS
    if (criteria.genderCS != null) {
      whereClauses.add(getCodesetInExpression("P.gender_concept_id", criteria.genderCS));
    }

    return whereClauses;
  }
}
