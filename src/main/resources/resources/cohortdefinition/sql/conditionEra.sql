-- Begin Condition Era Criteria
select C.person_id, C.condition_era_id as event_id, C.start_date, C.end_date,
  CAST(NULL as bigint) as visit_occurrence_id, C.start_date as sort_date@additionalColumns
from 
(
  select @selectClause @ordinalExpression
  FROM @cdm_database_schema.CONDITION_ERA ce
@codesetClause
) C
@joinClause
@whereClause
-- End Condition Era Criteria
