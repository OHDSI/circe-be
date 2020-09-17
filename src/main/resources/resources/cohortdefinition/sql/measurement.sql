-- Begin Measurement Criteria
select C.person_id, C.measurement_id as event_id, C.measurement_date as start_date, date_add(C.measurement_date, 1) as end_date,
       C.measurement_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.measurement_date as sort_date
from 
(
  select m.* @ordinalExpression
  FROM global_temp.measurement m
@codesetClause
) C
@joinClause
@whereClause
-- End Measurement Criteria
