-- Begin Episode Criteria
select C.person_id, C.episode_id as event_id, C.start_date, C.end_date,
       CAST(NULL as bigint) as visit_occurrence_id, C.start_date as sort_date@additionalColumns
from
(
  select @selectClause @ordinalExpression
  FROM @cdm_database_schema.EPISODE ep
@codesetClause
) C
@joinClause
@whereClause
-- End Episode Criteria