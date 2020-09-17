-- Begin Correlated Criteria
@windowedCriteria
GROUP BY p.person_id, p.event_id
@occurrenceCriteria
-- End Correlated Criteria
