-- Begin Death Criteria
select C.person_id, C.person_id as event_id, C.death_date as start_date, date_add(C.death_date, 1) as end_date,
       coalesce(C.cause_concept_id,0) as TARGET_CONCEPT_ID, CAST(NULL as bigint) as visit_occurrence_id,
       C.death_date as sort_date
from 
(
  select d.*
  FROM global_temp.death d
@codesetClause
) C
@joinClause
@whereClause
-- End Death Criteria

