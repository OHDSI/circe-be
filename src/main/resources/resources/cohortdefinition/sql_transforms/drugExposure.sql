-- Begin Drug Exposure Criteria
select C.person_id, C.drug_exposure_id as event_id, C.drug_exposure_start_date as start_date,
       COALESCE(C.drug_exposure_end_date, date_add(C.drug_exposure_start_date, C.days_supply), date_add(C.drug_exposure_start_date, 1)) as end_date,
       C.drug_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.drug_exposure_start_date as sort_date
from 
(
  select de.* @ordinalExpression
  FROM `@cdm_database_schema/drug_exposure` de
@codesetClause
) C
@joinClause
@whereClause
-- End Drug Exposure Criteria
