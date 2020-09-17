-- Begin Device Exposure Criteria
select C.person_id, C.device_exposure_id as event_id, C.device_exposure_start_date as start_date, C.device_exposure_end_date as end_date,
       C.device_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.device_exposure_start_date as sort_date
from 
(
  select de.* @ordinalExpression
  FROM global_temp.device_exposure de
@codesetClause
) C
@joinClause
@whereClause
-- End Device Exposure Criteria
