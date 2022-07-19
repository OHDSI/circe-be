-- Begin Observation Period Criteria
select C.person_id, C.observation_period_id as event_id, @startDateExpression as start_date, @endDateExpression as end_date,
       CAST(NULL as bigint) as visit_occurrence_id, C.start_date as sort_date@additionalColumns

from 
(
  select @selectClause , row_number() over (PARTITION BY op.person_id ORDER BY op.observation_period_start_date) as ordinal
  FROM @cdm_database_schema.OBSERVATION_PERIOD op
) C
@joinClause
@whereClause
-- End Observation Period Criteria
