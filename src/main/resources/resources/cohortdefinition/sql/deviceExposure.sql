-- Begin Device Exposure Criteria
select C.person_id, C.device_exposure_id as event_id, C.start_date, C.end_date,
        C.visit_occurrence_id, C.start_date as sort_date@additionalColumns
from 
(
  select @selectClause @ordinalExpression
  FROM @cdm_database_schema.DEVICE_EXPOSURE de
@codesetClause
) C
@joinClause
@whereClause
-- End Device Exposure Criteria
