-- Begin Observation Criteria
select C.person_id, C.observation_id as event_id, C.observation_date as start_date, date_add(C.observation_date, 1) as end_date,
       C.observation_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.observation_date as sort_date
from 
(
  select o.* @ordinalExpression
  FROM `@cdm_database_schema/observation` o
@codesetClause
) C
@joinClause
@whereClause
-- End Observation Criteria
