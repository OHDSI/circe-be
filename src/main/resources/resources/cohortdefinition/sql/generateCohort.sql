with primary_events as
(
@primaryEventsQuery
), 
qualified_events AS (
	SELECT event_id, person_id, start_date, end_date, op_start_date, op_end_date, visit_occurrence_id
	FROM 
	(
		select pe.event_id, pe.person_id, pe.start_date, pe.end_date, pe.op_start_date, pe.op_end_date, row_number() over (partition by pe.person_id order by pe.start_date @QualifiedEventSort) as ordinal, cast(pe.visit_occurrence_id as bigint) as visit_occurrence_id
		FROM primary_events pe
	@additionalCriteriaQuery
	) QE
	@QualifiedLimitFilter
),

--- Inclusion Rule Inserts

@inclusionCohortInserts

cteIncludedEvents as
(
  SELECT event_id, person_id, start_date, end_date, op_start_date, op_end_date, row_number() over (partition by person_id order by start_date @IncludedEventSort) as ordinal
  from
  (
    select Q.event_id, Q.person_id, Q.start_date, Q.end_date, Q.op_start_date, Q.op_end_date, SUM(coalesce(POWER(cast(2 as bigint), I.inclusion_rule_id), 0)) as inclusion_rule_mask
    from qualified_events Q
    LEFT JOIN inclusion_events I on I.person_id = Q.person_id and I.event_id = Q.event_id
    GROUP BY Q.event_id, Q.person_id, Q.start_date, Q.end_date, Q.op_start_date, Q.op_end_date
  ) MG -- matching groups
{@ruleTotal != 0}?{
  -- the matching group with all bits set ( POWER(2,# of inclusion rules) - 1 = inclusion_rule_mask
  WHERE (MG.inclusion_rule_mask = POWER(cast(2 as bigint),@ruleTotal)-1)
}
), 

included_events AS (
	select event_id, person_id, start_date, end_date, op_start_date, op_end_date
	FROM cteIncludedEvents Results
	@ResultLimitFilter
),

@strategy_ends_temp_tables

-- generate cohort periods into #final_cohort
cohort_ends as
(
	-- cohort exit dates
  @cohort_end_unions
),

first_ends as
(
	select F.person_id, F.start_date, F.end_date
	FROM (
	  select I.event_id, I.person_id, I.start_date, E.end_date, row_number() over (partition by I.person_id, I.event_id order by E.end_date) as ordinal 
	  from included_events I
	  join cohort_ends E on I.event_id = E.event_id1 and I.person_id = E.person_id and E.end_date >= I.start_date
	) F
	WHERE F.ordinal = 1
),

cohort_rows AS (
	select person_id, start_date, end_date
	from first_ends
),

cteEndDates AS -- the magic
(	
	SELECT
		person_id
		, date_add(event_date, -1 * @eraconstructorpad)  as end_date
	FROM
	(
		SELECT
			person_id
			, event_date
			, event_type
			, MAX(start_ordinal) OVER (PARTITION BY person_id ORDER BY event_date, event_type ROWS UNBOUNDED PRECEDING) AS start_ordinal 
			, ROW_NUMBER() OVER (PARTITION BY person_id ORDER BY event_date, event_type) AS overall_ord
		FROM
		(
			SELECT
				person_id
				, start_date AS event_date
				, -1 AS event_type
				, ROW_NUMBER() OVER (PARTITION BY person_id ORDER BY start_date) AS start_ordinal
			FROM cohort_rows
		
			UNION ALL
		

			SELECT
				person_id
				, date_add(end_date, @eraconstructorpad) as end_date
				, 1 AS event_type
				, NULL
			FROM cohort_rows
		) RAWDATA
	) e
	WHERE (2 * e.start_ordinal) - e.overall_ord = 0
),
cteEnds AS
(
	SELECT
		 c.person_id
		, c.start_date
		, MIN(e.end_date) AS end_date
	FROM cohort_rows c
	JOIN cteEndDates e ON c.person_id = e.person_id AND e.end_date >= c.start_date
	GROUP BY c.person_id, c.start_date
),
final_cohort AS (
	select person_id, min(start_date) as start_date, end_date
	from cteEnds
	group by person_id, end_date
)

@finalCohortQuery


{@generateStats != 0}?{
-- Find the event that is the 'best match' per person.  
-- the 'best match' is defined as the event that satisfies the most inclusion rules.
-- ties are solved by choosing the event that matches the earliest inclusion rule, and then earliest.

select q.person_id, q.event_id
into #best_events
from #qualified_events Q
join (
	SELECT R.person_id, R.event_id, ROW_NUMBER() OVER (PARTITION BY R.person_id ORDER BY R.rule_count DESC,R.min_rule_id ASC, R.start_date ASC) AS rank_value
	FROM (
		SELECT Q.person_id, Q.event_id, COALESCE(COUNT(DISTINCT I.inclusion_rule_id), 0) AS rule_count, COALESCE(MIN(I.inclusion_rule_id), 0) AS min_rule_id, Q.start_date
		FROM #qualified_events Q
		LEFT JOIN #inclusion_events I ON q.person_id = i.person_id AND q.event_id = i.event_id
		GROUP BY Q.person_id, Q.event_id, Q.start_date
	) R
) ranked on Q.person_id = ranked.person_id and Q.event_id = ranked.event_id
WHERE ranked.rank_value = 1
;

-- modes of generation: (the same tables store the results for the different modes, identified by the mode_id column)
-- 0: all events
-- 1: best event


-- BEGIN: Inclusion Impact Analysis - event
@inclusionImpactAnalysisByEventQuery
-- END: Inclusion Impact Analysis - event

-- BEGIN: Inclusion Impact Analysis - person
@inclusionImpactAnalysisByPersonQuery
-- END: Inclusion Impact Analysis - person

-- BEGIN: Censored Stats
@cohortCensoredStatsQuery
-- END: Censored Stats

TRUNCATE TABLE #best_events;
DROP TABLE #best_events;

}


