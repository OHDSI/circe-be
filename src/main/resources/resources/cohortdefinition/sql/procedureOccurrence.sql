-- Begin Procedure Occurrence Criteria
select C.person_id, C.procedure_occurrence_id as event_id, C.procedure_date as start_date, DATEADD(d,1,C.procedure_date) as END_DATE,
       C.procedure_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.procedure_date as sort_date
from 
(
  select po.* @ordinalExpression
  FROM @cdm_database_schema.PROCEDURE_OCCURRENCE po
@codesetClause
) C
@joinClause
@whereClause
-- End Procedure Occurrence Criteria
