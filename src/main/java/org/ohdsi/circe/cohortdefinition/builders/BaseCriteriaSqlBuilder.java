package org.ohdsi.circe.cohortdefinition.builders;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.Criteria;

import java.util.List;

public abstract class BaseCriteriaSqlBuilder<T extends Criteria> {

    public String getCriteriaSql(T criteria) {

        String query = getQueryTemplate();

        query = embedCodesetClause(query, criteria);

        List<String> joinClauses = resolveJoinClauses(criteria);
        List<String> whereClauses = resolveWhereClauses(criteria);

        query = embedOrdinalExpression(query, criteria, whereClauses);

        query = embedJoinClauses(query, joinClauses);
        query = embedWhereClauses(query, whereClauses);

        return query;
    }

    protected String embedJoinClauses(String query, List<String> joinClauses) {

        return StringUtils.replace(query, "@joinClause", StringUtils.join(joinClauses, "\n"));
    }

    protected String embedWhereClauses(String query, List<String> whereClauses) {

        String whereClause = "";
        if (whereClauses.size() > 0)
            whereClause = "WHERE " + StringUtils.join(whereClauses, "\nAND ");
        return StringUtils.replace(query, "@whereClause", whereClause);
    }

    protected abstract String getQueryTemplate();
    protected abstract String embedCodesetClause(String query, T criteria);
    protected abstract String embedOrdinalExpression(String query, T criteria, List<String> whereClauses);
    protected abstract List<String> resolveJoinClauses(T criteria);
    protected abstract List<String> resolveWhereClauses(T criteria);
}
