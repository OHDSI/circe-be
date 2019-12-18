-- Begin Condition Occurrence Criteria
SELECT C.person_id, C.condition_occurrence_id as event_id, C.condition_start_date as start_date, COALESCE(C.condition_end_date, DATEADD(day,1,C.condition_start_date)) as end_date,
       C.CONDITION_CONCEPT_ID as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.condition_start_date as sort_date
FROM 
(
  SELECT co.* @ordinalExpression
  FROM @cdm_database_schema.CONDITION_OCCURRENCE co
  @codesetClause
) C
@joinClause
@whereClause
-- End Condition Occurrence Criteria
