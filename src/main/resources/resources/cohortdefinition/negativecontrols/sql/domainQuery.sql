SELECT person_id subject_id,
        @domain_concept_id cohort_definition_id,
        @domain_start_date cohort_start_date,
        @domain_end_date cohort_end_date
FROM @cdm_database_schema.@domain_table
@detect_on_descendants_clause
WHERE @domain_concept_id IN (@outcome_ids)