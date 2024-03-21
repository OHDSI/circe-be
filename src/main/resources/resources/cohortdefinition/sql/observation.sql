-- Begin Observation Criteria
select C.person_id, C.observation_id as event_id, C.start_date, C.END_DATE,
       C.visit_occurrence_id, C.start_date as sort_date@additionalColumns @concept_id @c_observation_type_concept_id @c_value_as_number @c_value_as_string @c_value_as_concept_id @c_qualifier_concept_id @c_unit_concept_id @c_provider_id
from 
(
  select @selectClause @ordinalExpression
  FROM @cdm_database_schema.OBSERVATION o
@codesetClause
) C
@joinClause
@whereClause
-- End Observation Criteria
