{DEFAULT @cohort_id_field_name = 'cohort_definition_id'}

INSERT INTO @target_database_schema.@target_cohort_table (
	subject_id,
	@cohort_id_field_name,
	cohort_start_date,
	cohort_end_date
)
@cohort_query