-- Begin Drug Exposure Criteria
select C.person_id, C.drug_exposure_id as event_id, C.drug_exposure_start_date as start_date,
       COALESCE(C.DRUG_EXPOSURE_END_DATE, DATEADD(day,C.DAYS_SUPPLY,DRUG_EXPOSURE_START_DATE), DATEADD(day,1,C.DRUG_EXPOSURE_START_DATE)) as end_date,
       C.drug_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.drug_exposure_start_date as sort_date
from 
(
  select de.* @ordinalExpression
  FROM @cdm_database_schema.DRUG_EXPOSURE de
@codesetClause
) C
@joinClause
@whereClause
-- End Drug Exposure Criteria
