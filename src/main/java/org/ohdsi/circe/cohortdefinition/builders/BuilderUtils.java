package org.ohdsi.circe.cohortdefinition.builders;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.DateRange;
import org.ohdsi.circe.cohortdefinition.NumericRange;
import org.ohdsi.circe.cohortdefinition.TextFilter;
import org.ohdsi.circe.vocabulary.Concept;

import java.util.ArrayList;

public abstract class BuilderUtils {

    private final static String CODESET_JOIN_TEMPLATE = "JOIN #Codesets codesets on (@codesetClauses)";

    public static String getCodesetJoinExpression(Integer standardCodesetId, String standardConceptColumn, Integer sourceCodesetId, String sourceConceptColumn) {

        final String codsetJoinClause = "(%s = codesets.concept_id and codesets.codeset_id = %d)";
        String joinExpression = "";

        ArrayList<String> codesetClauses = new ArrayList<>();

        if (standardCodesetId != null) {
            codesetClauses.add(String.format(codsetJoinClause, standardConceptColumn, standardCodesetId));
        }

        // conditionSourceConcept
        if (sourceCodesetId != null) {
            codesetClauses.add(String.format(codsetJoinClause, sourceConceptColumn, sourceCodesetId));
        }

        if (codesetClauses.size() > 0) {
            joinExpression = StringUtils.replace(CODESET_JOIN_TEMPLATE, "@codesetClauses", StringUtils.join(codesetClauses, " AND "));
        }


        return joinExpression;
    }

    public static String getOperator(String op) {

        switch (op) {
            case "lt":
                return "<";
            case "lte":
                return "<=";
            case "eq":
                return "=";
            case "!eq":
                return "<>";
            case "gt":
                return ">";
            case "gte":
                return ">=";
        }
        throw new RuntimeException("Unknown operator type: " + op);
    }

    public static String getOperator(NumericRange range) {

        return getOperator(range.op);
    }

    public static String getOperator(DateRange range) {

        return getOperator(range.op);
    }

    public static String dateStringToSql(String date) {

        String[] dateParts = StringUtils.split(date, '-');
        return String.format("DATEFROMPARTS(%s, %s, %s)", Integer.valueOf(dateParts[0]), Integer.valueOf(dateParts[1]), Integer.valueOf(dateParts[2]));
    }

    public static String buildDateRangeClause(String sqlExpression, DateRange range) {

        String clause;
        if (range.op.endsWith("bt")) // range with a 'between' op
        {
            clause = String.format("%s(%s >= %s and %s <= %s)",
                    range.op.startsWith("!") ? "not " : "",
                    sqlExpression,
                    dateStringToSql(range.value),
                    sqlExpression,
                    dateStringToSql(range.extent));
        } else // single value range (less than/eq/greater than, etc)
        {
            clause = String.format("%s %s %s", sqlExpression, getOperator(range), dateStringToSql(range.value));
        }
        return clause;
    }

    // assumes decimal range
    public static String buildNumericRangeClause(String sqlExpression, NumericRange range, String format) {

        String clause;
        if (range.op.endsWith("bt")) {
            clause = String.format("%s(%s >= %" + format + " and %s <= %" + format + ")",
                    range.op.startsWith("!") ? "not " : "",
                    sqlExpression,
                    range.value.doubleValue(),
                    sqlExpression,
                    range.extent.doubleValue());
        } else {
            clause = String.format("%s %s %" + format, sqlExpression, getOperator(range), range.value.doubleValue());
        }
        return clause;
    }

    // Assumes integer numeric range
    public static String buildNumericRangeClause(String sqlExpression, NumericRange range) {

        String clause;
        if (range.op.endsWith("bt")) {
            clause = String.format("%s(%s >= %d and %s <= %d)",
                    range.op.startsWith("!") ? "not " : "",
                    sqlExpression,
                    range.value.intValue(),
                    sqlExpression,
                    range.extent.intValue());
        } else {
            clause = String.format("%s %s %d", sqlExpression, getOperator(range), range.value.intValue());
        }
        return clause;
    }

    public static ArrayList<Long> getConceptIdsFromConcepts(Concept[] concepts) {

        ArrayList<Long> conceptIdList = new ArrayList<>();
        for (Concept concept : concepts) {
            conceptIdList.add(concept.conceptId);
        }
        return conceptIdList;
    }

    public static String buildTextFilterClause(String sqlExpression, TextFilter filter) {

        String negation = filter.op.startsWith("!") ? "not" : "";
        String prefix = filter.op.endsWith("endsWith") || filter.op.endsWith("contains") ? "%" : "";
        String value = filter.text;
        String postfix = filter.op.endsWith("startsWith") || filter.op.endsWith("contains") ? "%" : "";

        return String.format("%s %s like '%s%s%s'", sqlExpression, negation, prefix, value, postfix);
    }
}
