-- Begin Visit Occurrence Criteria
select C.person_id, C.visit_occurrence_id as event_id, C.visit_start_date as start_date, C.visit_end_date as end_date,
       C.visit_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.visit_start_date as sort_date
from 
(
  select vo.* @ordinalExpression
  FROM @cdm_database_schema.VISIT_OCCURRENCE vo
@codesetClause
) C
@joinClause
@whereClause
-- End Visit Occurrence Criteria
