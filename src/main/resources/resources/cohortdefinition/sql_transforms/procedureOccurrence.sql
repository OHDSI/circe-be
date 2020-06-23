-- Begin Procedure Occurrence Criteria
select C.person_id, C.procedure_occurrence_id as event_id, C.procedure_date as start_date, date_add(C.procedure_date, 1) as end_date,
       C.procedure_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.procedure_date as sort_date
from 
(
  select po.* @ordinalExpression
  FROM `@cdm_database_schema/procedure_occurrence` po
@codesetClause
) C
@joinClause
@whereClause
-- End Procedure Occurrence Criteria
