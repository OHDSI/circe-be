package org.ohdsi.circe.cohortdefinition.builders;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.VisitDetail;
import org.ohdsi.circe.helper.ResourceHelper;

import java.util.*;

public class VisitDetailSqlBuilder<T extends VisitDetail> extends CriteriaSqlBuilder<T> {

  private final static String VISIT_DETAIL_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/visitDetail.sql");

  // default columns are those that are specified in the template, and dont' need to be added if specifeid in 'additionalColumns'
  private final Set<CriteriaColumn> DEFAULT_COLUMNS = new HashSet<>(Arrays.asList(CriteriaColumn.START_DATE, CriteriaColumn.END_DATE, CriteriaColumn.VISIT_DETAIL_ID));

  @Override
  protected Set<CriteriaColumn> getDefaultColumns() {
    return DEFAULT_COLUMNS;
  }

  @Override
  protected String getQueryTemplate() {
    return VISIT_DETAIL_TEMPLATE;
  }

  @Override
  protected String getTableColumnForCriteriaColumn(CriteriaColumn column) {
    switch (column) {
      case DOMAIN_CONCEPT:
        return "C.visit_detail_concept_id";
      case DURATION:
        return "DATEDIFF(d, C.visit_detail_start_date, C.visit_detail_end_date)";
      default:
        throw new IllegalArgumentException("Invalid CriteriaColumn for Visit Detail:" + column.toString());
    }
  }

  @Override
  protected String embedCodesetClause(String query, T criteria) {

    return StringUtils.replace(query, "@codesetClause",
            BuilderUtils.getCodesetJoinExpression(criteria.codesetId,
                    "vd.visit_detail_concept_id",
                    criteria.visitDetailSourceConcept,
                    "vd.visit_detail_source_concept_id")
    );
  }

  @Override
  protected String embedOrdinalExpression(String query, T criteria, List<String> whereClauses) {
    // first
    if (criteria.first != null && criteria.first == true) {
      whereClauses.add("C.ordinal = 1");
      query = StringUtils.replace(query, "@ordinalExpression", ", row_number() over (PARTITION BY vd.person_id ORDER BY vd.visit_detail_start_date, vd.visit_detail_id) as ordinal");
    } else {
      query = StringUtils.replace(query, "@ordinalExpression", "");
    }
    return query;
  }

  @Override
  protected List<String> resolveJoinClauses(T criteria) {

    List<String> joinClauses = new ArrayList<>();

    if (criteria.age != null || criteria.genderCS != null) { // join to PERSON
      joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
    }

    if (criteria.genderCS != null) {
      addFiltering(joinClauses, criteria.genderCS, "P.gender_concept_id");
    }

    if (criteria.placeOfServiceCS != null || criteria.placeOfServiceLocation != null) {
      joinClauses.add("JOIN @cdm_database_schema.CARE_SITE CS on C.care_site_id = CS.care_site_id");
    }

    if (criteria.placeOfServiceCS != null) {
      addFiltering(joinClauses, criteria.placeOfServiceCS, "CS.place_of_service_concept_id");
    }

    if (criteria.providerSpecialtyCS != null) {
      addFilteringByProviderSpeciality(joinClauses, criteria.providerSpecialtyCS);
    }

    if (criteria.placeOfServiceLocation != null) {
      addFilteringByCareSiteLocationRegion(joinClauses, criteria.placeOfServiceLocation);
    }

    if (criteria.visitDetailTypeCS != null) {
      addFiltering(joinClauses, criteria.visitDetailTypeCS, "C.visit_detail_type_concept_id", criteria.visitDetailTypeExclude);
    }

    return joinClauses;
  }

  @Override
  protected List<String> resolveWhereClauses(T criteria) {

    List<String> whereClauses = new ArrayList<>();

    // occurrenceStartDate
    if (criteria.visitDetailStartDate != null) {
      whereClauses.add(BuilderUtils.buildDateRangeClause("C.visit_detail_start_date", criteria.visitDetailStartDate));
    }

    // occurrenceEndDate
    if (criteria.visitDetailEndDate != null) {
      whereClauses.add(BuilderUtils.buildDateRangeClause("C.visit_detail_end_date", criteria.visitDetailEndDate));
    }

    // visitLength
    if (criteria.visitDetailLength != null) {
      whereClauses.add(BuilderUtils.buildNumericRangeClause("DATEDIFF(d,C.visit_detail_start_date, C.visit_detail_end_date)", criteria.visitDetailLength));
    }

    // age
    if (criteria.age != null) {
      whereClauses.add(BuilderUtils.buildNumericRangeClause("YEAR(C.visit_detail_start_date) - P.year_of_birth", criteria.age));
    }

    return whereClauses;
  }

  protected void addFilteringByProviderSpeciality(List<String> joinClauses, Integer codesetId) {
    joinClauses.add("LEFT JOIN @cdm_database_schema.PROVIDER PR on C.provider_id = PR.provider_id");
    addFiltering(joinClauses, codesetId, "PR.specialty_concept_id");
  }

  protected void addFilteringByCareSiteLocationRegion(List<String> joinClauses, Integer codesetId) {

    joinClauses.add(getLocationHistoryJoin("LH", "CARE_SITE", "C.care_site_id"));
    joinClauses.add("JOIN @cdm_database_schema.LOCATION LOC on LOC.location_id = LH.location_id");
    addFiltering(joinClauses, codesetId, "LOC.region_concept_id");
  }

  private void addFiltering(List<String> joinClauses, Integer codesetId, String standardConceptColumn) {
    addFiltering(joinClauses, codesetId, standardConceptColumn, false);
  }

  private void addFiltering(List<String> joinClauses, Integer codesetId, String standardConceptColumn, boolean exclude) {
    joinClauses.add(
            BuilderUtils.getCodesetJoinExpression(
                    codesetId,
                    standardConceptColumn,
                    null,
                    null,
                    exclude
            )
    );
  }

  protected String getLocationHistoryJoin(String alias, String domain, String entityIdField) {

    return "JOIN @cdm_database_schema.LOCATION_HISTORY " + alias + " "
            + "on " + alias + ".entity_id = " + entityIdField + " "
            + "AND " + alias + ".domain_id = '" + domain + "' "
            + "AND C.visit_detail_start_date >= " + alias + ".start_date "
            + "AND C.visit_detail_end_date <= ISNULL(" + alias + ".end_date, DATEFROMPARTS(2099,12,31))";
  }
}
