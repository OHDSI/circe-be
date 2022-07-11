-- Begin Correlated Criteria
select @indexId as index_id, p.person_id, p.event_id
from @eventTable p
LEFT JOIN (
@windowedCriteria ) cc on p.person_id = cc.person_id and p.event_id = cc.event_id
GROUP BY p.person_id, p.event_id
@occurrenceCriteria
-- End Correlated Criteria
