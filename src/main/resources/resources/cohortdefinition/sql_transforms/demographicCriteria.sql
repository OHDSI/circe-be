-- Begin Demographic Criteria
SELECT @indexId as index_id, e.person_id, e.event_id
FROM @eventTable e
JOIN `@cdm_database_schema/person` P ON P.person_id = e.person_id
@whereClause
GROUP BY e.person_id, e.event_id
-- End Demographic Criteria
