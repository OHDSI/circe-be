select i.event_id, i.person_id, MIN(c.start_date) as end_date
FROM included_events i
JOIN
(
@criteriaQuery
) C on C.person_id = i.person_id and C.start_date >= i.start_date and C.start_date <= i.op_end_date
GROUP BY i.event_id, i.person_id
