-- Begin Drug Exposure Criteria
select C.person_id, C.drug_exposure_id as event_id, C.start_date, C.end_date,
  C.visit_occurrence_id,C.start_date as sort_date@additionalColumns
from 
(
  select @selectClause @ordinalExpression
  FROM @cdm_database_schema.DRUG_EXPOSURE de
@codesetClause
) C
@joinClause
@whereClause
-- End Drug Exposure Criteria
