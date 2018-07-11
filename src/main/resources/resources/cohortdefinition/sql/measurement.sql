-- Begin Measurement Criteria
select C.person_id, C.measurement_id as event_id, C.measurement_date as start_date, DATEADD(d,1,C.measurement_date) as END_DATE, C.measurement_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id
from 
(
  select m.* @ordinalExpression
  FROM @cdm_database_schema.MEASUREMENT m
@codesetClause
) C
@joinClause
@whereClause
-- End Measurement Criteria
