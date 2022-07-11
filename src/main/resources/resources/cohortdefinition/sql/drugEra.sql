-- Begin Drug Era Criteria
select C.person_id, C.drug_era_id as event_id, C.drug_era_start_date as start_date, C.drug_era_end_date as end_date,
    CAST(NULL as bigint) as visit_occurrence_id,C.drug_era_start_date as sort_date@additionalColumns
from 
(
  select de.* @ordinalExpression
  FROM @cdm_database_schema.DRUG_ERA de
@codesetClause
) C
@joinClause
@whereClause
-- End Drug Era Criteria
