-- Begin Specimen Criteria
select C.person_id, C.specimen_id as event_id, C.specimen_date as start_date, date_add(C.specimen_date, 1) as end_date,
       C.specimen_concept_id as TARGET_CONCEPT_ID, CAST(NULL as bigint) as visit_occurrence_id,
       C.specimen_date as sort_date
from 
(
  select s.* @ordinalExpression
  FROM `@cdm_database_schema/specimen` s
@codesetClause
) C
@joinClause
@whereClause
-- End Specimen Criteria
