-- Begin Location region Criteria
select
  C.entity_id AS person_id,
  C.location_history_id as event_id,
  C.start_date as start_date,
  ISNULL(C.end_date, DATEFROMPARTS(2099,12,31)) as end_date,
  C.region_concept_id as TARGET_CONCEPT_ID,
  CAST(NULL as bigint) as visit_occurrence_id,
  C.start_date as sort_date
from 
(
  select l.*, lh.*
  FROM global_temp.location l
    JOIN global_temp.location_history lh ON l.location_id = lh.location_id
@codesetClause
  WHERE lh.domain_id = 'PERSON'
) C
-- End Location region Criteria
