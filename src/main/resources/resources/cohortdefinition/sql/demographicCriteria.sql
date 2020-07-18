-- Begin Demographic Criteria
SELECT @indexId as index_id, E.person_id, E.event_id
FROM @eventTable E
JOIN global_temp.person P ON P.person_id = E.person_id
@whereClause
GROUP BY E.person_id, E.event_id
-- End Demographic Criteria
