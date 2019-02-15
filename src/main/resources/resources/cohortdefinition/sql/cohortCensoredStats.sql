-- calculate censored
delete from @results_database_schema.cohort_censor_stats where cohort_definition_id = @target_cohort_id;
insert into @results_database_schema.cohort_censor_stats (cohort_definition_id, lost_count)
	select @target_cohort_id as cohort_definition_id, coalesce(FCC.total_people - TC.total, 0) as lost_count
	FROM
		(select count_big(distinct person_id) as total_people from #final_cohort) FCC,
		(select count_big(distinct subject_id) as total from @target_database_schema.@target_cohort_table t where t.cohort_definition_id = @target_cohort_id) TC;