-- Begin Condition Era Criteria
select C.person_id, C.condition_era_id as event_id, C.condition_era_start_date as start_date, C.condition_era_end_date as end_date, C.CONDITION_CONCEPT_ID as TARGET_CONCEPT_ID, NULL as visit_occurrence_id
from 
(
  select ce.*, row_number() over (PARTITION BY ce.person_id ORDER BY ce.condition_era_start_date, ce.condition_era_id) as ordinal
  FROM @cdm_database_schema.CONDITION_ERA ce
@codesetClause
) C
@joinClause
@whereClause
-- End Condition Era Criteria
