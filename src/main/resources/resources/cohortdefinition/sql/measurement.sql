-- Begin Measurement Criteria
select C.person_id, C.measurement_id as event_id, C.measurement_date as start_date, DATEADD(d,1,C.measurement_date) as END_DATE,
       C.visit_occurrence_id, C.measurement_date as sort_date@additionalColumns
from 
(
  select m.* @ordinalExpression
  FROM @cdm_database_schema.MEASUREMENT m
@codesetClause
) C
@joinClause
@whereClause
-- End Measurement Criteria
