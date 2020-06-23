-- date offset strategy

strategy_ends AS (
  select event_id, person_id, 
  case when date_add(@dateField, @offset) > start_date then date_add(@dateField, @offset) else start_date end as end_date
  from @eventTable
), 