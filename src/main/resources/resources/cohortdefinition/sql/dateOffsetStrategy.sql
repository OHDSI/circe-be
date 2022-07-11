-- date offset strategy

select event_id, person_id, 
  case when DATEADD(day,@offset,@dateField) > op_end_date then op_end_date else DATEADD(day,@offset,@dateField) end as end_date
INTO #strategy_ends
from @eventTable;
