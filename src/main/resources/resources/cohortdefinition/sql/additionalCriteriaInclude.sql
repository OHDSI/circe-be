-- Begin Correlated Criteria
select @indexId as index_id, cc.person_id, cc.event_id@additionColumnscc
from (@windowedCriteria ) cc 
GROUP BY cc.person_id, cc.event_id@additionColumnscc
@occurrenceCriteria
-- End Correlated Criteria
