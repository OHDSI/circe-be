package org.ohdsi.circe.cohortdefinition.negativecontrols;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.helper.ResourceHelper;

public class CohortExpressionQueryBuilder {
    private final static String DOMAIN_QUERY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/negativecontrols/sql/domainQuery.sql");
    private final static String FIRST_OCCURRENCE_QUERY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/negativecontrols/sql/firstOccurrence.sql");
    private final static String INSERT_COHORT_QUERY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/negativecontrols/sql/insertCohort.sql");
    private final static String DETECT_ON_DESCENDANTS_CLAUSE = "INNER JOIN @cdm_database_schema.concept_ancestor\n ON @domain_concept_id = descendant_concept_id";
    
    public String buildExpressionQuery(CohortExpression expression) throws Exception {
        StringBuilder sb = new StringBuilder();
        if (expression.domainIds.isEmpty()) {
            throw new Exception("You must specify 1 or more domains for the expression");
        }
        try {
            String domainQuery = this.getDomainQuery(expression.domainIds, expression.detectOnDescendants);
            if (expression.occurrenceType == OccurrenceType.FIRST) {
                domainQuery = this.getFirstOccurenceQuery(domainQuery);
            }
            String insertQuery = this.getInsertCohortQuery(domainQuery);
            sb.append(insertQuery);
        } catch (Exception e) {
            throw e;
        }
        return sb.toString();
    }
    
    protected String getFirstOccurenceQuery(String domainQuery) {
        String query = FIRST_OCCURRENCE_QUERY_TEMPLATE;
        query = StringUtils.replace(query, "@domain_query", domainQuery);
        return query;
    }
    
    protected String getInsertCohortQuery(String cohortQuery) {
        String query = INSERT_COHORT_QUERY_TEMPLATE;
        query = StringUtils.replace(query, "@cohort_query", cohortQuery);
        return query;        
    }
    
    public String getDomainQuery(List<String> domainIds, boolean detectOnDescendants) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < domainIds.size(); i++) {
            try {
                sb.append(this.getDomainQuery(i, domainIds.get(i), detectOnDescendants));                
            } catch (Exception e) {
                throw e;
            }
            if (domainIds.size() > 1 && i < domainIds.size() - 1) {
                sb.append("\nUNION ALL\n");
            }
        }
        return sb.toString();
    }
    
    public String getDomainQuery(int tempTableId, String domainId, boolean detectOnDescendants) throws Exception {
        String query = DOMAIN_QUERY_TEMPLATE;
        
        String detectOnDescendantsClause = detectOnDescendants ? DETECT_ON_DESCENDANTS_CLAUSE : "";
        query = StringUtils.replace(query, "@detect_on_descendants_clause", detectOnDescendantsClause);

        DomainConfiguration dc = this.getDomainConfiguration(domainId);
        if (!dc.domainTable.isEmpty()) {
            query = StringUtils.replace(query, "@domain_table", dc.domainTable);
            query = StringUtils.replace(query, "@domain_start_date", dc.domainStartDate);
            query = StringUtils.replace(query, "@domain_end_date", dc.domainEndDate);
            query = StringUtils.replace(query, "@domain_concept_id", detectOnDescendants ? "ancestor_concept_id" : dc.domainConceptId);            
        } else {
            throw new Exception("Domain " + domainId + " not supported");
        }

        return query;
    }
    
    protected DomainConfiguration getDomainConfiguration(String domainId) {
        DomainConfiguration dc = new DomainConfiguration();
        
        switch(domainId.toUpperCase()) {
            case "CONDITION":
                dc.domainConceptId = "condition_concept_id";
                dc.domainTable = "condition_occurrence";
                dc.domainStartDate = "condition_start_date";
                dc.domainEndDate = "condition_end_date";
                break;
            case "DRUG":
                dc.domainConceptId = "drug_concept_id";
                dc.domainTable = "drug_exposure";
                dc.domainStartDate = "drug_exposure_start_date";
                dc.domainEndDate = "drug_exposure_end_date";
                break;
            case "DEVICE":
                dc.domainConceptId = "device_concept_id";
                dc.domainTable = "device_exposure";
                dc.domainStartDate = "device_exposure_start_date";
                dc.domainEndDate = "device_exposure_end_date";
                break;
            case "MEASUREMENT":
                dc.domainConceptId = "measurement_concept_id";
                dc.domainTable = "measurement";
                dc.domainStartDate = "measurement_date";
                dc.domainEndDate = "measurement_date";
                break;
            case "OBSERVATION":
                dc.domainConceptId = "observation_concept_id";
                dc.domainTable = "observation";
                dc.domainStartDate = "observation_date";
                dc.domainEndDate = "observation_date";
                break;
            case "PROCEDURE":
                dc.domainConceptId = "procedure_concept_id";
                dc.domainTable = "procedure_occurrence";
                dc.domainStartDate = "procedure_date";
                dc.domainEndDate = "procedure_date";
                break;
            case "VISIT":
                dc.domainConceptId = "visit_concept_id";
                dc.domainTable = "visit_occurrence";
                dc.domainStartDate = "visit_start_date";
                dc.domainEndDate = "visit_end_date";
                break;
        }
        
        return dc;
    }
}
