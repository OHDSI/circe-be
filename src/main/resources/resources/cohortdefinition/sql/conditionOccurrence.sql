-- Begin Condition Occurrence Criteria
SELECT C.person_id, C.condition_occurrence_id as event_id, C.start_date, C.end_date,
  C.visit_occurrence_id, C.start_date as sort_date@additionalColumns
FROM 
(
  SELECT @selectClause @ordinalExpression
  FROM @cdm_database_schema.CONDITION_OCCURRENCE co
  @codesetClause
) C
@joinClause
@whereClause
-- End Condition Occurrence Criteria
