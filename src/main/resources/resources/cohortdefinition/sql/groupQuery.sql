-- Begin Criteria Group
select @indexId as index_id, person_id, event_id @additonColumnsGroup
FROM
(
  select E.person_id, E.event_id@e.additonColumns
  FROM @eventTable E
  @joinType JOIN
  (
    @criteriaQueries
  ) CQ on E.person_id = CQ.person_id and E.event_id = CQ.event_id
  GROUP BY E.person_id, E.event_id@e.additonGroupColumns
  @occurrenceCountClause
) G
-- End Criteria Group
