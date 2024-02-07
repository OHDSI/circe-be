-- custom era strategy

with ctePersons(person_id) as (
	select distinct person_id from @eventTable
)

select person_id, drug_exposure_start_date, drug_exposure_end_date
INTO #drugTarget
FROM (
	select de.PERSON_ID, DRUG_EXPOSURE_START_DATE, @drugExposureEndDateExpression as DRUG_EXPOSURE_END_DATE 
	FROM @cdm_database_schema.DRUG_EXPOSURE de
	JOIN ctePersons p on de.person_id = p.person_id
	JOIN #Codesets cs on cs.codeset_id = @drugCodesetId AND de.drug_concept_id = cs.concept_id

	UNION ALL

	select de.PERSON_ID, DRUG_EXPOSURE_START_DATE, @drugExposureEndDateExpression as DRUG_EXPOSURE_END_DATE 
	FROM @cdm_database_schema.DRUG_EXPOSURE de
	JOIN ctePersons p on de.person_id = p.person_id
	JOIN #Codesets cs on cs.codeset_id = @drugCodesetId AND de.drug_source_concept_id = cs.concept_id
) E
;

select et.event_id, et.person_id, ERAS.era_end_date as end_date
INTO #strategy_ends
from @eventTable et
JOIN 
(

  select person_id, min(start_date) as era_start_date, DATEADD(day,-1 * @gapDays, max(end_date)) as era_end_date
  from (
    select person_id, start_date, end_date, sum(is_start) over (partition by person_id order by start_date, is_start desc rows unbounded preceding) group_idx
    from (
      select person_id, start_date, end_date, 
        case when max(end_date) over (partition by person_id order by start_date rows between unbounded preceding and 1 preceding) >= start_date then 0 else 1 end is_start
      from (
        select person_id, drug_exposure_start_date as start_date, DATEADD(day,(@gapDays + @offset),DRUG_EXPOSURE_END_DATE) as end_date
        FROM #drugTarget
      ) DT
    ) ST
  ) GR
  group by person_id, group_idx
) ERAS on ERAS.person_id = et.person_id 
WHERE et.start_date between ERAS.era_start_date and ERAS.era_end_date;

TRUNCATE TABLE #drugTarget;
DROP TABLE #drugTarget;
