-- Begin Primary Events
select P.ordinal as event_id, P.person_id, P.start_date, P.end_date, op_start_date, op_end_date, cast(P.visit_occurrence_id as bigint) as visit_occurrence_id
FROM
(
  select E.person_id, E.start_date, E.end_date,
         row_number() OVER (PARTITION BY E.person_id ORDER BY E.sort_date @EventSort) ordinal,
         op.observation_period_start_date as op_start_date, op.observation_period_end_date as op_end_date, cast(E.visit_occurrence_id as bigint) as visit_occurrence_id
  FROM 
  (
  @criteriaQueries
  ) E
	JOIN `@cdm_database_schema/observation_period` op on E.person_id = op.person_id and E.start_date >=  op.observation_period_start_date and E.start_date <= op.observation_period_end_date
  WHERE @primaryEventsFilter
) P
@primaryEventLimit
-- End Primary Events
