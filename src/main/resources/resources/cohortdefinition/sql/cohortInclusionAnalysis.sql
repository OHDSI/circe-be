-- calculte matching group counts
delete from @results_database_schema.cohort_inclusion_result where @cohort_id_field_name = @target_cohort_id and mode_id = @inclusionImpactMode;
insert into @results_database_schema.cohort_inclusion_result (@cohort_id_field_name, inclusion_rule_mask, person_count, mode_id)
select @target_cohort_id as @cohort_id_field_name, inclusion_rule_mask, count_big(*) as person_count, @inclusionImpactMode as mode_id
from
(
  select Q.person_id, Q.event_id, CAST(SUM(coalesce(POWER(cast(2 as bigint), I.inclusion_rule_id), 0)) AS bigint) as inclusion_rule_mask
  from @eventTable Q
  LEFT JOIN #inclusion_events I on q.person_id = i.person_id and q.event_id = i.event_id
  GROUP BY Q.person_id, Q.event_id
) MG -- matching groups
group by inclusion_rule_mask
;

-- calculate gain counts 
delete from @results_database_schema.cohort_inclusion_stats where @cohort_id_field_name = @target_cohort_id and mode_id = @inclusionImpactMode;
insert into @results_database_schema.cohort_inclusion_stats (@cohort_id_field_name, rule_sequence, person_count, gain_count, person_total, mode_id)
select @target_cohort_id as @cohort_id_field_name, ir.rule_sequence, coalesce(T.person_count, 0) as person_count, coalesce(SR.person_count, 0) gain_count, EventTotal.total, @inclusionImpactMode as mode_id
from #inclusion_rules ir
left join
(
  select i.inclusion_rule_id, count_big(i.event_id) as person_count
  from @eventTable Q
  JOIN #inclusion_events i on Q.person_id = I.person_id and Q.event_id = i.event_id
  group by i.inclusion_rule_id
) T on ir.rule_sequence = T.inclusion_rule_id
CROSS JOIN (select count(*) as total_rules from #inclusion_rules) RuleTotal
CROSS JOIN (select count_big(event_id) as total from @eventTable) EventTotal
LEFT JOIN @results_database_schema.cohort_inclusion_result SR on SR.mode_id = @inclusionImpactMode AND SR.@cohort_id_field_name = @target_cohort_id AND (POWER(cast(2 as bigint),RuleTotal.total_rules) - POWER(cast(2 as bigint),ir.rule_sequence) - 1) = SR.inclusion_rule_mask -- POWER(2,rule count) - POWER(2,rule sequence) - 1 is the mask for 'all except this rule'
;

-- calculate totals
delete from @results_database_schema.cohort_summary_stats where @cohort_id_field_name = @target_cohort_id and mode_id = @inclusionImpactMode;
insert into @results_database_schema.cohort_summary_stats (@cohort_id_field_name, base_count, final_count, mode_id)
select @target_cohort_id as @cohort_id_field_name, PC.total as person_count, coalesce(FC.total, 0) as final_count, @inclusionImpactMode as mode_id
FROM
(select count_big(event_id) as total from @eventTable) PC,
(select sum(sr.person_count) as total
  from @results_database_schema.cohort_inclusion_result sr
  CROSS JOIN (select count(*) as total_rules from #inclusion_rules) RuleTotal
  where sr.mode_id = @inclusionImpactMode and sr.@cohort_id_field_name = @target_cohort_id and sr.inclusion_rule_mask = POWER(cast(2 as bigint),RuleTotal.total_rules)-1
  ) FC
;
