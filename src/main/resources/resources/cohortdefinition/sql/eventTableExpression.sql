SELECT Q.person_id, Q.event_id, Q.start_date, Q.end_date, Q.visit_occurrence_id, op.observation_period_start_date as op_start_date, op.observation_period_end_date as op_end_date
FROM (@eventQuery) Q
JOIN global_temp.observation_period op on Q.person_id = op.person_id 
  and op.observation_period_start_date <= Q.start_date and op.observation_period_end_date >= Q.start_date
