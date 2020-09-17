-- Begin Drug Era Criteria
select C.person_id, C.drug_era_id as event_id, C.drug_era_start_date as start_date, C.drug_era_end_date as end_date,
       C.drug_concept_id as TARGET_CONCEPT_ID, CAST(NULL as bigint) as visit_occurrence_id,
       C.drug_era_start_date as sort_date
from 
(
  select de.* @ordinalExpression
  FROM global_temp.drug_era de
@codesetClause
) C
@joinClause
@whereClause
-- End Drug Era Criteria
