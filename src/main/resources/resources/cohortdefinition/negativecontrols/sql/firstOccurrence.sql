{DEFAULT @cohort_id_field_name cohort_definition_id}

SELECT
	s.subject_id,
	s.@cohort_id_field_name,
	s.cohort_start_date,
	s.cohort_start_date cohort_end_date
FROM (
    SELECT
            e.subject_id,
            e.@cohort_id_field_name,
            e.cohort_start_date,
            ROW_NUMBER() OVER (PARTITION BY e.subject_id, e.@cohort_id_field_name ORDER BY e.COHORT_START_DATE ASC) ordinal
    FROM (
        @domain_query
    ) e
) s
WHERE s.ordinal = 1
;