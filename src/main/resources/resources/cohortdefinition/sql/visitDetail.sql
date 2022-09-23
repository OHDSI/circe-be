-- Begin Visit Detail Criteria
select C.person_id, C.visit_detail_id as event_id, C.visit_detail_start_date as start_date, C.visit_detail_end_date as end_date,
       C.visit_occurrence_id, C.visit_detail_start_date as sort_date@additionalColumns
from 
(
  select vd.* @ordinalExpression
  FROM @cdm_database_schema.VISIT_DETAIL vd
@codesetClause
) C
@joinClause
@whereClause
-- End Visit Detail Criteria
