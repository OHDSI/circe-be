-- Begin Specimen Criteria
select C.person_id, C.specimen_id as event_id, C.specimen_date as start_date, DATEADD(d,1,C.specimen_date) as end_date,
       C.specimen_concept_id as TARGET_CONCEPT_ID, CAST(NULL as bigint) as visit_occurrence_id,
       C.specimen_date as sort_date
from 
(
  select s.* @ordinalExpression
  FROM @cdm_database_schema.SPECIMEN s
@codesetClause
) C
@joinClause
@whereClause
-- End Specimen Criteria
