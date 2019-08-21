{DEFAULT @cohort_id_field_name cohort_definition_id}

SELECT
	s.subject_id,
	s.@cohort_id_field_name,
	s.cohort_start_date,
	s.cohort_start_date cohort_end_date
FROM (
    @domain_query
) s
;