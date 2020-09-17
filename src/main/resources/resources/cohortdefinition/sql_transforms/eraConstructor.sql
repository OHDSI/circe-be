-- era constructor
WITH cteSource AS
(
	SELECT
		@eraGroup  
		, start_date
		, end_date
		, dense_rank() over(order by @eraGroup) as groupid
	FROM collapse_input so
)
,
--------------------------------------------------------------------------------------------------------------
cteEndDates AS -- the magic
(	
	SELECT
		groupid
		, date_add(event_date, -1 * @eraconstructorpad)  as end_date
	FROM
	(
		SELECT
			groupid
			, event_date
			, event_type
			, MAX(start_ordinal) OVER (PARTITION BY groupid ORDER BY event_date, event_type ROWS UNBOUNDED PRECEDING) AS start_ordinal 
			, ROW_NUMBER() OVER (PARTITION BY groupid ORDER BY event_date, event_type) AS overall_ord
		FROM
		(

			SELECT
				groupid
				, start_date AS event_date
				, -1 AS event_type
				, ROW_NUMBER() OVER (PARTITION BY groupid ORDER BY start_date) AS start_ordinal
			FROM cteSource
		
			UNION ALL
		

			SELECT
				groupid
				, date_add(end_date, @eraconstructorpad) as end_date
				, 1 AS event_type
				, NULL
			FROM cteSource
		) RAWDATA
	) e
	WHERE (2 * e.start_ordinal) - e.overall_ord = 0
),
--------------------------------------------------------------------------------------------------------------
cteEnds AS
(
	SELECT
		 c.groupid
		, c.start_date
		, MIN(e.end_date) AS era_end_date
	FROM cteSource c
	JOIN cteEndDates e ON c.groupid = e.groupid AND e.end_date >= c.start_date
	GROUP BY
		 c.groupid
		, c.start_date
), 
collapse_output AS
(
	select @eraGroup, start_date, end_date
	from
	(
		select @eraGroup , min(b.start_date) as start_date, b.end_date
		from (
			select distinct @eraGroup, groupid from cteSource
		) a
		inner join cteEnds b on a.groupid = b.groupid
		group by @eraGroup, end_date
	) q
),