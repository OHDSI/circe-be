-- Begin Device Exposure Criteria
select C.person_id, C.device_exposure_id as event_id, C.device_exposure_start_date as start_date, COALESCE(C.device_exposure_end_date, DATEADD(day,1,C.device_exposure_start_date)) as end_date,
        C.visit_occurrence_id, C.device_exposure_start_date as sort_date@additionalColumns
from 
(
  select de.* @ordinalExpression
  FROM @cdm_database_schema.DEVICE_EXPOSURE de
@codesetClause
) C
@joinClause
@whereClause
-- End Device Exposure Criteria
