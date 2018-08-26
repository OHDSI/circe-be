SELECT d.person_id subject_id,
        c.ancestor_concept_id cohort_definition_id,
        d.@domain_start_date cohort_start_date,
        d.@domain_start_date cohort_end_date
FROM @cdm_database_schema.@domain_table d
INNER JOIN #Codesets c ON c.concept_id = d.@domain_concept_id