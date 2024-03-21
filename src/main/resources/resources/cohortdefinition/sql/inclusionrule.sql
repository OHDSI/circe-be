select @inclusion_rule_id as inclusion_rule_id, person_id,
       event_id,
       start_date, end_date @addColumnQeTempId @concept_id @additionalColumnsInclusionN
INTO #Inclusion_@inclusion_rule_id
FROM
  (
    select pe.person_id, pe.event_id, pe.start_date, pe.end_date @addColumnQeTempId @conceptid @additionalColumnsCriteriaQuery
    FROM #qualified_events pe
      @additionalCriteriaQuery
  ) Results
;