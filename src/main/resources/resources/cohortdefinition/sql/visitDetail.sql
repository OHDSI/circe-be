-- Begin Visit Detail Criteria
select C.person_id, C.visit_detail_id as event_id, C.start_date, C.end_date,
       C.visit_occurrence_id, C.start_date as sort_date@additionalColumns
from 
(
  select @selectClause @ordinalExpression
  FROM @cdm_database_schema.VISIT_DETAIL vd
@codesetClause
) C
@joinClause
@whereClause
-- End Visit Detail Criteria
