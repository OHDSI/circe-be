-- Begin Death Criteria
select C.person_id, C.person_id as event_id, C.start_date, c.end_date,
  CAST(NULL as bigint) as visit_occurrence_id, C.start_date as sort_date@additionalColumns
from 
(
  select @selectClause
  FROM @cdm_database_schema.DEATH d
@codesetClause
) C
@joinClause
@whereClause
-- End Death Criteria

