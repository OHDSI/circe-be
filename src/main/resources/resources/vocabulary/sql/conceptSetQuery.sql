select concept_id from @vocabulary_database_schema.CONCEPT where concept_id in (@conceptIds)and invalid_reason is null
