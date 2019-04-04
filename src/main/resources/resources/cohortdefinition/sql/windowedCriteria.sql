SELECT @indexId as index_id, p.person_id, p.event_id
FROM @eventTable P
@joinType JOIN
(
  @criteriaQuery
) A on A.person_id = P.person_id @windowCriteria