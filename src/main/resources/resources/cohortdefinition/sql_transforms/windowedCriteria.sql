SELECT @indexId as index_id, P.person_id, P.event_id
FROM @eventTable P
@joinType JOIN
(
  @criteriaQuery
) A on A.person_id = P.person_id @windowCriteria