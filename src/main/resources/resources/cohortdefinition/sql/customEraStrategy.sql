-- custom era strategy

ctePersons as (
	select distinct person_id from @eventTable
),

drug_target as (
	select person_id, drug_exposure_start_date, drug_exposure_end_date
	FROM (
		select de.person_id, drug_exposure_start_date, @drugExposureEndDateExpression as drug_exposure_end_date 
		FROM global_temp.drug_exposure de
		JOIN ctePersons p on de.person_id = p.person_id
		JOIN global_temp.codesets cs on cs.codeset_id = @drugCodesetId AND de.drug_concept_id = cs.concept_id

		UNION ALL

		select de.person_id, drug_exposure_start_date, @drugExposureEndDateExpression as drug_exposure_end_date 
		FROM global_temp.drug_exposure de
		JOIN ctePersons p on de.person_id = p.person_id
		JOIN global_temp.codesets cs on cs.codeset_id = @drugCodesetId AND de.drug_source_concept_id = cs.concept_id
	) E
),

strategy_ends as (
	select et.event_id, et.person_id, ERAS.era_end_date as end_date
	from @eventTable et
	JOIN 
	(
	select ENDS.person_id, min(drug_exposure_start_date) as era_start_date, date_add(ENDS.era_end_date, @offset) as era_end_date
	from
	(
		select DE.person_id, DE.drug_exposure_start_date, MIN(E.end_date) as era_end_date
		FROM drug_target DE
		JOIN 
		(
		--cteEndDates
		select person_id, date_add(event_date, -1 * @gapDays) as end_date -- unpad the end date by @gapDays
		FROM
		(
					select person_id, event_date, event_type, 
					MAX(start_ordinal) OVER (PARTITION BY person_id ORDER BY event_date, event_type ROWS UNBOUNDED PRECEDING) AS start_ordinal,
					ROW_NUMBER() OVER (PARTITION BY person_id ORDER BY event_date, event_type) AS overall_ord -- this re-numbers the inner UNION so all rows are numbered ordered by the event date
					from
					(
						-- select the start dates, assigning a row number to each
						Select person_id, drug_exposure_start_date AS event_date, 0 as event_type, ROW_NUMBER() OVER (PARTITION BY person_id ORDER BY drug_exposure_start_date) as start_ordinal
						from drug_target D

						UNION ALL

						-- add the end dates with NULL as the row number, padding the end dates by @gapDays to allow a grace period for overlapping ranges.
						select person_id, date_add(drug_exposure_end_date, @gapDays), 1 as event_type, NULL
						FROM drug_target D
					) RAWDATA
		) E
		WHERE 2 * E.start_ordinal - E.overall_ord = 0
		) E on DE.person_id = E.person_id and E.end_date >= DE.drug_exposure_start_date
		GROUP BY DE.person_id, DE.drug_exposure_start_date
	) ENDS
	GROUP BY ENDS.person_id, ENDS.era_end_date
	) ERAS on ERAS.person_id = et.person_id 
	WHERE et.start_date between ERAS.era_start_date and ERAS.era_end_date
),
