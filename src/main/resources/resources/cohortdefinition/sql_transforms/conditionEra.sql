-- Begin Condition Era Criteria
select C.person_id, C.condition_era_id as event_id, C.condition_era_start_date as start_date,
       C.condition_era_end_date as end_date, C.condition_concept_id as TARGET_CONCEPT_ID, CAST(NULL as bigint) as visit_occurrence_id,
       C.condition_era_start_date as sort_date
from 
(
  select ce.* @ordinalExpression
  FROM `@cdm_database_schema/condition_era` ce
@codesetClause
) C
@joinClause
@whereClause
-- End Condition Era Criteria
