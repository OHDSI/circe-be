@codesetQuery

with primary_events (event_id, person_id, start_date, end_date, op_start_date, op_end_date, visit_occurrence_id) as
(
@primaryEventsQuery
)
SELECT event_id, person_id, start_date, end_date, op_start_date, op_end_date, visit_occurrence_id
INTO #qualified_events
FROM 
(
  select pe.event_id, pe.person_id, pe.start_date, pe.end_date, pe.op_start_date, pe.op_end_date, dense_rank() over (partition by pe.person_id order by pe.start_date @QualifiedEventSort) as ordinal, pe.visit_occurrence_id
  FROM primary_events pe
  @additionalCriteriaQuery
) QE
@QualifiedLimitFilter
;


create table #inclusionRuleCohorts 
(
  inclusion_rule_id bigint,
  person_id bigint,
  event_id bigint
)
;
@inclusionCohortInserts

with cteIncludedEvents(event_id, person_id, start_date, end_date, op_start_date, op_end_date, ordinal) as
(
  SELECT event_id, person_id, start_date, end_date, op_start_date, op_end_date, row_number() over (partition by person_id order by start_date @IncludedEventSort) as ordinal
  from
  (
    select Q.event_id, Q.person_id, Q.start_date, Q.end_date, Q.op_start_date, Q.op_end_date, SUM(coalesce(POWER(cast(2 as bigint), I.inclusion_rule_id), 0)) as inclusion_rule_mask
    from #qualified_events Q
    LEFT JOIN #inclusionRuleCohorts I on I.person_id = Q.person_id and I.event_id = Q.event_id
    GROUP BY Q.event_id, Q.person_id, Q.start_date, Q.end_date, Q.op_start_date, Q.op_end_date
  ) MG -- matching groups
{@ruleTotal != 0}?{
  -- the matching group with all bits set ( POWER(2,# of inclusion rules) - 1 = inclusion_rule_mask
  WHERE (MG.inclusion_rule_mask = POWER(cast(2 as bigint),@ruleTotal)-1)
}
)
select event_id, person_id, start_date, end_date, op_start_date, op_end_date
into #included_events
FROM cteIncludedEvents Results
@ResultLimitFilter
;

-- Apply end date stratagies
-- by default, all events extend to the op_end_date.
select event_id, person_id, op_end_date as end_date
into #cohort_ends
from #included_events;

@strategyInserts

@censoringInserts



with collapse_constructor_input (person_id, start_date, end_date) as
(
	select F.person_id, F.start_date, F.end_date
	FROM (
	  select I.event_id, I.person_id, I.start_date, E.end_date, row_number() over (partition by I.person_id, I.event_id order by E.end_date) as ordinal 
	  from #included_events I
	  join #cohort_ends E on I.event_id = E.event_id and I.person_id = E.person_id and E.end_date >= I.start_date
	) F
	WHERE F.ordinal = 1
)
select person_id, start_date, end_date
into #collapse_constructor_input
from collapse_constructor_input
;

@collapseConstructor

DELETE FROM @target_database_schema.@target_cohort_table where cohort_definition_id = @target_cohort_id;
INSERT INTO @target_database_schema.@target_cohort_table (cohort_definition_id, subject_id, cohort_start_date, cohort_end_date)
select @target_cohort_id as cohort_definition_id, person_id, start_date, end_date
FROM @output_table CO --@output: change depending on what is selected for collapse construction
;

{@generateStats != 0}?{
-- calculte matching group counts
delete from @results_database_schema.cohort_inclusion_result where cohort_definition_id = @target_cohort_id;
insert into @results_database_schema.cohort_inclusion_result (cohort_definition_id, inclusion_rule_mask, person_count)
select @target_cohort_id as cohort_definition_id, inclusion_rule_mask, count(*) as person_count
from
(
  select Q.person_id, Q.event_id, CAST(SUM(coalesce(POWER(cast(2 as bigint), I.inclusion_rule_id), 0)) AS bigint) as inclusion_rule_mask
  from #qualified_events Q
  LEFT JOIN #inclusionRuleCohorts I on q.person_id = i.person_id and q.event_id = i.event_id
  GROUP BY Q.person_id, Q.event_id
) MG -- matching groups
group by inclusion_rule_mask
;

-- calculate gain counts
delete from @results_database_schema.cohort_inclusion_stats where cohort_definition_id = @target_cohort_id;
insert into @results_database_schema.cohort_inclusion_stats (cohort_definition_id, rule_sequence, person_count, gain_count, person_total)
select ir.cohort_definition_id, ir.rule_sequence, coalesce(T.person_count, 0) as person_count, coalesce(SR.person_count, 0) gain_count, EventTotal.total
from @results_database_schema.cohort_inclusion ir
left join
(
  select i.inclusion_rule_id, count(i.event_id) as person_count
  from #qualified_events Q
  JOIN #inclusionRuleCohorts i on Q.person_id = I.person_id and Q.event_id = i.event_id
  group by i.inclusion_rule_id
) T on ir.rule_sequence = T.inclusion_rule_id
CROSS JOIN (select count(*) as total_rules from @results_database_schema.cohort_inclusion where cohort_definition_id = @target_cohort_id) RuleTotal
CROSS JOIN (select count(event_id) as total from #qualified_events) EventTotal
LEFT JOIN @results_database_schema.cohort_inclusion_result SR on SR.cohort_definition_id = @target_cohort_id AND (POWER(cast(2 as bigint),RuleTotal.total_rules) - POWER(cast(2 as bigint),ir.rule_sequence) - 1) = SR.inclusion_rule_mask -- POWER(2,rule count) - POWER(2,rule sequence) - 1 is the mask for 'all except this rule' 
WHERE ir.cohort_definition_id = @target_cohort_id
;

-- calculate totals
delete from @results_database_schema.cohort_summary_stats where cohort_definition_id = @target_cohort_id;
insert into @results_database_schema.cohort_summary_stats (cohort_definition_id, base_count, final_count)
select @target_cohort_id as cohort_definition_id, PC.total as person_count, coalesce(FC.total, 0) as final_count
FROM
(select count(event_id) as total from #qualified_events) PC,
(select sum(sr.person_count) as total
  from @results_database_schema.cohort_inclusion_result sr
  CROSS JOIN (select count(*) as total_rules from @results_database_schema.cohort_inclusion where cohort_definition_id = @target_cohort_id) RuleTotal
  where cohort_definition_id = @target_cohort_id and sr.inclusion_rule_mask = POWER(cast(2 as bigint),RuleTotal.total_rules)-1
) FC
;
}

TRUNCATE TABLE #collapse_constructor_input;
DROP TABLE #collapse_constructor_input;

TRUNCATE TABLE #collapse_constructor_output;
DROP TABLE #collapse_constructor_output;

TRUNCATE TABLE #cohort_ends;
DROP TABLE #cohort_ends;

TRUNCATE TABLE #inclusionRuleCohorts;
DROP TABLE #inclusionRuleCohorts;

TRUNCATE TABLE #qualified_events;
DROP TABLE #qualified_events;

TRUNCATE TABLE #included_events;
DROP TABLE #included_events;

TRUNCATE TABLE #Codesets;
DROP TABLE #Codesets;
