package org.ohdsi.circe.cohortdefinition.builders;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.LocationRegion;
import org.ohdsi.circe.helper.ResourceHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.getCodesetJoinExpression;

public class LocationRegionSqlBuilder<T extends LocationRegion> extends CriteriaSqlBuilder<T> {

    private final static String LOCATION_REGION_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/locationRegion.sql");

    @Override
    protected String getQueryTemplate() {

        return LOCATION_REGION_TEMPLATE;
    }

    @Override
    protected String embedCodesetClause(String query, T criteria) {

        return StringUtils.replace(query, "@codesetClause",
                getCodesetJoinExpression(criteria.codesetId,
                        "l.region_concept_id",
                        null,
                        null)
        );
    }

    @Override
    protected String embedOrdinalExpression(String query, T criteria, List<String> whereClauses) {

        return query;
    }

    @Override
    protected List<String> resolveJoinClauses(T criteria) {

        return new ArrayList<>();
    }

    @Override
    protected List<String> resolveWhereClauses(T criteria, Map<String, String> additionalVariables) {

        return new ArrayList<>();
    }
}
