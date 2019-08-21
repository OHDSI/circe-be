{DEFAULT @cohort_id_field_name cohort_definition_id}

SELECT d.person_id subject_id,
        c.ancestor_concept_id @cohort_id_field_name,
        d.@domain_start_date cohort_start_date
FROM @cdm_database_schema.@domain_table d
INNER JOIN #Codesets c ON c.concept_id = d.@domain_concept_id