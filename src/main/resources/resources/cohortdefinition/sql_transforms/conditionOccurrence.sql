-- Begin Condition Occurrence Criteria
SELECT C.person_id, C.condition_occurrence_id as event_id, C.condition_start_date as start_date, COALESCE(C.condition_end_date, date_add(C.condition_start_date, 1)) as end_date,
       C.condition_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.condition_start_date as sort_date
FROM 
(
  SELECT co.* @ordinalExpression
  FROM `@cdm_database_schema/condition_occurrence` co
  @codesetClause
) C
@joinClause
@whereClause
-- End Condition Occurrence Criteria
