-- Begin Payer Plan Period Criteria
select C.person_id, C.payer_plan_period_id as event_id, @startDateExpression as start_date, @endDateExpression as end_date, C.payer_concept_id as TARGET_CONCEPT_ID, CAST(NULL as bigint) as visit_occurrence_id
from
(
  select ppp.*, row_number() over (PARTITION BY ppp.person_id ORDER BY ppp.payer_plan_period_start_date) as ordinal
  FROM @cdm_database_schema.PAYER_PLAN_PERIOD ppp
) C
@joinClause
@whereClause
-- End Payer Plan Period Criteria
