SELECT 
	s.subject_id,
	s.cohort_definition_id,
	s.cohort_start_date,
	s.cohort_start_date cohort_end_date
FROM (
    SELECT
            e.subject_id,
            e.cohort_definition_id,
            e.cohort_start_date,
            ROW_NUMBER() OVER (PARTITION BY e.subject_id, e.cohort_definition_id ORDER BY e.COHORT_START_DATE ASC) ordinal
    FROM (
        @domain_query
    ) e
) s
WHERE s.ordinal = 1
;