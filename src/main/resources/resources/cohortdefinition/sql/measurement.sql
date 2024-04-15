-- Begin Measurement Criteria
select C.person_id, C.measurement_id as event_id, C.start_date, C.end_date,
       C.visit_occurrence_id, C.start_date as sort_date@additionalColumns @concept_id @c_value_as_number @c_value_as_concept_id @c_unit_concept_id @c_provider_id @c_range_low @c_range_high @c_measurement_type_concept_id @c_operator_concept_id
from 
(
  select @selectClause @ordinalExpression
  FROM @cdm_database_schema.MEASUREMENT m
@codesetClause
) C
@joinClause
@whereClause
-- End Measurement Criteria
