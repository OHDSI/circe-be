package org.ohdsi.circe.cohortdefinition.builders;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.DateRange;
import org.ohdsi.circe.cohortdefinition.DateAdjustment;
import org.ohdsi.circe.cohortdefinition.NumericRange;
import org.ohdsi.circe.cohortdefinition.TextFilter;
import org.ohdsi.circe.vocabulary.Concept;

import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.ohdsi.circe.helper.ResourceHelper;

public abstract class BuilderUtils {

  private final static String CODESET_JOIN_TEMPLATE = "JOIN #Codesets %s on (%s = %s.concept_id and %s.codeset_id = %d)";
  private final static String DATE_ADJUSTMENT_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/dateAdjustment.sql");
  ;
    private final static String STANARD_ALIAS = "cs";
  private final static String NON_STANARD_ALIAS = "cns";

  public static String getDateAdjustmentExpression(DateAdjustment dateAdjustment, String startColumn, String endColumn) {
    String expression = StringUtils.replace(DATE_ADJUSTMENT_TEMPLATE, "@startOffset", Integer.toString(dateAdjustment.startOffset));
    expression = StringUtils.replace(expression, "@startColumn", startColumn);
    expression = StringUtils.replace(expression, "@endOffset", Integer.toString(dateAdjustment.endOffset));
    expression = StringUtils.replace(expression, "@endColumn", endColumn);
    return expression;
  }

  public static String getCodesetJoinExpression(Integer standardCodesetId, String standardConceptColumn, Integer sourceCodesetId, String sourceConceptColumn) {

    String joinExpression = "";
    ArrayList<String> codesetClauses = new ArrayList<>();

    if (standardCodesetId != null) {
      codesetClauses.add(String.format(CODESET_JOIN_TEMPLATE, STANARD_ALIAS, standardConceptColumn, STANARD_ALIAS, STANARD_ALIAS, standardCodesetId));
    }

    // conditionSourceConcept
    if (sourceCodesetId != null) {
      codesetClauses.add(String.format(CODESET_JOIN_TEMPLATE, NON_STANARD_ALIAS, sourceConceptColumn, NON_STANARD_ALIAS, NON_STANARD_ALIAS, sourceCodesetId));
    }

    if (codesetClauses.size() > 0) {
      joinExpression = StringUtils.join(codesetClauses, "\n");
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
      clause = String.format("%s(%s >= %s and %s <= %s)",
              range.op.startsWith("!") ? "not " : "",
              sqlExpression,
              formatDouble(range.value.doubleValue(), format),
              sqlExpression,
              formatDouble(range.extent.doubleValue(), format));
    } else {
      clause = String.format("%s %s %s", sqlExpression, getOperator(range), formatDouble(range.value.doubleValue(), format));
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
    String postfix = filter.op.endsWith("startsWith") || filter.op.endsWith("contains") ? "%" : "";

    String value = escapeSqlParam(filter.text);

    return String.format("%s %s like '%s%s%s'", sqlExpression, negation, prefix, value, postfix);
  }

  private static String escapeSqlParam(String value) {
    if (StringUtils.isEmpty(value)) {
      return value;
    }
    return value.replaceAll("\\\\*\\'", "''");
  }

  private static String formatDouble(double d, String format) {
    // Forces the US Locale formatting for all double values
    // for Issue #184: https://github.com/OHDSI/circe-be/issues/184
    String formatString = "%" + format;
    return String.format(Locale.US, formatString, d);
  }
  
  public static <T> String splitInClause(String column, List<T> values, int groupSize) {
    // split the values into groupSize lists
    List<List<T>> groups = new ArrayList<>();
    for (int i = 0; i < values.size(); i += groupSize) {
        int endIndex = Math.min(i + groupSize, values.size());
        groups.add(values.subList(i, endIndex));
    }

    /// create individual IN statements
    List<String> ins = groups.stream().map(group -> String.format("%s in (%s)", column, StringUtils.join(group, ","))).collect(Collectors.toList());
    
    // return the set of INs grouped into ORs
    return String.format("(%s)", StringUtils.join(ins, " or "));
    
  }
}
