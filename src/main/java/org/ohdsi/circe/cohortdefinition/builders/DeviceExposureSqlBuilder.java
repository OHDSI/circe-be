package org.ohdsi.circe.cohortdefinition.builders;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.DeviceExposure;
import org.ohdsi.circe.helper.ResourceHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ohdsi.circe.cohortdefinition.DateAdjustment;

import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildDateRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildNumericRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildTextFilterClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.getCodesetJoinExpression;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.getConceptIdsFromConcepts;

public class DeviceExposureSqlBuilder<T extends DeviceExposure> extends CriteriaSqlBuilder<T> {

  private final static String DEVICE_EXPOSURE_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/deviceExposure.sql");

  // default columns are those that are specified in the template, and dont' need to be added if specifeid in 'additionalColumns'
  private final Set<CriteriaColumn> DEFAULT_COLUMNS = new HashSet<>(Arrays.asList(CriteriaColumn.START_DATE, CriteriaColumn.END_DATE, CriteriaColumn.VISIT_ID));

  // default select columns are the columns that will always be returned from the subquery, but are added to based on the specific criteria
  private final List<String> DEFAULT_SELECT_COLUMNS = new ArrayList<>(Arrays.asList("de.person_id", "de.device_exposure_id", "de.device_concept_id", 
          "de.visit_occurrence_id", "de.quantity"));

  @Override
  protected Set<CriteriaColumn> getDefaultColumns() {
    return DEFAULT_COLUMNS;
  }

  @Override
  protected String getQueryTemplate() {

    return DEVICE_EXPOSURE_TEMPLATE;
  }

  @Override
  protected String getTableColumnForCriteriaColumn(CriteriaColumn column) {
    switch (column) {
      case DOMAIN_CONCEPT:
        return "C.device_concept_id";
      case QUANTITY:
        return "C.quantity";
      case DURATION:
        return "DATEDIFF(d,c.start_date, c.end_date)";
      default:
        throw new IllegalArgumentException("Invalid CriteriaColumn for Device Exposure:" + column.toString());
    }
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
  protected List<String> resolveSelectClauses(T criteria) {
    ArrayList<String> selectCols = new ArrayList<>(DEFAULT_SELECT_COLUMNS);
    // Device Type
    if (criteria.deviceType != null && criteria.deviceType.length > 0) {
      selectCols.add("de.device_type_concept_id");
    }

    // uniqueDeviceId
    if (criteria.uniqueDeviceId != null) {
      selectCols.add("de.unique_device_id");
    }

    // providerSpecialty
    if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0) {
      selectCols.add("de.provider_id");
    }

    // dateAdjustment or default start/end dates
    if (criteria.dateAdjustment != null) {
      selectCols.add(BuilderUtils.getDateAdjustmentExpression(criteria.dateAdjustment,
              criteria.dateAdjustment.startWith == DateAdjustment.DateType.START_DATE ? "de.device_exposure_start_date" : "COALESCE(de.device_exposure_end_date, DATEADD(day,1,de.device_exposure_start_date))",
              criteria.dateAdjustment.endWith == DateAdjustment.DateType.START_DATE ? "de.device_exposure_start_date" : "COALESCE(de.device_exposure_end_date, DATEADD(day,1,de.device_exposure_start_date))"));
    } else {
      selectCols.add("de.device_exposure_start_date as start_date, COALESCE(de.device_exposure_end_date, DATEADD(day,1,de.device_exposure_start_date)) as end_date");
    }
    return selectCols;
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
  protected List<String> resolveWhereClauses(T criteria) {

    List<String> whereClauses = super.resolveWhereClauses(criteria);

    // occurrenceStartDate
    if (criteria.occurrenceStartDate != null) {
      whereClauses.add(buildDateRangeClause("C.start_date", criteria.occurrenceStartDate));
    }

    // occurrenceEndDate
    if (criteria.occurrenceEndDate != null) {
      whereClauses.add(buildDateRangeClause("C.end_date", criteria.occurrenceEndDate));
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
      whereClauses.add(buildNumericRangeClause("YEAR(C.start_date) - P.year_of_birth", criteria.age));
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
