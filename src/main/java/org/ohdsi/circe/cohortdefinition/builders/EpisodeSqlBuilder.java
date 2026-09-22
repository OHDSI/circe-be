package org.ohdsi.circe.cohortdefinition.builders;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.DateAdjustment;
import org.ohdsi.circe.cohortdefinition.Episode;
import org.ohdsi.circe.helper.ResourceHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildDateRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildNumericRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.getCodesetInExpression;

public class EpisodeSqlBuilder<T extends Episode> extends CriteriaSqlBuilder<T> {

  private final static String EPISODE_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/episode.sql");

  private final Set<CriteriaColumn> DEFAULT_COLUMNS = new HashSet<>(Arrays.asList(CriteriaColumn.START_DATE, CriteriaColumn.END_DATE, CriteriaColumn.DOMAIN_CONCEPT));

  private final List<String> DEFAULT_SELECT_COLUMNS = new ArrayList<>(Arrays.asList(
          "ep.person_id",
          "ep.episode_id",
          "ep.episode_concept_id",
          "ep.episode_number",
          "ep.episode_object_concept_id",
          "ep.episode_type_concept_id"
  ));

  @Override
  protected Set<CriteriaColumn> getDefaultColumns() {
    return DEFAULT_COLUMNS;
  }

  @Override
  protected String getQueryTemplate() {
    return EPISODE_TEMPLATE;
  }

  @Override
  protected String getTableColumnForCriteriaColumn(CriteriaColumn column) {
    switch (column) {
      case DOMAIN_CONCEPT:
        return "C.episode_concept_id";
      case DURATION:
        return "DATEDIFF(d, C.start_date, C.end_date)";
      default:
        throw new IllegalArgumentException("Invalid CriteriaColumn for Episode:" + column.toString());
    }
  }

  @Override
  protected String embedCodesetClause(String query, T criteria) {
    return StringUtils.replace(query, "@codesetClause",
            BuilderUtils.getCodesetJoinExpression(criteria.codesetId, "ep.episode_concept_id", null, null)
    );
  }

  @Override
  protected String embedOrdinalExpression(String query, T criteria, List<String> whereClauses) {
    if (criteria.first != null && criteria.first) {
      whereClauses.add("C.ordinal = 1");
      query = StringUtils.replace(query, "@ordinalExpression", ", row_number() over (PARTITION BY ep.person_id ORDER BY ep.episode_start_date, ep.episode_id) as ordinal");
    } else {
      query = StringUtils.replace(query, "@ordinalExpression", "");
    }
    return query;
  }

  @Override
  protected List<String> resolveSelectClauses(T criteria) {
    ArrayList<String> selectCols = new ArrayList<>(DEFAULT_SELECT_COLUMNS);

    if (criteria.dateAdjustment != null) {
      selectCols.add(BuilderUtils.getDateAdjustmentExpression(criteria.dateAdjustment,
              criteria.dateAdjustment.startWith == DateAdjustment.DateType.START_DATE ? "ep.episode_start_date" : "ep.episode_end_date",
              criteria.dateAdjustment.endWith == DateAdjustment.DateType.START_DATE ? "ep.episode_start_date" : "ep.episode_end_date"));
    } else {
      selectCols.add("ep.episode_start_date as start_date, ep.episode_end_date as end_date");
    }

    return selectCols;
  }

  @Override
  protected List<String> resolveJoinClauses(T criteria) {
    List<String> joinClauses = new ArrayList<>();

    if (criteria.age != null ||
            criteria.genderCS != null) {
      joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
    }

    return joinClauses;
  }

  @Override
  protected List<String> resolveWhereClauses(T criteria) {
    List<String> whereClauses = super.resolveWhereClauses(criteria);

    if (criteria.episodeStartDate != null) {
      whereClauses.add(buildDateRangeClause("C.start_date", criteria.episodeStartDate));
    }

    if (criteria.episodeEndDate != null) {
      whereClauses.add(buildDateRangeClause("C.end_date", criteria.episodeEndDate));
    }

    if (criteria.episodeNumber != null) {
      whereClauses.add(buildNumericRangeClause("C.episode_number", criteria.episodeNumber));
    }

    if (criteria.age != null) {
      whereClauses.add(buildNumericRangeClause("YEAR(C.start_date) - P.year_of_birth", criteria.age));
    }

    if (criteria.genderCS != null) {
      whereClauses.add(getCodesetInExpression("P.gender_concept_id", criteria.genderCS));
    }

    if (criteria.episodeObjectConceptCS != null) {
      whereClauses.add(getCodesetInExpression("C.episode_object_concept_id", criteria.episodeObjectConceptCS));
    }

    if (criteria.episodeTypeCS != null) {
      whereClauses.add(getCodesetInExpression("C.episode_type_concept_id", criteria.episodeTypeCS));
    }

    return whereClauses;
  }
}