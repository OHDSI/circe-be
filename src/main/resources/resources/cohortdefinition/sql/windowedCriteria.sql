SELECT p.person_id, p.event_id @additionalColumns
FROM @eventTable P
JOIN (
  @criteriaQuery
) A on A.person_id = P.person_id @windowCriteria