select C.person_id, C.event_id, C.start_date, C.end_date,
  CAST(NULL as bigint) as visit_occurrence_id, C.start_date as sort_date@additionalColumns
from
(
  select @selectClause @ordinalExpression
  from
  (
    select person_id, min(start_date) as start_date, DATEADD(day,-1 * @eraconstructorpad, max(end_date)) as end_date
    from (
      select person_id, start_date, end_date, sum(is_start) over (partition by person_id order by start_date, is_start desc rows unbounded preceding) group_idx
      from (
        select person_id, start_date, DATEADD(day,@eraconstructorpad,end_date) as end_date,
          case when max(end_date) over (partition by person_id order by start_date rows between unbounded preceding and 1 preceding) >= start_date then 0 else 1 end is_start
        from (
          @criteriaQueries
        ) D
      ) CR
    ) ST
    group by person_id, group_idx
  ) E
) C
@joinClause
@whereClause
