SELECT p.person_id, p.event_id, A.start_date, A.end_date, A.target_concept_id
FROM @eventTable P
@joinType JOIN (
  @criteriaQuery
) A on A.person_id = P.person_id @windowCriteria