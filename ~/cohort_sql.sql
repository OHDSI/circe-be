CREATE TEMP TABLE Codesets  (codeset_id int NOT NULL,
  concept_id bigint NOT NULL
)
;

INSERT INTO Codesets (codeset_id, concept_id)
SELECT 0 as codeset_id, c.concept_id FROM (select distinct I.concept_id FROM
( 
  select concept_id from cdm.CONCEPT where 0=1
) I
) C
;

CREATE TEMP TABLE qualified_events

AS
WITH primary_events (event_id, person_id, start_date, end_date, op_start_date, op_end_date, visit_occurrence_id)  AS (
-- Begin Primary Events
select P.ordinal as event_id, P.person_id, P.start_date, P.end_date, op_start_date, op_end_date, cast(P.visit_occurrence_id as bigint) as visit_occurrence_id
FROM
(
  select E.person_id, E.start_date, E.end_date,
         row_number() OVER (PARTITION BY E.person_id ORDER BY E.sort_date ASC) ordinal,
         OP.observation_period_start_date as op_start_date, OP.observation_period_end_date as op_end_date, cast(E.visit_occurrence_id as bigint) as visit_occurrence_id
  FROM 
  (
  select PE.person_id, PE.event_id, PE.start_date, PE.end_date, PE.target_concept_id, PE.visit_occurrence_id, PE.sort_date FROM (
-- Begin Observation Period Criteria
select C.person_id, C.observation_period_id as event_id, TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') as start_date, TO_DATE(TO_CHAR(2060,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') as end_date,
       C.period_type_concept_id as TARGET_CONCEPT_ID, CAST(NULL as bigint) as visit_occurrence_id,
       C.observation_period_start_date as sort_date

from 
(
        select op.*, row_number() over (PARTITION BY op.person_id ORDER BY op.observation_period_start_date) as ordinal
        FROM cdm.OBSERVATION_PERIOD op
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
WHERE C.ordinal = 1
AND C.OBSERVATION_PERIOD_START_DATE <= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.OBSERVATION_PERIOD_END_DATE >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD')
AND C.OBSERVATION_PERIOD_START_DATE <= TO_DATE(TO_CHAR(2060,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.OBSERVATION_PERIOD_END_DATE >= TO_DATE(TO_CHAR(2060,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD')
AND (C.observation_period_end_date >= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.observation_period_end_date <= TO_DATE(TO_CHAR(2999,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.period_type_concept_id in (44814725,44787739)
AND ((CAST(C.observation_period_end_date AS DATE) - CAST(C.observation_period_start_date AS DATE)) >= 0 and (CAST(C.observation_period_end_date AS DATE) - CAST(C.observation_period_start_date AS DATE)) <= 999)
AND (EXTRACT(YEAR FROM C.observation_period_start_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.observation_period_start_date) - P.year_of_birth <= 99)
AND (EXTRACT(YEAR FROM C.observation_period_end_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.observation_period_end_date) - P.year_of_birth <= 99)
-- End Observation Period Criteria

) PE
JOIN (
-- Begin Criteria Group
select 0 as index_id, person_id, event_id
FROM
(
  select E.person_id, E.event_id 
  FROM (SELECT Q.person_id, Q.event_id, Q.start_date, Q.end_date, Q.visit_occurrence_id, OP.observation_period_start_date as op_start_date, OP.observation_period_end_date as op_end_date
FROM (-- Begin Observation Period Criteria
select C.person_id, C.observation_period_id as event_id, TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') as start_date, TO_DATE(TO_CHAR(2060,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') as end_date,
       C.period_type_concept_id as TARGET_CONCEPT_ID, CAST(NULL as bigint) as visit_occurrence_id,
       C.observation_period_start_date as sort_date

from 
(
        select op.*, row_number() over (PARTITION BY op.person_id ORDER BY op.observation_period_start_date) as ordinal
        FROM cdm.OBSERVATION_PERIOD op
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
WHERE C.ordinal = 1
AND C.OBSERVATION_PERIOD_START_DATE <= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.OBSERVATION_PERIOD_END_DATE >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD')
AND C.OBSERVATION_PERIOD_START_DATE <= TO_DATE(TO_CHAR(2060,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.OBSERVATION_PERIOD_END_DATE >= TO_DATE(TO_CHAR(2060,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD')
AND (C.observation_period_end_date >= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.observation_period_end_date <= TO_DATE(TO_CHAR(2999,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.period_type_concept_id in (44814725,44787739)
AND ((CAST(C.observation_period_end_date AS DATE) - CAST(C.observation_period_start_date AS DATE)) >= 0 and (CAST(C.observation_period_end_date AS DATE) - CAST(C.observation_period_start_date AS DATE)) <= 999)
AND (EXTRACT(YEAR FROM C.observation_period_start_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.observation_period_start_date) - P.year_of_birth <= 99)
AND (EXTRACT(YEAR FROM C.observation_period_end_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.observation_period_end_date) - P.year_of_birth <= 99)
-- End Observation Period Criteria
) Q
JOIN cdm.OBSERVATION_PERIOD OP on Q.person_id = OP.person_id 
  and OP.observation_period_start_date <= Q.start_date and OP.observation_period_end_date >= Q.start_date
) E
  INNER JOIN
  (
    -- Begin Correlated Criteria
SELECT 0 as index_id, p.person_id, p.event_id
FROM (SELECT Q.person_id, Q.event_id, Q.start_date, Q.end_date, Q.visit_occurrence_id, OP.observation_period_start_date as op_start_date, OP.observation_period_end_date as op_end_date
FROM (-- Begin Observation Period Criteria
select C.person_id, C.observation_period_id as event_id, TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') as start_date, TO_DATE(TO_CHAR(2060,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') as end_date,
       C.period_type_concept_id as TARGET_CONCEPT_ID, CAST(NULL as bigint) as visit_occurrence_id,
       C.observation_period_start_date as sort_date

from 
(
        select op.*, row_number() over (PARTITION BY op.person_id ORDER BY op.observation_period_start_date) as ordinal
        FROM cdm.OBSERVATION_PERIOD op
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
WHERE C.ordinal = 1
AND C.OBSERVATION_PERIOD_START_DATE <= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.OBSERVATION_PERIOD_END_DATE >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD')
AND C.OBSERVATION_PERIOD_START_DATE <= TO_DATE(TO_CHAR(2060,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.OBSERVATION_PERIOD_END_DATE >= TO_DATE(TO_CHAR(2060,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD')
AND (C.observation_period_end_date >= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.observation_period_end_date <= TO_DATE(TO_CHAR(2999,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.period_type_concept_id in (44814725,44787739)
AND ((CAST(C.observation_period_end_date AS DATE) - CAST(C.observation_period_start_date AS DATE)) >= 0 and (CAST(C.observation_period_end_date AS DATE) - CAST(C.observation_period_start_date AS DATE)) <= 999)
AND (EXTRACT(YEAR FROM C.observation_period_start_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.observation_period_start_date) - P.year_of_birth <= 99)
AND (EXTRACT(YEAR FROM C.observation_period_end_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.observation_period_end_date) - P.year_of_birth <= 99)
-- End Observation Period Criteria
) Q
JOIN cdm.OBSERVATION_PERIOD OP on Q.person_id = OP.person_id 
  and OP.observation_period_start_date <= Q.start_date and OP.observation_period_end_date >= Q.start_date
) P
INNER JOIN
(
  -- Begin Condition Occurrence Criteria
SELECT C.person_id, C.condition_occurrence_id as event_id, C.condition_start_date as start_date, COALESCE(C.condition_end_date, (C.condition_start_date + 1*INTERVAL'1 day')) as end_date,
       C.CONDITION_CONCEPT_ID as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.condition_start_date as sort_date
FROM 
(
  SELECT co.* 
  FROM cdm.CONDITION_OCCURRENCE co
  
) C


-- End Condition Occurrence Criteria

) A on A.person_id = P.person_id  AND A.visit_occurrence_id = P.visit_occurrence_id
GROUP BY p.person_id, p.event_id
HAVING COUNT(A.TARGET_CONCEPT_ID) >= 1
-- End Correlated Criteria

  ) CQ on E.person_id = CQ.person_id and E.event_id = CQ.event_id
  GROUP BY E.person_id, E.event_id
  HAVING COUNT(index_id) = 1
) G
-- End Criteria Group
) AC on AC.person_id = pe.person_id and AC.event_id = pe.event_id

  ) E
	JOIN cdm.observation_period OP on E.person_id = OP.person_id and E.start_date >=  OP.observation_period_start_date and E.start_date <= op.observation_period_end_date
  WHERE (OP.OBSERVATION_PERIOD_START_DATE + 0*INTERVAL'1 day') <= E.START_DATE AND (E.START_DATE + 0*INTERVAL'1 day') <= OP.OBSERVATION_PERIOD_END_DATE
) P
WHERE P.ordinal = 1
-- End Primary Events

)
 SELECT
event_id, person_id, start_date, end_date, op_start_date, op_end_date, visit_occurrence_id

FROM
(
  select pe.event_id, pe.person_id, pe.start_date, pe.end_date, pe.op_start_date, pe.op_end_date, row_number() over (partition by pe.person_id order by pe.start_date ASC) as ordinal, cast(pe.visit_occurrence_id as bigint) as visit_occurrence_id
  FROM primary_events pe
  
) QE

;
ANALYZE qualified_events
;

--- Inclusion Rule Inserts

CREATE TEMP TABLE Inclusion_0

AS
SELECT
0 as inclusion_rule_id, person_id, event_id

FROM
(
  select pe.person_id, pe.event_id
  FROM qualified_events pe
  
JOIN (
-- Begin Criteria Group
select 0 as index_id, person_id, event_id
FROM
(
  select E.person_id, E.event_id 
  FROM qualified_events E
  INNER JOIN
  (
    -- Begin Demographic Criteria
SELECT 0 as index_id, e.person_id, e.event_id
FROM qualified_events E
JOIN cdm.PERSON P ON P.PERSON_ID = E.PERSON_ID
WHERE (EXTRACT(YEAR FROM E.start_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM E.start_date) - P.year_of_birth <= 99) AND P.gender_concept_id in (8507) AND P.race_concept_id in (8515,38003578) AND P.race_concept_id in (8515,38003578) AND P.ethnicity_concept_id in (38003564) AND (E.start_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and E.start_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD')) AND (E.end_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and E.end_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
GROUP BY e.person_id, e.event_id
-- End Demographic Criteria

  ) CQ on E.person_id = CQ.person_id and E.event_id = CQ.event_id
  GROUP BY E.person_id, E.event_id
  HAVING COUNT(index_id) = 1
) G
-- End Criteria Group
) AC on AC.person_id = pe.person_id AND AC.event_id = pe.event_id
) Results
;
ANALYZE Inclusion_0
;

CREATE TEMP TABLE Inclusion_1

AS
SELECT
1 as inclusion_rule_id, person_id, event_id

FROM
(
  select pe.person_id, pe.event_id
  FROM qualified_events pe
  
JOIN (
-- Begin Criteria Group
select 0 as index_id, person_id, event_id
FROM
(
  select E.person_id, E.event_id 
  FROM qualified_events E
  INNER JOIN
  (
    -- Begin Correlated Criteria
SELECT 0 as index_id, p.person_id, p.event_id
FROM qualified_events P
INNER JOIN
(
  select PE.person_id, PE.event_id, PE.start_date, PE.end_date, PE.target_concept_id, PE.visit_occurrence_id, PE.sort_date FROM (
-- Begin Condition Occurrence Criteria
SELECT C.person_id, C.condition_occurrence_id as event_id, C.condition_start_date as start_date, COALESCE(C.condition_end_date, (C.condition_start_date + 1*INTERVAL'1 day')) as end_date,
       C.CONDITION_CONCEPT_ID as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.condition_start_date as sort_date
FROM 
(
  SELECT co.* , row_number() over (PARTITION BY co.person_id ORDER BY co.condition_start_date, co.condition_occurrence_id) as ordinal
  FROM cdm.CONDITION_OCCURRENCE co
  JOIN Codesets codesets on ((co.condition_concept_id = codesets.concept_id and codesets.codeset_id = 0) AND (co.condition_source_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
JOIN cdm.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id
LEFT JOIN cdm.PROVIDER PR on C.provider_id = PR.provider_id
WHERE (C.condition_start_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.condition_start_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND (C.condition_end_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.condition_end_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.condition_type_concept_id  in (38000199)
AND C.stop_reason  like '%this is some test text%'
AND (EXTRACT(YEAR FROM C.condition_start_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.condition_start_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8507)
AND PR.specialty_concept_id in (45444755,45418395)
AND V.visit_concept_id in (9201)
AND C.ordinal = 1
-- End Condition Occurrence Criteria

) PE
JOIN (
-- Begin Criteria Group
select 0 as index_id, person_id, event_id
FROM
(
  select E.person_id, E.event_id 
  FROM (SELECT Q.person_id, Q.event_id, Q.start_date, Q.end_date, Q.visit_occurrence_id, OP.observation_period_start_date as op_start_date, OP.observation_period_end_date as op_end_date
FROM (-- Begin Condition Occurrence Criteria
SELECT C.person_id, C.condition_occurrence_id as event_id, C.condition_start_date as start_date, COALESCE(C.condition_end_date, (C.condition_start_date + 1*INTERVAL'1 day')) as end_date,
       C.CONDITION_CONCEPT_ID as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.condition_start_date as sort_date
FROM 
(
  SELECT co.* , row_number() over (PARTITION BY co.person_id ORDER BY co.condition_start_date, co.condition_occurrence_id) as ordinal
  FROM cdm.CONDITION_OCCURRENCE co
  JOIN Codesets codesets on ((co.condition_concept_id = codesets.concept_id and codesets.codeset_id = 0) AND (co.condition_source_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
JOIN cdm.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id
LEFT JOIN cdm.PROVIDER PR on C.provider_id = PR.provider_id
WHERE (C.condition_start_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.condition_start_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND (C.condition_end_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.condition_end_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.condition_type_concept_id  in (38000199)
AND C.stop_reason  like '%this is some test text%'
AND (EXTRACT(YEAR FROM C.condition_start_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.condition_start_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8507)
AND PR.specialty_concept_id in (45444755,45418395)
AND V.visit_concept_id in (9201)
AND C.ordinal = 1
-- End Condition Occurrence Criteria
) Q
JOIN cdm.OBSERVATION_PERIOD OP on Q.person_id = OP.person_id 
  and OP.observation_period_start_date <= Q.start_date and OP.observation_period_end_date >= Q.start_date
) E
  INNER JOIN
  (
    -- Begin Correlated Criteria
SELECT 0 as index_id, p.person_id, p.event_id
FROM (SELECT Q.person_id, Q.event_id, Q.start_date, Q.end_date, Q.visit_occurrence_id, OP.observation_period_start_date as op_start_date, OP.observation_period_end_date as op_end_date
FROM (-- Begin Condition Occurrence Criteria
SELECT C.person_id, C.condition_occurrence_id as event_id, C.condition_start_date as start_date, COALESCE(C.condition_end_date, (C.condition_start_date + 1*INTERVAL'1 day')) as end_date,
       C.CONDITION_CONCEPT_ID as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.condition_start_date as sort_date
FROM 
(
  SELECT co.* , row_number() over (PARTITION BY co.person_id ORDER BY co.condition_start_date, co.condition_occurrence_id) as ordinal
  FROM cdm.CONDITION_OCCURRENCE co
  JOIN Codesets codesets on ((co.condition_concept_id = codesets.concept_id and codesets.codeset_id = 0) AND (co.condition_source_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
JOIN cdm.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id
LEFT JOIN cdm.PROVIDER PR on C.provider_id = PR.provider_id
WHERE (C.condition_start_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.condition_start_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND (C.condition_end_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.condition_end_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.condition_type_concept_id  in (38000199)
AND C.stop_reason  like '%this is some test text%'
AND (EXTRACT(YEAR FROM C.condition_start_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.condition_start_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8507)
AND PR.specialty_concept_id in (45444755,45418395)
AND V.visit_concept_id in (9201)
AND C.ordinal = 1
-- End Condition Occurrence Criteria
) Q
JOIN cdm.OBSERVATION_PERIOD OP on Q.person_id = OP.person_id 
  and OP.observation_period_start_date <= Q.start_date and OP.observation_period_end_date >= Q.start_date
) P
INNER JOIN
(
  -- Begin Condition Occurrence Criteria
SELECT C.person_id, C.condition_occurrence_id as event_id, C.condition_start_date as start_date, COALESCE(C.condition_end_date, (C.condition_start_date + 1*INTERVAL'1 day')) as end_date,
       C.CONDITION_CONCEPT_ID as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.condition_start_date as sort_date
FROM 
(
  SELECT co.* 
  FROM cdm.CONDITION_OCCURRENCE co
  
) C


-- End Condition Occurrence Criteria

) A on A.person_id = P.person_id  AND A.visit_occurrence_id = P.visit_occurrence_id
GROUP BY p.person_id, p.event_id
HAVING COUNT(A.TARGET_CONCEPT_ID) >= 1
-- End Correlated Criteria

  ) CQ on E.person_id = CQ.person_id and E.event_id = CQ.event_id
  GROUP BY E.person_id, E.event_id
  HAVING COUNT(index_id) = 1
) G
-- End Criteria Group
) AC on AC.person_id = pe.person_id and AC.event_id = pe.event_id

) A on A.person_id = P.person_id  AND A.START_DATE >= P.OP_START_DATE AND A.START_DATE <= P.OP_END_DATE AND A.START_DATE >= (P.START_DATE + -30*INTERVAL'1 day') AND A.START_DATE <= (P.START_DATE + 30*INTERVAL'1 day')
GROUP BY p.person_id, p.event_id
HAVING COUNT(A.TARGET_CONCEPT_ID) >= 1
-- End Correlated Criteria

  ) CQ on E.person_id = CQ.person_id and E.event_id = CQ.event_id
  GROUP BY E.person_id, E.event_id
  HAVING COUNT(index_id) = 1
) G
-- End Criteria Group
) AC on AC.person_id = pe.person_id AND AC.event_id = pe.event_id
) Results
;
ANALYZE Inclusion_1
;

CREATE TEMP TABLE Inclusion_2

AS
SELECT
2 as inclusion_rule_id, person_id, event_id

FROM
(
  select pe.person_id, pe.event_id
  FROM qualified_events pe
  
JOIN (
-- Begin Criteria Group
select 0 as index_id, person_id, event_id
FROM
(
  select E.person_id, E.event_id 
  FROM qualified_events E
  INNER JOIN
  (
    -- Begin Correlated Criteria
SELECT 0 as index_id, p.person_id, p.event_id
FROM qualified_events P
INNER JOIN
(
  select PE.person_id, PE.event_id, PE.start_date, PE.end_date, PE.target_concept_id, PE.visit_occurrence_id, PE.sort_date FROM (
-- Begin Condition Era Criteria
select C.person_id, C.condition_era_id as event_id, C.condition_era_start_date as start_date,
       C.condition_era_end_date as end_date, C.CONDITION_CONCEPT_ID as TARGET_CONCEPT_ID, CAST(NULL as bigint) as visit_occurrence_id,
       C.condition_era_start_date as sort_date
from 
(
  select ce.* , row_number() over (PARTITION BY ce.person_id ORDER BY ce.condition_era_start_date, ce.condition_era_id) as ordinal
  FROM cdm.CONDITION_ERA ce
where ce.condition_concept_id in (SELECT concept_id from  Codesets where codeset_id = 0)
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
WHERE (C.condition_era_start_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.condition_era_start_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND (C.condition_era_end_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.condition_era_end_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND (C.condition_occurrence_count >= 0 and C.condition_occurrence_count <= 99)
AND ((CAST(C.condition_era_end_date AS DATE) - CAST(C.condition_era_start_date AS DATE)) >= 0 and (CAST(C.condition_era_end_date AS DATE) - CAST(C.condition_era_start_date AS DATE)) <= 99)
AND (EXTRACT(YEAR FROM C.condition_era_start_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.condition_era_start_date) - P.year_of_birth <= 99)
AND (EXTRACT(YEAR FROM C.condition_era_end_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.condition_era_end_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8507)
AND C.ordinal = 1
-- End Condition Era Criteria

) PE
JOIN (
-- Begin Criteria Group
select 0 as index_id, person_id, event_id
FROM
(
  select E.person_id, E.event_id 
  FROM (SELECT Q.person_id, Q.event_id, Q.start_date, Q.end_date, Q.visit_occurrence_id, OP.observation_period_start_date as op_start_date, OP.observation_period_end_date as op_end_date
FROM (-- Begin Condition Era Criteria
select C.person_id, C.condition_era_id as event_id, C.condition_era_start_date as start_date,
       C.condition_era_end_date as end_date, C.CONDITION_CONCEPT_ID as TARGET_CONCEPT_ID, CAST(NULL as bigint) as visit_occurrence_id,
       C.condition_era_start_date as sort_date
from 
(
  select ce.* , row_number() over (PARTITION BY ce.person_id ORDER BY ce.condition_era_start_date, ce.condition_era_id) as ordinal
  FROM cdm.CONDITION_ERA ce
where ce.condition_concept_id in (SELECT concept_id from  Codesets where codeset_id = 0)
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
WHERE (C.condition_era_start_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.condition_era_start_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND (C.condition_era_end_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.condition_era_end_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND (C.condition_occurrence_count >= 0 and C.condition_occurrence_count <= 99)
AND ((CAST(C.condition_era_end_date AS DATE) - CAST(C.condition_era_start_date AS DATE)) >= 0 and (CAST(C.condition_era_end_date AS DATE) - CAST(C.condition_era_start_date AS DATE)) <= 99)
AND (EXTRACT(YEAR FROM C.condition_era_start_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.condition_era_start_date) - P.year_of_birth <= 99)
AND (EXTRACT(YEAR FROM C.condition_era_end_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.condition_era_end_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8507)
AND C.ordinal = 1
-- End Condition Era Criteria
) Q
JOIN cdm.OBSERVATION_PERIOD OP on Q.person_id = OP.person_id 
  and OP.observation_period_start_date <= Q.start_date and OP.observation_period_end_date >= Q.start_date
) E
  INNER JOIN
  (
    -- Begin Correlated Criteria
SELECT 0 as index_id, p.person_id, p.event_id
FROM (SELECT Q.person_id, Q.event_id, Q.start_date, Q.end_date, Q.visit_occurrence_id, OP.observation_period_start_date as op_start_date, OP.observation_period_end_date as op_end_date
FROM (-- Begin Condition Era Criteria
select C.person_id, C.condition_era_id as event_id, C.condition_era_start_date as start_date,
       C.condition_era_end_date as end_date, C.CONDITION_CONCEPT_ID as TARGET_CONCEPT_ID, CAST(NULL as bigint) as visit_occurrence_id,
       C.condition_era_start_date as sort_date
from 
(
  select ce.* , row_number() over (PARTITION BY ce.person_id ORDER BY ce.condition_era_start_date, ce.condition_era_id) as ordinal
  FROM cdm.CONDITION_ERA ce
where ce.condition_concept_id in (SELECT concept_id from  Codesets where codeset_id = 0)
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
WHERE (C.condition_era_start_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.condition_era_start_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND (C.condition_era_end_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.condition_era_end_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND (C.condition_occurrence_count >= 0 and C.condition_occurrence_count <= 99)
AND ((CAST(C.condition_era_end_date AS DATE) - CAST(C.condition_era_start_date AS DATE)) >= 0 and (CAST(C.condition_era_end_date AS DATE) - CAST(C.condition_era_start_date AS DATE)) <= 99)
AND (EXTRACT(YEAR FROM C.condition_era_start_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.condition_era_start_date) - P.year_of_birth <= 99)
AND (EXTRACT(YEAR FROM C.condition_era_end_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.condition_era_end_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8507)
AND C.ordinal = 1
-- End Condition Era Criteria
) Q
JOIN cdm.OBSERVATION_PERIOD OP on Q.person_id = OP.person_id 
  and OP.observation_period_start_date <= Q.start_date and OP.observation_period_end_date >= Q.start_date
) P
INNER JOIN
(
  -- Begin Condition Era Criteria
select C.person_id, C.condition_era_id as event_id, C.condition_era_start_date as start_date,
       C.condition_era_end_date as end_date, C.CONDITION_CONCEPT_ID as TARGET_CONCEPT_ID, CAST(NULL as bigint) as visit_occurrence_id,
       C.condition_era_start_date as sort_date
from 
(
  select ce.* 
  FROM cdm.CONDITION_ERA ce

) C


-- End Condition Era Criteria

) A on A.person_id = P.person_id 
GROUP BY p.person_id, p.event_id
HAVING COUNT(A.TARGET_CONCEPT_ID) >= 1
-- End Correlated Criteria

  ) CQ on E.person_id = CQ.person_id and E.event_id = CQ.event_id
  GROUP BY E.person_id, E.event_id
  HAVING COUNT(index_id) = 1
) G
-- End Criteria Group
) AC on AC.person_id = pe.person_id and AC.event_id = pe.event_id

) A on A.person_id = P.person_id  AND A.START_DATE >= P.OP_START_DATE AND A.START_DATE <= P.OP_END_DATE AND A.START_DATE >= (P.START_DATE + -30*INTERVAL'1 day') AND A.START_DATE <= (P.START_DATE + 30*INTERVAL'1 day')
GROUP BY p.person_id, p.event_id
HAVING COUNT(A.TARGET_CONCEPT_ID) >= 1
-- End Correlated Criteria

  ) CQ on E.person_id = CQ.person_id and E.event_id = CQ.event_id
  GROUP BY E.person_id, E.event_id
  HAVING COUNT(index_id) = 1
) G
-- End Criteria Group
) AC on AC.person_id = pe.person_id AND AC.event_id = pe.event_id
) Results
;
ANALYZE Inclusion_2
;

CREATE TEMP TABLE Inclusion_3

AS
SELECT
3 as inclusion_rule_id, person_id, event_id

FROM
(
  select pe.person_id, pe.event_id
  FROM qualified_events pe
  
JOIN (
-- Begin Criteria Group
select 0 as index_id, person_id, event_id
FROM
(
  select E.person_id, E.event_id 
  FROM qualified_events E
  INNER JOIN
  (
    -- Begin Correlated Criteria
SELECT 0 as index_id, p.person_id, p.event_id
FROM qualified_events P
INNER JOIN
(
  select PE.person_id, PE.event_id, PE.start_date, PE.end_date, PE.target_concept_id, PE.visit_occurrence_id, PE.sort_date FROM (
-- Begin Death Criteria
select C.person_id, C.person_id as event_id, C.death_date as start_date, (C.death_date + 1*INTERVAL'1 day') as end_date,
       coalesce(C.cause_concept_id,0) as TARGET_CONCEPT_ID, CAST(NULL as bigint) as visit_occurrence_id,
       C.death_date as sort_date
from 
(
  select d.*
  FROM cdm.DEATH d
JOIN Codesets codesets on ((d.cause_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
WHERE (C.death_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.death_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.death_type_concept_id  in (256,255,254)
AND (EXTRACT(YEAR FROM C.death_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.death_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8532,8507)
-- End Death Criteria


) PE
JOIN (
-- Begin Criteria Group
select 0 as index_id, person_id, event_id
FROM
(
  select E.person_id, E.event_id 
  FROM (SELECT Q.person_id, Q.event_id, Q.start_date, Q.end_date, Q.visit_occurrence_id, OP.observation_period_start_date as op_start_date, OP.observation_period_end_date as op_end_date
FROM (-- Begin Death Criteria
select C.person_id, C.person_id as event_id, C.death_date as start_date, (C.death_date + 1*INTERVAL'1 day') as end_date,
       coalesce(C.cause_concept_id,0) as TARGET_CONCEPT_ID, CAST(NULL as bigint) as visit_occurrence_id,
       C.death_date as sort_date
from 
(
  select d.*
  FROM cdm.DEATH d
JOIN Codesets codesets on ((d.cause_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
WHERE (C.death_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.death_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.death_type_concept_id  in (256,255,254)
AND (EXTRACT(YEAR FROM C.death_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.death_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8532,8507)
-- End Death Criteria

) Q
JOIN cdm.OBSERVATION_PERIOD OP on Q.person_id = OP.person_id 
  and OP.observation_period_start_date <= Q.start_date and OP.observation_period_end_date >= Q.start_date
) E
  INNER JOIN
  (
    -- Begin Correlated Criteria
SELECT 0 as index_id, p.person_id, p.event_id
FROM (SELECT Q.person_id, Q.event_id, Q.start_date, Q.end_date, Q.visit_occurrence_id, OP.observation_period_start_date as op_start_date, OP.observation_period_end_date as op_end_date
FROM (-- Begin Death Criteria
select C.person_id, C.person_id as event_id, C.death_date as start_date, (C.death_date + 1*INTERVAL'1 day') as end_date,
       coalesce(C.cause_concept_id,0) as TARGET_CONCEPT_ID, CAST(NULL as bigint) as visit_occurrence_id,
       C.death_date as sort_date
from 
(
  select d.*
  FROM cdm.DEATH d
JOIN Codesets codesets on ((d.cause_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
WHERE (C.death_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.death_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.death_type_concept_id  in (256,255,254)
AND (EXTRACT(YEAR FROM C.death_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.death_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8532,8507)
-- End Death Criteria

) Q
JOIN cdm.OBSERVATION_PERIOD OP on Q.person_id = OP.person_id 
  and OP.observation_period_start_date <= Q.start_date and OP.observation_period_end_date >= Q.start_date
) P
INNER JOIN
(
  -- Begin Death Criteria
select C.person_id, C.person_id as event_id, C.death_date as start_date, (C.death_date + 1*INTERVAL'1 day') as end_date,
       coalesce(C.cause_concept_id,0) as TARGET_CONCEPT_ID, CAST(NULL as bigint) as visit_occurrence_id,
       C.death_date as sort_date
from 
(
  select d.*
  FROM cdm.DEATH d
JOIN Codesets codesets on ((d.cause_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C


-- End Death Criteria


) A on A.person_id = P.person_id 
GROUP BY p.person_id, p.event_id
HAVING COUNT(A.TARGET_CONCEPT_ID) >= 1
-- End Correlated Criteria

  ) CQ on E.person_id = CQ.person_id and E.event_id = CQ.event_id
  GROUP BY E.person_id, E.event_id
  HAVING COUNT(index_id) = 1
) G
-- End Criteria Group
) AC on AC.person_id = pe.person_id and AC.event_id = pe.event_id

) A on A.person_id = P.person_id  AND A.START_DATE >= P.OP_START_DATE AND A.START_DATE <= P.OP_END_DATE AND A.START_DATE >= (P.START_DATE + -30*INTERVAL'1 day') AND A.START_DATE <= (P.START_DATE + 30*INTERVAL'1 day')
GROUP BY p.person_id, p.event_id
HAVING COUNT(A.TARGET_CONCEPT_ID) >= 1
-- End Correlated Criteria

  ) CQ on E.person_id = CQ.person_id and E.event_id = CQ.event_id
  GROUP BY E.person_id, E.event_id
  HAVING COUNT(index_id) = 1
) G
-- End Criteria Group
) AC on AC.person_id = pe.person_id AND AC.event_id = pe.event_id
) Results
;
ANALYZE Inclusion_3
;

CREATE TEMP TABLE Inclusion_4

AS
SELECT
4 as inclusion_rule_id, person_id, event_id

FROM
(
  select pe.person_id, pe.event_id
  FROM qualified_events pe
  
JOIN (
-- Begin Criteria Group
select 0 as index_id, person_id, event_id
FROM
(
  select E.person_id, E.event_id 
  FROM qualified_events E
  INNER JOIN
  (
    -- Begin Correlated Criteria
SELECT 0 as index_id, p.person_id, p.event_id
FROM qualified_events P
INNER JOIN
(
  select PE.person_id, PE.event_id, PE.start_date, PE.end_date, PE.target_concept_id, PE.visit_occurrence_id, PE.sort_date FROM (
-- Begin Device Exposure Criteria
select C.person_id, C.device_exposure_id as event_id, C.device_exposure_start_date as start_date, C.device_exposure_end_date as end_date,
       C.device_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.device_exposure_start_date as sort_date
from 
(
  select de.* , row_number() over (PARTITION BY de.person_id ORDER BY de.device_exposure_start_date, de.device_exposure_id) as ordinal
  FROM cdm.DEVICE_EXPOSURE de
JOIN Codesets codesets on ((de.device_concept_id = codesets.concept_id and codesets.codeset_id = 0) AND (de.device_source_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
JOIN cdm.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id
LEFT JOIN cdm.PROVIDER PR on C.provider_id = PR.provider_id
WHERE (C.device_exposure_start_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.device_exposure_start_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND (C.device_exposure_end_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.device_exposure_end_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.device_type_concept_id  in (32465,44818706)
AND C.unique_device_id  like '%null%'
AND (C.quantity >= 0 and C.quantity <= 99)
AND (EXTRACT(YEAR FROM C.device_exposure_start_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.device_exposure_start_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8532,8507)
AND PR.specialty_concept_id in (38004455,38003625,38003626)
AND V.visit_concept_id in (8717,8756)
AND C.ordinal = 1
-- End Device Exposure Criteria

) PE
JOIN (
-- Begin Criteria Group
select 0 as index_id, person_id, event_id
FROM
(
  select E.person_id, E.event_id 
  FROM (SELECT Q.person_id, Q.event_id, Q.start_date, Q.end_date, Q.visit_occurrence_id, OP.observation_period_start_date as op_start_date, OP.observation_period_end_date as op_end_date
FROM (-- Begin Device Exposure Criteria
select C.person_id, C.device_exposure_id as event_id, C.device_exposure_start_date as start_date, C.device_exposure_end_date as end_date,
       C.device_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.device_exposure_start_date as sort_date
from 
(
  select de.* , row_number() over (PARTITION BY de.person_id ORDER BY de.device_exposure_start_date, de.device_exposure_id) as ordinal
  FROM cdm.DEVICE_EXPOSURE de
JOIN Codesets codesets on ((de.device_concept_id = codesets.concept_id and codesets.codeset_id = 0) AND (de.device_source_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
JOIN cdm.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id
LEFT JOIN cdm.PROVIDER PR on C.provider_id = PR.provider_id
WHERE (C.device_exposure_start_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.device_exposure_start_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND (C.device_exposure_end_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.device_exposure_end_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.device_type_concept_id  in (32465,44818706)
AND C.unique_device_id  like '%null%'
AND (C.quantity >= 0 and C.quantity <= 99)
AND (EXTRACT(YEAR FROM C.device_exposure_start_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.device_exposure_start_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8532,8507)
AND PR.specialty_concept_id in (38004455,38003625,38003626)
AND V.visit_concept_id in (8717,8756)
AND C.ordinal = 1
-- End Device Exposure Criteria
) Q
JOIN cdm.OBSERVATION_PERIOD OP on Q.person_id = OP.person_id 
  and OP.observation_period_start_date <= Q.start_date and OP.observation_period_end_date >= Q.start_date
) E
  INNER JOIN
  (
    -- Begin Correlated Criteria
SELECT 0 as index_id, p.person_id, p.event_id
FROM (SELECT Q.person_id, Q.event_id, Q.start_date, Q.end_date, Q.visit_occurrence_id, OP.observation_period_start_date as op_start_date, OP.observation_period_end_date as op_end_date
FROM (-- Begin Device Exposure Criteria
select C.person_id, C.device_exposure_id as event_id, C.device_exposure_start_date as start_date, C.device_exposure_end_date as end_date,
       C.device_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.device_exposure_start_date as sort_date
from 
(
  select de.* , row_number() over (PARTITION BY de.person_id ORDER BY de.device_exposure_start_date, de.device_exposure_id) as ordinal
  FROM cdm.DEVICE_EXPOSURE de
JOIN Codesets codesets on ((de.device_concept_id = codesets.concept_id and codesets.codeset_id = 0) AND (de.device_source_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
JOIN cdm.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id
LEFT JOIN cdm.PROVIDER PR on C.provider_id = PR.provider_id
WHERE (C.device_exposure_start_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.device_exposure_start_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND (C.device_exposure_end_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.device_exposure_end_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.device_type_concept_id  in (32465,44818706)
AND C.unique_device_id  like '%null%'
AND (C.quantity >= 0 and C.quantity <= 99)
AND (EXTRACT(YEAR FROM C.device_exposure_start_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.device_exposure_start_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8532,8507)
AND PR.specialty_concept_id in (38004455,38003625,38003626)
AND V.visit_concept_id in (8717,8756)
AND C.ordinal = 1
-- End Device Exposure Criteria
) Q
JOIN cdm.OBSERVATION_PERIOD OP on Q.person_id = OP.person_id 
  and OP.observation_period_start_date <= Q.start_date and OP.observation_period_end_date >= Q.start_date
) P
INNER JOIN
(
  -- Begin Device Exposure Criteria
select C.person_id, C.device_exposure_id as event_id, C.device_exposure_start_date as start_date, C.device_exposure_end_date as end_date,
       C.device_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.device_exposure_start_date as sort_date
from 
(
  select de.* 
  FROM cdm.DEVICE_EXPOSURE de
JOIN Codesets codesets on ((de.device_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C


-- End Device Exposure Criteria

) A on A.person_id = P.person_id  AND A.visit_occurrence_id = P.visit_occurrence_id
GROUP BY p.person_id, p.event_id
HAVING COUNT(A.TARGET_CONCEPT_ID) >= 1
-- End Correlated Criteria

  ) CQ on E.person_id = CQ.person_id and E.event_id = CQ.event_id
  GROUP BY E.person_id, E.event_id
  HAVING COUNT(index_id) = 1
) G
-- End Criteria Group
) AC on AC.person_id = pe.person_id and AC.event_id = pe.event_id

) A on A.person_id = P.person_id  AND A.START_DATE >= P.OP_START_DATE AND A.START_DATE <= P.OP_END_DATE AND A.START_DATE >= (P.START_DATE + -30*INTERVAL'1 day') AND A.START_DATE <= (P.START_DATE + 30*INTERVAL'1 day')
GROUP BY p.person_id, p.event_id
HAVING COUNT(A.TARGET_CONCEPT_ID) >= 1
-- End Correlated Criteria

  ) CQ on E.person_id = CQ.person_id and E.event_id = CQ.event_id
  GROUP BY E.person_id, E.event_id
  HAVING COUNT(index_id) = 1
) G
-- End Criteria Group
) AC on AC.person_id = pe.person_id AND AC.event_id = pe.event_id
) Results
;
ANALYZE Inclusion_4
;

CREATE TEMP TABLE Inclusion_5

AS
SELECT
5 as inclusion_rule_id, person_id, event_id

FROM
(
  select pe.person_id, pe.event_id
  FROM qualified_events pe
  
JOIN (
-- Begin Criteria Group
select 0 as index_id, person_id, event_id
FROM
(
  select E.person_id, E.event_id 
  FROM qualified_events E
  INNER JOIN
  (
    -- Begin Correlated Criteria
SELECT 0 as index_id, p.person_id, p.event_id
FROM qualified_events P
INNER JOIN
(
  select PE.person_id, PE.event_id, PE.start_date, PE.end_date, PE.target_concept_id, PE.visit_occurrence_id, PE.sort_date FROM (
-- Begin Drug Era Criteria
select C.person_id, C.drug_era_id as event_id, C.drug_era_start_date as start_date, C.drug_era_end_date as end_date,
       C.drug_concept_id as TARGET_CONCEPT_ID, CAST(NULL as bigint) as visit_occurrence_id,
       C.drug_era_start_date as sort_date
from 
(
  select de.* , row_number() over (PARTITION BY de.person_id ORDER BY de.drug_era_start_date, de.drug_era_id) as ordinal
  FROM cdm.DRUG_ERA de

) C
JOIN cdm.PERSON P on C.person_id = P.person_id
WHERE (C.drug_era_start_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.drug_era_start_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND (C.drug_era_end_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.drug_era_end_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND (C.drug_exposure_count >= 0 and C.drug_exposure_count <= 99)
AND ((CAST(C.drug_era_end_date AS DATE) - CAST(C.drug_era_start_date AS DATE)) >= 0 and (CAST(C.drug_era_end_date AS DATE) - CAST(C.drug_era_start_date AS DATE)) <= 99)
AND (EXTRACT(YEAR FROM C.drug_era_start_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.drug_era_start_date) - P.year_of_birth <= 99)
AND (EXTRACT(YEAR FROM C.drug_era_end_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.drug_era_end_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8532,8507)
AND C.ordinal = 1
-- End Drug Era Criteria

) PE
JOIN (
-- Begin Criteria Group
select 0 as index_id, person_id, event_id
FROM
(
  select E.person_id, E.event_id 
  FROM (SELECT Q.person_id, Q.event_id, Q.start_date, Q.end_date, Q.visit_occurrence_id, OP.observation_period_start_date as op_start_date, OP.observation_period_end_date as op_end_date
FROM (-- Begin Drug Era Criteria
select C.person_id, C.drug_era_id as event_id, C.drug_era_start_date as start_date, C.drug_era_end_date as end_date,
       C.drug_concept_id as TARGET_CONCEPT_ID, CAST(NULL as bigint) as visit_occurrence_id,
       C.drug_era_start_date as sort_date
from 
(
  select de.* , row_number() over (PARTITION BY de.person_id ORDER BY de.drug_era_start_date, de.drug_era_id) as ordinal
  FROM cdm.DRUG_ERA de

) C
JOIN cdm.PERSON P on C.person_id = P.person_id
WHERE (C.drug_era_start_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.drug_era_start_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND (C.drug_era_end_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.drug_era_end_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND (C.drug_exposure_count >= 0 and C.drug_exposure_count <= 99)
AND ((CAST(C.drug_era_end_date AS DATE) - CAST(C.drug_era_start_date AS DATE)) >= 0 and (CAST(C.drug_era_end_date AS DATE) - CAST(C.drug_era_start_date AS DATE)) <= 99)
AND (EXTRACT(YEAR FROM C.drug_era_start_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.drug_era_start_date) - P.year_of_birth <= 99)
AND (EXTRACT(YEAR FROM C.drug_era_end_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.drug_era_end_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8532,8507)
AND C.ordinal = 1
-- End Drug Era Criteria
) Q
JOIN cdm.OBSERVATION_PERIOD OP on Q.person_id = OP.person_id 
  and OP.observation_period_start_date <= Q.start_date and OP.observation_period_end_date >= Q.start_date
) E
  INNER JOIN
  (
    -- Begin Correlated Criteria
SELECT 0 as index_id, p.person_id, p.event_id
FROM (SELECT Q.person_id, Q.event_id, Q.start_date, Q.end_date, Q.visit_occurrence_id, OP.observation_period_start_date as op_start_date, OP.observation_period_end_date as op_end_date
FROM (-- Begin Drug Era Criteria
select C.person_id, C.drug_era_id as event_id, C.drug_era_start_date as start_date, C.drug_era_end_date as end_date,
       C.drug_concept_id as TARGET_CONCEPT_ID, CAST(NULL as bigint) as visit_occurrence_id,
       C.drug_era_start_date as sort_date
from 
(
  select de.* , row_number() over (PARTITION BY de.person_id ORDER BY de.drug_era_start_date, de.drug_era_id) as ordinal
  FROM cdm.DRUG_ERA de

) C
JOIN cdm.PERSON P on C.person_id = P.person_id
WHERE (C.drug_era_start_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.drug_era_start_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND (C.drug_era_end_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.drug_era_end_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND (C.drug_exposure_count >= 0 and C.drug_exposure_count <= 99)
AND ((CAST(C.drug_era_end_date AS DATE) - CAST(C.drug_era_start_date AS DATE)) >= 0 and (CAST(C.drug_era_end_date AS DATE) - CAST(C.drug_era_start_date AS DATE)) <= 99)
AND (EXTRACT(YEAR FROM C.drug_era_start_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.drug_era_start_date) - P.year_of_birth <= 99)
AND (EXTRACT(YEAR FROM C.drug_era_end_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.drug_era_end_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8532,8507)
AND C.ordinal = 1
-- End Drug Era Criteria
) Q
JOIN cdm.OBSERVATION_PERIOD OP on Q.person_id = OP.person_id 
  and OP.observation_period_start_date <= Q.start_date and OP.observation_period_end_date >= Q.start_date
) P
INNER JOIN
(
  -- Begin Drug Era Criteria
select C.person_id, C.drug_era_id as event_id, C.drug_era_start_date as start_date, C.drug_era_end_date as end_date,
       C.drug_concept_id as TARGET_CONCEPT_ID, CAST(NULL as bigint) as visit_occurrence_id,
       C.drug_era_start_date as sort_date
from 
(
  select de.* 
  FROM cdm.DRUG_ERA de
where de.drug_concept_id in (SELECT concept_id from  Codesets where codeset_id = 0)
) C


-- End Drug Era Criteria

) A on A.person_id = P.person_id 
GROUP BY p.person_id, p.event_id
HAVING COUNT(A.TARGET_CONCEPT_ID) >= 1
-- End Correlated Criteria

  ) CQ on E.person_id = CQ.person_id and E.event_id = CQ.event_id
  GROUP BY E.person_id, E.event_id
  HAVING COUNT(index_id) = 1
) G
-- End Criteria Group
) AC on AC.person_id = pe.person_id and AC.event_id = pe.event_id

) A on A.person_id = P.person_id  AND A.START_DATE >= P.OP_START_DATE AND A.START_DATE <= P.OP_END_DATE AND A.START_DATE >= (P.START_DATE + -30*INTERVAL'1 day') AND A.START_DATE <= (P.START_DATE + 30*INTERVAL'1 day')
GROUP BY p.person_id, p.event_id
HAVING COUNT(A.TARGET_CONCEPT_ID) >= 1
-- End Correlated Criteria

  ) CQ on E.person_id = CQ.person_id and E.event_id = CQ.event_id
  GROUP BY E.person_id, E.event_id
  HAVING COUNT(index_id) = 1
) G
-- End Criteria Group
) AC on AC.person_id = pe.person_id AND AC.event_id = pe.event_id
) Results
;
ANALYZE Inclusion_5
;

CREATE TEMP TABLE Inclusion_6

AS
SELECT
6 as inclusion_rule_id, person_id, event_id

FROM
(
  select pe.person_id, pe.event_id
  FROM qualified_events pe
  
JOIN (
-- Begin Criteria Group
select 0 as index_id, person_id, event_id
FROM
(
  select E.person_id, E.event_id 
  FROM qualified_events E
  INNER JOIN
  (
    -- Begin Correlated Criteria
SELECT 0 as index_id, p.person_id, p.event_id
FROM qualified_events P
INNER JOIN
(
  select PE.person_id, PE.event_id, PE.start_date, PE.end_date, PE.target_concept_id, PE.visit_occurrence_id, PE.sort_date FROM (
-- Begin Drug Exposure Criteria
select C.person_id, C.drug_exposure_id as event_id, C.drug_exposure_start_date as start_date,
       COALESCE(C.DRUG_EXPOSURE_END_DATE, (DRUG_EXPOSURE_START_DATE + C.DAYS_SUPPLY*INTERVAL'1 day'), (C.DRUG_EXPOSURE_START_DATE + 1*INTERVAL'1 day')) as end_date,
       C.drug_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.drug_exposure_start_date as sort_date
from 
(
  select de.* , row_number() over (PARTITION BY de.person_id ORDER BY de.drug_exposure_start_date, de.drug_exposure_id) as ordinal
  FROM cdm.DRUG_EXPOSURE de
JOIN Codesets codesets on ((de.drug_concept_id = codesets.concept_id and codesets.codeset_id = 0) AND (de.drug_source_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
JOIN cdm.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id
LEFT JOIN cdm.PROVIDER PR on C.provider_id = PR.provider_id
WHERE (C.drug_exposure_start_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.drug_exposure_start_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND (C.drug_exposure_end_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.drug_exposure_end_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.drug_type_concept_id  in (38000175,38000176)
AND C.stop_reason  like '%stop reason%'
AND (C.refills >= 0 and C.refills <= 99)
AND (C.quantity >= 0.0000 and C.quantity <= 99.0000)
AND (C.days_supply >= 0 and C.days_supply <= 99)
AND C.route_concept_id in (4263689,4023156,4006860)
AND (C.effective_drug_dose >= 0.0000 and C.effective_drug_dose <= 99.0000)
AND C.dose_unit_concept_id in (8554,8849,9214)
AND C.lot_number  like '%lot number%'
AND (EXTRACT(YEAR FROM C.drug_exposure_start_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.drug_exposure_start_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8532,8507)
AND PR.specialty_concept_id in (38004446,38003625)
AND V.visit_concept_id in (262,9201)
AND C.ordinal = 1
-- End Drug Exposure Criteria

) PE
JOIN (
-- Begin Criteria Group
select 0 as index_id, person_id, event_id
FROM
(
  select E.person_id, E.event_id 
  FROM (SELECT Q.person_id, Q.event_id, Q.start_date, Q.end_date, Q.visit_occurrence_id, OP.observation_period_start_date as op_start_date, OP.observation_period_end_date as op_end_date
FROM (-- Begin Drug Exposure Criteria
select C.person_id, C.drug_exposure_id as event_id, C.drug_exposure_start_date as start_date,
       COALESCE(C.DRUG_EXPOSURE_END_DATE, (DRUG_EXPOSURE_START_DATE + C.DAYS_SUPPLY*INTERVAL'1 day'), (C.DRUG_EXPOSURE_START_DATE + 1*INTERVAL'1 day')) as end_date,
       C.drug_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.drug_exposure_start_date as sort_date
from 
(
  select de.* , row_number() over (PARTITION BY de.person_id ORDER BY de.drug_exposure_start_date, de.drug_exposure_id) as ordinal
  FROM cdm.DRUG_EXPOSURE de
JOIN Codesets codesets on ((de.drug_concept_id = codesets.concept_id and codesets.codeset_id = 0) AND (de.drug_source_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
JOIN cdm.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id
LEFT JOIN cdm.PROVIDER PR on C.provider_id = PR.provider_id
WHERE (C.drug_exposure_start_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.drug_exposure_start_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND (C.drug_exposure_end_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.drug_exposure_end_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.drug_type_concept_id  in (38000175,38000176)
AND C.stop_reason  like '%stop reason%'
AND (C.refills >= 0 and C.refills <= 99)
AND (C.quantity >= 0.0000 and C.quantity <= 99.0000)
AND (C.days_supply >= 0 and C.days_supply <= 99)
AND C.route_concept_id in (4263689,4023156,4006860)
AND (C.effective_drug_dose >= 0.0000 and C.effective_drug_dose <= 99.0000)
AND C.dose_unit_concept_id in (8554,8849,9214)
AND C.lot_number  like '%lot number%'
AND (EXTRACT(YEAR FROM C.drug_exposure_start_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.drug_exposure_start_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8532,8507)
AND PR.specialty_concept_id in (38004446,38003625)
AND V.visit_concept_id in (262,9201)
AND C.ordinal = 1
-- End Drug Exposure Criteria
) Q
JOIN cdm.OBSERVATION_PERIOD OP on Q.person_id = OP.person_id 
  and OP.observation_period_start_date <= Q.start_date and OP.observation_period_end_date >= Q.start_date
) E
  INNER JOIN
  (
    -- Begin Correlated Criteria
SELECT 0 as index_id, p.person_id, p.event_id
FROM (SELECT Q.person_id, Q.event_id, Q.start_date, Q.end_date, Q.visit_occurrence_id, OP.observation_period_start_date as op_start_date, OP.observation_period_end_date as op_end_date
FROM (-- Begin Drug Exposure Criteria
select C.person_id, C.drug_exposure_id as event_id, C.drug_exposure_start_date as start_date,
       COALESCE(C.DRUG_EXPOSURE_END_DATE, (DRUG_EXPOSURE_START_DATE + C.DAYS_SUPPLY*INTERVAL'1 day'), (C.DRUG_EXPOSURE_START_DATE + 1*INTERVAL'1 day')) as end_date,
       C.drug_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.drug_exposure_start_date as sort_date
from 
(
  select de.* , row_number() over (PARTITION BY de.person_id ORDER BY de.drug_exposure_start_date, de.drug_exposure_id) as ordinal
  FROM cdm.DRUG_EXPOSURE de
JOIN Codesets codesets on ((de.drug_concept_id = codesets.concept_id and codesets.codeset_id = 0) AND (de.drug_source_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
JOIN cdm.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id
LEFT JOIN cdm.PROVIDER PR on C.provider_id = PR.provider_id
WHERE (C.drug_exposure_start_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.drug_exposure_start_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND (C.drug_exposure_end_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.drug_exposure_end_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.drug_type_concept_id  in (38000175,38000176)
AND C.stop_reason  like '%stop reason%'
AND (C.refills >= 0 and C.refills <= 99)
AND (C.quantity >= 0.0000 and C.quantity <= 99.0000)
AND (C.days_supply >= 0 and C.days_supply <= 99)
AND C.route_concept_id in (4263689,4023156,4006860)
AND (C.effective_drug_dose >= 0.0000 and C.effective_drug_dose <= 99.0000)
AND C.dose_unit_concept_id in (8554,8849,9214)
AND C.lot_number  like '%lot number%'
AND (EXTRACT(YEAR FROM C.drug_exposure_start_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.drug_exposure_start_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8532,8507)
AND PR.specialty_concept_id in (38004446,38003625)
AND V.visit_concept_id in (262,9201)
AND C.ordinal = 1
-- End Drug Exposure Criteria
) Q
JOIN cdm.OBSERVATION_PERIOD OP on Q.person_id = OP.person_id 
  and OP.observation_period_start_date <= Q.start_date and OP.observation_period_end_date >= Q.start_date
) P
INNER JOIN
(
  -- Begin Drug Exposure Criteria
select C.person_id, C.drug_exposure_id as event_id, C.drug_exposure_start_date as start_date,
       COALESCE(C.DRUG_EXPOSURE_END_DATE, (DRUG_EXPOSURE_START_DATE + C.DAYS_SUPPLY*INTERVAL'1 day'), (C.DRUG_EXPOSURE_START_DATE + 1*INTERVAL'1 day')) as end_date,
       C.drug_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.drug_exposure_start_date as sort_date
from 
(
  select de.* 
  FROM cdm.DRUG_EXPOSURE de

) C


-- End Drug Exposure Criteria

) A on A.person_id = P.person_id  AND A.visit_occurrence_id = P.visit_occurrence_id
GROUP BY p.person_id, p.event_id
HAVING COUNT(A.TARGET_CONCEPT_ID) >= 1
-- End Correlated Criteria

  ) CQ on E.person_id = CQ.person_id and E.event_id = CQ.event_id
  GROUP BY E.person_id, E.event_id
  HAVING COUNT(index_id) = 1
) G
-- End Criteria Group
) AC on AC.person_id = pe.person_id and AC.event_id = pe.event_id

) A on A.person_id = P.person_id  AND A.START_DATE >= P.OP_START_DATE AND A.START_DATE <= P.OP_END_DATE AND A.START_DATE >= (P.START_DATE + -30*INTERVAL'1 day') AND A.START_DATE <= (P.START_DATE + 30*INTERVAL'1 day')
GROUP BY p.person_id, p.event_id
HAVING COUNT(A.TARGET_CONCEPT_ID) >= 1
-- End Correlated Criteria

  ) CQ on E.person_id = CQ.person_id and E.event_id = CQ.event_id
  GROUP BY E.person_id, E.event_id
  HAVING COUNT(index_id) = 1
) G
-- End Criteria Group
) AC on AC.person_id = pe.person_id AND AC.event_id = pe.event_id
) Results
;
ANALYZE Inclusion_6
;

CREATE TEMP TABLE Inclusion_7

AS
SELECT
7 as inclusion_rule_id, person_id, event_id

FROM
(
  select pe.person_id, pe.event_id
  FROM qualified_events pe
  
JOIN (
-- Begin Criteria Group
select 0 as index_id, person_id, event_id
FROM
(
  select E.person_id, E.event_id 
  FROM qualified_events E
  INNER JOIN
  (
    -- Begin Correlated Criteria
SELECT 0 as index_id, p.person_id, p.event_id
FROM qualified_events P
INNER JOIN
(
  select PE.person_id, PE.event_id, PE.start_date, PE.end_date, PE.target_concept_id, PE.visit_occurrence_id, PE.sort_date FROM (
-- Begin Measurement Criteria
select C.person_id, C.measurement_id as event_id, C.measurement_date as start_date, (C.measurement_date + 1*INTERVAL'1 day') as END_DATE,
       C.measurement_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.measurement_date as sort_date
from 
(
  select m.* , row_number() over (PARTITION BY m.person_id ORDER BY m.measurement_date, m.measurement_id) as ordinal
  FROM cdm.MEASUREMENT m
JOIN Codesets codesets on ((m.measurement_source_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
JOIN cdm.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id
LEFT JOIN cdm.PROVIDER PR on C.provider_id = PR.provider_id
WHERE (C.measurement_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.measurement_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.measurement_type_concept_id  in (38000183,38000199)
AND C.operator_concept_id in (4172704)
AND (C.value_as_number >= 0.0000 and C.value_as_number <= 99.0000)
AND C.value_as_concept_id in (9191)
AND C.unit_concept_id in (9256)
AND (C.range_low >= 0.0000 and C.range_low <= 99.0000)
AND (C.range_high >= 0.0000 and C.range_high <= 99.0000)
AND ((C.value_as_number / NULLIF(C.range_low, 0)) >= 0.0000 and (C.value_as_number / NULLIF(C.range_low, 0)) <= 99.0000)
AND ((C.value_as_number / NULLIF(C.range_high, 0)) >= 0.0000 and (C.value_as_number / NULLIF(C.range_high, 0)) <= 99.0000)
AND (C.value_as_number < C.range_low or C.value_as_number > C.range_high or C.value_as_concept_id in (4155142, 4155143))
AND (EXTRACT(YEAR FROM C.measurement_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.measurement_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8532,8507)
AND PR.specialty_concept_id in (38004446,38003626,38003627)
AND V.visit_concept_id in (262,9201)
AND C.ordinal = 1
-- End Measurement Criteria

) PE
JOIN (
-- Begin Criteria Group
select 0 as index_id, person_id, event_id
FROM
(
  select E.person_id, E.event_id 
  FROM (SELECT Q.person_id, Q.event_id, Q.start_date, Q.end_date, Q.visit_occurrence_id, OP.observation_period_start_date as op_start_date, OP.observation_period_end_date as op_end_date
FROM (-- Begin Measurement Criteria
select C.person_id, C.measurement_id as event_id, C.measurement_date as start_date, (C.measurement_date + 1*INTERVAL'1 day') as END_DATE,
       C.measurement_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.measurement_date as sort_date
from 
(
  select m.* , row_number() over (PARTITION BY m.person_id ORDER BY m.measurement_date, m.measurement_id) as ordinal
  FROM cdm.MEASUREMENT m
JOIN Codesets codesets on ((m.measurement_source_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
JOIN cdm.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id
LEFT JOIN cdm.PROVIDER PR on C.provider_id = PR.provider_id
WHERE (C.measurement_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.measurement_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.measurement_type_concept_id  in (38000183,38000199)
AND C.operator_concept_id in (4172704)
AND (C.value_as_number >= 0.0000 and C.value_as_number <= 99.0000)
AND C.value_as_concept_id in (9191)
AND C.unit_concept_id in (9256)
AND (C.range_low >= 0.0000 and C.range_low <= 99.0000)
AND (C.range_high >= 0.0000 and C.range_high <= 99.0000)
AND ((C.value_as_number / NULLIF(C.range_low, 0)) >= 0.0000 and (C.value_as_number / NULLIF(C.range_low, 0)) <= 99.0000)
AND ((C.value_as_number / NULLIF(C.range_high, 0)) >= 0.0000 and (C.value_as_number / NULLIF(C.range_high, 0)) <= 99.0000)
AND (C.value_as_number < C.range_low or C.value_as_number > C.range_high or C.value_as_concept_id in (4155142, 4155143))
AND (EXTRACT(YEAR FROM C.measurement_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.measurement_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8532,8507)
AND PR.specialty_concept_id in (38004446,38003626,38003627)
AND V.visit_concept_id in (262,9201)
AND C.ordinal = 1
-- End Measurement Criteria
) Q
JOIN cdm.OBSERVATION_PERIOD OP on Q.person_id = OP.person_id 
  and OP.observation_period_start_date <= Q.start_date and OP.observation_period_end_date >= Q.start_date
) E
  INNER JOIN
  (
    -- Begin Correlated Criteria
SELECT 0 as index_id, p.person_id, p.event_id
FROM (SELECT Q.person_id, Q.event_id, Q.start_date, Q.end_date, Q.visit_occurrence_id, OP.observation_period_start_date as op_start_date, OP.observation_period_end_date as op_end_date
FROM (-- Begin Measurement Criteria
select C.person_id, C.measurement_id as event_id, C.measurement_date as start_date, (C.measurement_date + 1*INTERVAL'1 day') as END_DATE,
       C.measurement_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.measurement_date as sort_date
from 
(
  select m.* , row_number() over (PARTITION BY m.person_id ORDER BY m.measurement_date, m.measurement_id) as ordinal
  FROM cdm.MEASUREMENT m
JOIN Codesets codesets on ((m.measurement_source_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
JOIN cdm.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id
LEFT JOIN cdm.PROVIDER PR on C.provider_id = PR.provider_id
WHERE (C.measurement_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.measurement_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.measurement_type_concept_id  in (38000183,38000199)
AND C.operator_concept_id in (4172704)
AND (C.value_as_number >= 0.0000 and C.value_as_number <= 99.0000)
AND C.value_as_concept_id in (9191)
AND C.unit_concept_id in (9256)
AND (C.range_low >= 0.0000 and C.range_low <= 99.0000)
AND (C.range_high >= 0.0000 and C.range_high <= 99.0000)
AND ((C.value_as_number / NULLIF(C.range_low, 0)) >= 0.0000 and (C.value_as_number / NULLIF(C.range_low, 0)) <= 99.0000)
AND ((C.value_as_number / NULLIF(C.range_high, 0)) >= 0.0000 and (C.value_as_number / NULLIF(C.range_high, 0)) <= 99.0000)
AND (C.value_as_number < C.range_low or C.value_as_number > C.range_high or C.value_as_concept_id in (4155142, 4155143))
AND (EXTRACT(YEAR FROM C.measurement_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.measurement_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8532,8507)
AND PR.specialty_concept_id in (38004446,38003626,38003627)
AND V.visit_concept_id in (262,9201)
AND C.ordinal = 1
-- End Measurement Criteria
) Q
JOIN cdm.OBSERVATION_PERIOD OP on Q.person_id = OP.person_id 
  and OP.observation_period_start_date <= Q.start_date and OP.observation_period_end_date >= Q.start_date
) P
INNER JOIN
(
  -- Begin Measurement Criteria
select C.person_id, C.measurement_id as event_id, C.measurement_date as start_date, (C.measurement_date + 1*INTERVAL'1 day') as END_DATE,
       C.measurement_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.measurement_date as sort_date
from 
(
  select m.* 
  FROM cdm.MEASUREMENT m
JOIN Codesets codesets on ((m.measurement_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C


-- End Measurement Criteria

) A on A.person_id = P.person_id  AND A.visit_occurrence_id = P.visit_occurrence_id
GROUP BY p.person_id, p.event_id
HAVING COUNT(A.TARGET_CONCEPT_ID) >= 1
-- End Correlated Criteria

  ) CQ on E.person_id = CQ.person_id and E.event_id = CQ.event_id
  GROUP BY E.person_id, E.event_id
  HAVING COUNT(index_id) = 1
) G
-- End Criteria Group
) AC on AC.person_id = pe.person_id and AC.event_id = pe.event_id

) A on A.person_id = P.person_id  AND A.START_DATE >= P.OP_START_DATE AND A.START_DATE <= P.OP_END_DATE AND A.START_DATE >= (P.START_DATE + -30*INTERVAL'1 day') AND A.START_DATE <= (P.START_DATE + 30*INTERVAL'1 day')
GROUP BY p.person_id, p.event_id
HAVING COUNT(A.TARGET_CONCEPT_ID) >= 1
-- End Correlated Criteria

  ) CQ on E.person_id = CQ.person_id and E.event_id = CQ.event_id
  GROUP BY E.person_id, E.event_id
  HAVING COUNT(index_id) = 1
) G
-- End Criteria Group
) AC on AC.person_id = pe.person_id AND AC.event_id = pe.event_id
) Results
;
ANALYZE Inclusion_7
;

CREATE TEMP TABLE Inclusion_8

AS
SELECT
8 as inclusion_rule_id, person_id, event_id

FROM
(
  select pe.person_id, pe.event_id
  FROM qualified_events pe
  
JOIN (
-- Begin Criteria Group
select 0 as index_id, person_id, event_id
FROM
(
  select E.person_id, E.event_id 
  FROM qualified_events E
  INNER JOIN
  (
    -- Begin Correlated Criteria
SELECT 0 as index_id, p.person_id, p.event_id
FROM qualified_events P
INNER JOIN
(
  select PE.person_id, PE.event_id, PE.start_date, PE.end_date, PE.target_concept_id, PE.visit_occurrence_id, PE.sort_date FROM (
-- Begin Observation Criteria
select C.person_id, C.observation_id as event_id, C.observation_date as start_date, (C.observation_date + 1*INTERVAL'1 day') as END_DATE,
       C.observation_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.observation_date as sort_date
from 
(
  select o.* , row_number() over (PARTITION BY o.person_id ORDER BY o.observation_date, o.observation_id) as ordinal
  FROM cdm.OBSERVATION o
JOIN Codesets codesets on ((o.observation_concept_id = codesets.concept_id and codesets.codeset_id = 0) AND (o.observation_source_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
JOIN cdm.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id
LEFT JOIN cdm.PROVIDER PR on C.provider_id = PR.provider_id
WHERE (C.observation_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.observation_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.observation_type_concept_id  in (38000183,38000199)
AND (C.value_as_number >= 0.0000 and C.value_as_number <= 99.0000)
AND C.value_as_string  like '%some value%'
AND C.value_as_concept_id in (36312355)
AND C.qualifier_concept_id in (4172703,4171754)
AND C.unit_concept_id in (8554)
AND (EXTRACT(YEAR FROM C.observation_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.observation_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8532,8507)
AND PR.specialty_concept_id in (38004446,38003627)
AND V.visit_concept_id in (262,9201)
AND C.ordinal = 1
-- End Observation Criteria

) PE
JOIN (
-- Begin Criteria Group
select 0 as index_id, person_id, event_id
FROM
(
  select E.person_id, E.event_id 
  FROM (SELECT Q.person_id, Q.event_id, Q.start_date, Q.end_date, Q.visit_occurrence_id, OP.observation_period_start_date as op_start_date, OP.observation_period_end_date as op_end_date
FROM (-- Begin Observation Criteria
select C.person_id, C.observation_id as event_id, C.observation_date as start_date, (C.observation_date + 1*INTERVAL'1 day') as END_DATE,
       C.observation_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.observation_date as sort_date
from 
(
  select o.* , row_number() over (PARTITION BY o.person_id ORDER BY o.observation_date, o.observation_id) as ordinal
  FROM cdm.OBSERVATION o
JOIN Codesets codesets on ((o.observation_concept_id = codesets.concept_id and codesets.codeset_id = 0) AND (o.observation_source_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
JOIN cdm.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id
LEFT JOIN cdm.PROVIDER PR on C.provider_id = PR.provider_id
WHERE (C.observation_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.observation_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.observation_type_concept_id  in (38000183,38000199)
AND (C.value_as_number >= 0.0000 and C.value_as_number <= 99.0000)
AND C.value_as_string  like '%some value%'
AND C.value_as_concept_id in (36312355)
AND C.qualifier_concept_id in (4172703,4171754)
AND C.unit_concept_id in (8554)
AND (EXTRACT(YEAR FROM C.observation_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.observation_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8532,8507)
AND PR.specialty_concept_id in (38004446,38003627)
AND V.visit_concept_id in (262,9201)
AND C.ordinal = 1
-- End Observation Criteria
) Q
JOIN cdm.OBSERVATION_PERIOD OP on Q.person_id = OP.person_id 
  and OP.observation_period_start_date <= Q.start_date and OP.observation_period_end_date >= Q.start_date
) E
  INNER JOIN
  (
    -- Begin Correlated Criteria
SELECT 0 as index_id, p.person_id, p.event_id
FROM (SELECT Q.person_id, Q.event_id, Q.start_date, Q.end_date, Q.visit_occurrence_id, OP.observation_period_start_date as op_start_date, OP.observation_period_end_date as op_end_date
FROM (-- Begin Observation Criteria
select C.person_id, C.observation_id as event_id, C.observation_date as start_date, (C.observation_date + 1*INTERVAL'1 day') as END_DATE,
       C.observation_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.observation_date as sort_date
from 
(
  select o.* , row_number() over (PARTITION BY o.person_id ORDER BY o.observation_date, o.observation_id) as ordinal
  FROM cdm.OBSERVATION o
JOIN Codesets codesets on ((o.observation_concept_id = codesets.concept_id and codesets.codeset_id = 0) AND (o.observation_source_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
JOIN cdm.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id
LEFT JOIN cdm.PROVIDER PR on C.provider_id = PR.provider_id
WHERE (C.observation_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.observation_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.observation_type_concept_id  in (38000183,38000199)
AND (C.value_as_number >= 0.0000 and C.value_as_number <= 99.0000)
AND C.value_as_string  like '%some value%'
AND C.value_as_concept_id in (36312355)
AND C.qualifier_concept_id in (4172703,4171754)
AND C.unit_concept_id in (8554)
AND (EXTRACT(YEAR FROM C.observation_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.observation_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8532,8507)
AND PR.specialty_concept_id in (38004446,38003627)
AND V.visit_concept_id in (262,9201)
AND C.ordinal = 1
-- End Observation Criteria
) Q
JOIN cdm.OBSERVATION_PERIOD OP on Q.person_id = OP.person_id 
  and OP.observation_period_start_date <= Q.start_date and OP.observation_period_end_date >= Q.start_date
) P
INNER JOIN
(
  -- Begin Observation Criteria
select C.person_id, C.observation_id as event_id, C.observation_date as start_date, (C.observation_date + 1*INTERVAL'1 day') as END_DATE,
       C.observation_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.observation_date as sort_date
from 
(
  select o.* 
  FROM cdm.OBSERVATION o
JOIN Codesets codesets on ((o.observation_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C


-- End Observation Criteria

) A on A.person_id = P.person_id  AND A.START_DATE >= P.OP_START_DATE AND A.START_DATE <= P.OP_END_DATE AND A.START_DATE >= P.OP_START_DATE AND A.START_DATE <= P.OP_END_DATE AND A.visit_occurrence_id = P.visit_occurrence_id
GROUP BY p.person_id, p.event_id
HAVING COUNT(A.TARGET_CONCEPT_ID) >= 1
-- End Correlated Criteria

  ) CQ on E.person_id = CQ.person_id and E.event_id = CQ.event_id
  GROUP BY E.person_id, E.event_id
  HAVING COUNT(index_id) = 1
) G
-- End Criteria Group
) AC on AC.person_id = pe.person_id and AC.event_id = pe.event_id

) A on A.person_id = P.person_id  AND A.START_DATE >= P.OP_START_DATE AND A.START_DATE <= P.OP_END_DATE AND A.START_DATE >= (P.START_DATE + -30*INTERVAL'1 day') AND A.START_DATE <= (P.START_DATE + 30*INTERVAL'1 day')
GROUP BY p.person_id, p.event_id
HAVING COUNT(A.TARGET_CONCEPT_ID) >= 1
-- End Correlated Criteria

  ) CQ on E.person_id = CQ.person_id and E.event_id = CQ.event_id
  GROUP BY E.person_id, E.event_id
  HAVING COUNT(index_id) = 1
) G
-- End Criteria Group
) AC on AC.person_id = pe.person_id AND AC.event_id = pe.event_id
) Results
;
ANALYZE Inclusion_8
;

CREATE TEMP TABLE Inclusion_9

AS
SELECT
9 as inclusion_rule_id, person_id, event_id

FROM
(
  select pe.person_id, pe.event_id
  FROM qualified_events pe
  
JOIN (
-- Begin Criteria Group
select 0 as index_id, person_id, event_id
FROM
(
  select E.person_id, E.event_id 
  FROM qualified_events E
  INNER JOIN
  (
    -- Begin Correlated Criteria
SELECT 0 as index_id, p.person_id, p.event_id
FROM qualified_events P
INNER JOIN
(
  select PE.person_id, PE.event_id, PE.start_date, PE.end_date, PE.target_concept_id, PE.visit_occurrence_id, PE.sort_date FROM (
-- Begin Observation Period Criteria
select C.person_id, C.observation_period_id as event_id, TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') as start_date, TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') as end_date,
       C.period_type_concept_id as TARGET_CONCEPT_ID, CAST(NULL as bigint) as visit_occurrence_id,
       C.observation_period_start_date as sort_date

from 
(
        select op.*, row_number() over (PARTITION BY op.person_id ORDER BY op.observation_period_start_date) as ordinal
        FROM cdm.OBSERVATION_PERIOD op
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
WHERE C.ordinal = 1
AND C.OBSERVATION_PERIOD_START_DATE <= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.OBSERVATION_PERIOD_END_DATE >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD')
AND C.OBSERVATION_PERIOD_START_DATE <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.OBSERVATION_PERIOD_END_DATE >= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD')
AND (C.observation_period_start_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.observation_period_start_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND (C.observation_period_end_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.observation_period_end_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.period_type_concept_id in (38000183,38000199)
AND ((CAST(C.observation_period_end_date AS DATE) - CAST(C.observation_period_start_date AS DATE)) >= 0 and (CAST(C.observation_period_end_date AS DATE) - CAST(C.observation_period_start_date AS DATE)) <= 99)
AND (EXTRACT(YEAR FROM C.observation_period_start_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.observation_period_start_date) - P.year_of_birth <= 99)
AND (EXTRACT(YEAR FROM C.observation_period_end_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.observation_period_end_date) - P.year_of_birth <= 99)
-- End Observation Period Criteria

) PE
JOIN (
-- Begin Criteria Group
select 0 as index_id, person_id, event_id
FROM
(
  select E.person_id, E.event_id 
  FROM (SELECT Q.person_id, Q.event_id, Q.start_date, Q.end_date, Q.visit_occurrence_id, OP.observation_period_start_date as op_start_date, OP.observation_period_end_date as op_end_date
FROM (-- Begin Observation Period Criteria
select C.person_id, C.observation_period_id as event_id, TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') as start_date, TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') as end_date,
       C.period_type_concept_id as TARGET_CONCEPT_ID, CAST(NULL as bigint) as visit_occurrence_id,
       C.observation_period_start_date as sort_date

from 
(
        select op.*, row_number() over (PARTITION BY op.person_id ORDER BY op.observation_period_start_date) as ordinal
        FROM cdm.OBSERVATION_PERIOD op
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
WHERE C.ordinal = 1
AND C.OBSERVATION_PERIOD_START_DATE <= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.OBSERVATION_PERIOD_END_DATE >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD')
AND C.OBSERVATION_PERIOD_START_DATE <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.OBSERVATION_PERIOD_END_DATE >= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD')
AND (C.observation_period_start_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.observation_period_start_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND (C.observation_period_end_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.observation_period_end_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.period_type_concept_id in (38000183,38000199)
AND ((CAST(C.observation_period_end_date AS DATE) - CAST(C.observation_period_start_date AS DATE)) >= 0 and (CAST(C.observation_period_end_date AS DATE) - CAST(C.observation_period_start_date AS DATE)) <= 99)
AND (EXTRACT(YEAR FROM C.observation_period_start_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.observation_period_start_date) - P.year_of_birth <= 99)
AND (EXTRACT(YEAR FROM C.observation_period_end_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.observation_period_end_date) - P.year_of_birth <= 99)
-- End Observation Period Criteria
) Q
JOIN cdm.OBSERVATION_PERIOD OP on Q.person_id = OP.person_id 
  and OP.observation_period_start_date <= Q.start_date and OP.observation_period_end_date >= Q.start_date
) E
  INNER JOIN
  (
    -- Begin Correlated Criteria
SELECT 0 as index_id, p.person_id, p.event_id
FROM (SELECT Q.person_id, Q.event_id, Q.start_date, Q.end_date, Q.visit_occurrence_id, OP.observation_period_start_date as op_start_date, OP.observation_period_end_date as op_end_date
FROM (-- Begin Observation Period Criteria
select C.person_id, C.observation_period_id as event_id, TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') as start_date, TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') as end_date,
       C.period_type_concept_id as TARGET_CONCEPT_ID, CAST(NULL as bigint) as visit_occurrence_id,
       C.observation_period_start_date as sort_date

from 
(
        select op.*, row_number() over (PARTITION BY op.person_id ORDER BY op.observation_period_start_date) as ordinal
        FROM cdm.OBSERVATION_PERIOD op
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
WHERE C.ordinal = 1
AND C.OBSERVATION_PERIOD_START_DATE <= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.OBSERVATION_PERIOD_END_DATE >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD')
AND C.OBSERVATION_PERIOD_START_DATE <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.OBSERVATION_PERIOD_END_DATE >= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD')
AND (C.observation_period_start_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.observation_period_start_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND (C.observation_period_end_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.observation_period_end_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.period_type_concept_id in (38000183,38000199)
AND ((CAST(C.observation_period_end_date AS DATE) - CAST(C.observation_period_start_date AS DATE)) >= 0 and (CAST(C.observation_period_end_date AS DATE) - CAST(C.observation_period_start_date AS DATE)) <= 99)
AND (EXTRACT(YEAR FROM C.observation_period_start_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.observation_period_start_date) - P.year_of_birth <= 99)
AND (EXTRACT(YEAR FROM C.observation_period_end_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.observation_period_end_date) - P.year_of_birth <= 99)
-- End Observation Period Criteria
) Q
JOIN cdm.OBSERVATION_PERIOD OP on Q.person_id = OP.person_id 
  and OP.observation_period_start_date <= Q.start_date and OP.observation_period_end_date >= Q.start_date
) P
INNER JOIN
(
  -- Begin Observation Period Criteria
select C.person_id, C.observation_period_id as event_id, C.observation_period_start_date as start_date, C.observation_period_end_date as end_date,
       C.period_type_concept_id as TARGET_CONCEPT_ID, CAST(NULL as bigint) as visit_occurrence_id,
       C.observation_period_start_date as sort_date

from 
(
        select op.*, row_number() over (PARTITION BY op.person_id ORDER BY op.observation_period_start_date) as ordinal
        FROM cdm.OBSERVATION_PERIOD op
) C


-- End Observation Period Criteria

) A on A.person_id = P.person_id 
GROUP BY p.person_id, p.event_id
HAVING COUNT(A.TARGET_CONCEPT_ID) >= 1
-- End Correlated Criteria

  ) CQ on E.person_id = CQ.person_id and E.event_id = CQ.event_id
  GROUP BY E.person_id, E.event_id
  HAVING COUNT(index_id) = 1
) G
-- End Criteria Group
) AC on AC.person_id = pe.person_id and AC.event_id = pe.event_id

) A on A.person_id = P.person_id  AND A.START_DATE >= P.OP_START_DATE AND A.START_DATE <= P.OP_END_DATE AND A.START_DATE >= (P.START_DATE + -30*INTERVAL'1 day') AND A.START_DATE <= (P.START_DATE + 30*INTERVAL'1 day')
GROUP BY p.person_id, p.event_id
HAVING COUNT(A.TARGET_CONCEPT_ID) >= 1
-- End Correlated Criteria

  ) CQ on E.person_id = CQ.person_id and E.event_id = CQ.event_id
  GROUP BY E.person_id, E.event_id
  HAVING COUNT(index_id) = 1
) G
-- End Criteria Group
) AC on AC.person_id = pe.person_id AND AC.event_id = pe.event_id
) Results
;
ANALYZE Inclusion_9
;

CREATE TEMP TABLE Inclusion_10

AS
SELECT
10 as inclusion_rule_id, person_id, event_id

FROM
(
  select pe.person_id, pe.event_id
  FROM qualified_events pe
  
JOIN (
-- Begin Criteria Group
select 0 as index_id, person_id, event_id
FROM
(
  select E.person_id, E.event_id 
  FROM qualified_events E
  INNER JOIN
  (
    -- Begin Correlated Criteria
SELECT 0 as index_id, p.person_id, p.event_id
FROM qualified_events P
INNER JOIN
(
  select PE.person_id, PE.event_id, PE.start_date, PE.end_date, PE.target_concept_id, PE.visit_occurrence_id, PE.sort_date FROM (
-- Begin Procedure Occurrence Criteria
select C.person_id, C.procedure_occurrence_id as event_id, C.procedure_date as start_date, (C.procedure_date + 1*INTERVAL'1 day') as END_DATE,
       C.procedure_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.procedure_date as sort_date
from 
(
  select po.* , row_number() over (PARTITION BY po.person_id ORDER BY po.procedure_date, po.procedure_occurrence_id) as ordinal
  FROM cdm.PROCEDURE_OCCURRENCE po
JOIN Codesets codesets on ((po.procedure_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
JOIN cdm.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id
LEFT JOIN cdm.PROVIDER PR on C.provider_id = PR.provider_id
WHERE (C.procedure_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.procedure_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.procedure_type_concept_id  in (38000183,38000199)
AND C.modifier_concept_id in (4000804,4001771)
AND (C.quantity >= 0 and C.quantity <= 99)
AND (EXTRACT(YEAR FROM C.procedure_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.procedure_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8532,8507)
AND PR.specialty_concept_id in (38004446,38003627)
AND V.visit_concept_id in (262,9201)
AND C.ordinal = 1
-- End Procedure Occurrence Criteria

) PE
JOIN (
-- Begin Criteria Group
select 0 as index_id, person_id, event_id
FROM
(
  select E.person_id, E.event_id 
  FROM (SELECT Q.person_id, Q.event_id, Q.start_date, Q.end_date, Q.visit_occurrence_id, OP.observation_period_start_date as op_start_date, OP.observation_period_end_date as op_end_date
FROM (-- Begin Procedure Occurrence Criteria
select C.person_id, C.procedure_occurrence_id as event_id, C.procedure_date as start_date, (C.procedure_date + 1*INTERVAL'1 day') as END_DATE,
       C.procedure_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.procedure_date as sort_date
from 
(
  select po.* , row_number() over (PARTITION BY po.person_id ORDER BY po.procedure_date, po.procedure_occurrence_id) as ordinal
  FROM cdm.PROCEDURE_OCCURRENCE po
JOIN Codesets codesets on ((po.procedure_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
JOIN cdm.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id
LEFT JOIN cdm.PROVIDER PR on C.provider_id = PR.provider_id
WHERE (C.procedure_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.procedure_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.procedure_type_concept_id  in (38000183,38000199)
AND C.modifier_concept_id in (4000804,4001771)
AND (C.quantity >= 0 and C.quantity <= 99)
AND (EXTRACT(YEAR FROM C.procedure_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.procedure_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8532,8507)
AND PR.specialty_concept_id in (38004446,38003627)
AND V.visit_concept_id in (262,9201)
AND C.ordinal = 1
-- End Procedure Occurrence Criteria
) Q
JOIN cdm.OBSERVATION_PERIOD OP on Q.person_id = OP.person_id 
  and OP.observation_period_start_date <= Q.start_date and OP.observation_period_end_date >= Q.start_date
) E
  INNER JOIN
  (
    -- Begin Correlated Criteria
SELECT 0 as index_id, p.person_id, p.event_id
FROM (SELECT Q.person_id, Q.event_id, Q.start_date, Q.end_date, Q.visit_occurrence_id, OP.observation_period_start_date as op_start_date, OP.observation_period_end_date as op_end_date
FROM (-- Begin Procedure Occurrence Criteria
select C.person_id, C.procedure_occurrence_id as event_id, C.procedure_date as start_date, (C.procedure_date + 1*INTERVAL'1 day') as END_DATE,
       C.procedure_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.procedure_date as sort_date
from 
(
  select po.* , row_number() over (PARTITION BY po.person_id ORDER BY po.procedure_date, po.procedure_occurrence_id) as ordinal
  FROM cdm.PROCEDURE_OCCURRENCE po
JOIN Codesets codesets on ((po.procedure_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
JOIN cdm.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id
LEFT JOIN cdm.PROVIDER PR on C.provider_id = PR.provider_id
WHERE (C.procedure_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.procedure_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.procedure_type_concept_id  in (38000183,38000199)
AND C.modifier_concept_id in (4000804,4001771)
AND (C.quantity >= 0 and C.quantity <= 99)
AND (EXTRACT(YEAR FROM C.procedure_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.procedure_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8532,8507)
AND PR.specialty_concept_id in (38004446,38003627)
AND V.visit_concept_id in (262,9201)
AND C.ordinal = 1
-- End Procedure Occurrence Criteria
) Q
JOIN cdm.OBSERVATION_PERIOD OP on Q.person_id = OP.person_id 
  and OP.observation_period_start_date <= Q.start_date and OP.observation_period_end_date >= Q.start_date
) P
INNER JOIN
(
  -- Begin Procedure Occurrence Criteria
select C.person_id, C.procedure_occurrence_id as event_id, C.procedure_date as start_date, (C.procedure_date + 1*INTERVAL'1 day') as END_DATE,
       C.procedure_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.procedure_date as sort_date
from 
(
  select po.* 
  FROM cdm.PROCEDURE_OCCURRENCE po
JOIN Codesets codesets on ((po.procedure_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C


-- End Procedure Occurrence Criteria

) A on A.person_id = P.person_id  AND A.visit_occurrence_id = P.visit_occurrence_id
GROUP BY p.person_id, p.event_id
HAVING COUNT(A.TARGET_CONCEPT_ID) >= 1
-- End Correlated Criteria

  ) CQ on E.person_id = CQ.person_id and E.event_id = CQ.event_id
  GROUP BY E.person_id, E.event_id
  HAVING COUNT(index_id) = 1
) G
-- End Criteria Group
) AC on AC.person_id = pe.person_id and AC.event_id = pe.event_id

) A on A.person_id = P.person_id  AND A.START_DATE >= P.OP_START_DATE AND A.START_DATE <= P.OP_END_DATE AND A.START_DATE >= (P.START_DATE + -30*INTERVAL'1 day') AND A.START_DATE <= (P.START_DATE + 30*INTERVAL'1 day')
GROUP BY p.person_id, p.event_id
HAVING COUNT(A.TARGET_CONCEPT_ID) >= 1
-- End Correlated Criteria

  ) CQ on E.person_id = CQ.person_id and E.event_id = CQ.event_id
  GROUP BY E.person_id, E.event_id
  HAVING COUNT(index_id) = 1
) G
-- End Criteria Group
) AC on AC.person_id = pe.person_id AND AC.event_id = pe.event_id
) Results
;
ANALYZE Inclusion_10
;

CREATE TEMP TABLE Inclusion_11

AS
SELECT
11 as inclusion_rule_id, person_id, event_id

FROM
(
  select pe.person_id, pe.event_id
  FROM qualified_events pe
  
JOIN (
-- Begin Criteria Group
select 0 as index_id, person_id, event_id
FROM
(
  select E.person_id, E.event_id 
  FROM qualified_events E
  INNER JOIN
  (
    -- Begin Correlated Criteria
SELECT 0 as index_id, p.person_id, p.event_id
FROM qualified_events P
INNER JOIN
(
  select PE.person_id, PE.event_id, PE.start_date, PE.end_date, PE.target_concept_id, PE.visit_occurrence_id, PE.sort_date FROM (
-- Begin Specimen Criteria
select C.person_id, C.specimen_id as event_id, C.specimen_date as start_date, (C.specimen_date + 1*INTERVAL'1 day') as end_date,
       C.specimen_concept_id as TARGET_CONCEPT_ID, CAST(NULL as bigint) as visit_occurrence_id,
       C.specimen_date as sort_date
from 
(
  select s.* , row_number() over (PARTITION BY s.person_id ORDER BY s.specimen_date, s.specimen_id) as ordinal
  FROM cdm.SPECIMEN s
where s.specimen_concept_id in (SELECT concept_id from  Codesets where codeset_id = 0)
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
WHERE (C.specimen_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.specimen_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.specimen_type_concept_id  in (38000183,38000199)
AND (C.quantity >= 0.0000 and C.quantity <= 99.0000)
AND C.unit_concept_id in (8688,9218)
AND C.anatomic_site_concept_id in (4002601,4001422,4001433)
AND C.disease_status_concept_id in (4066212)
AND C.specimen_source_id  like '%some sourceID%'
AND (EXTRACT(YEAR FROM C.specimen_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.specimen_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8532,8507)
AND C.ordinal = 1
-- End Specimen Criteria

) PE
JOIN (
-- Begin Criteria Group
select 0 as index_id, person_id, event_id
FROM
(
  select E.person_id, E.event_id 
  FROM (SELECT Q.person_id, Q.event_id, Q.start_date, Q.end_date, Q.visit_occurrence_id, OP.observation_period_start_date as op_start_date, OP.observation_period_end_date as op_end_date
FROM (-- Begin Specimen Criteria
select C.person_id, C.specimen_id as event_id, C.specimen_date as start_date, (C.specimen_date + 1*INTERVAL'1 day') as end_date,
       C.specimen_concept_id as TARGET_CONCEPT_ID, CAST(NULL as bigint) as visit_occurrence_id,
       C.specimen_date as sort_date
from 
(
  select s.* , row_number() over (PARTITION BY s.person_id ORDER BY s.specimen_date, s.specimen_id) as ordinal
  FROM cdm.SPECIMEN s
where s.specimen_concept_id in (SELECT concept_id from  Codesets where codeset_id = 0)
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
WHERE (C.specimen_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.specimen_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.specimen_type_concept_id  in (38000183,38000199)
AND (C.quantity >= 0.0000 and C.quantity <= 99.0000)
AND C.unit_concept_id in (8688,9218)
AND C.anatomic_site_concept_id in (4002601,4001422,4001433)
AND C.disease_status_concept_id in (4066212)
AND C.specimen_source_id  like '%some sourceID%'
AND (EXTRACT(YEAR FROM C.specimen_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.specimen_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8532,8507)
AND C.ordinal = 1
-- End Specimen Criteria
) Q
JOIN cdm.OBSERVATION_PERIOD OP on Q.person_id = OP.person_id 
  and OP.observation_period_start_date <= Q.start_date and OP.observation_period_end_date >= Q.start_date
) E
  INNER JOIN
  (
    -- Begin Correlated Criteria
SELECT 0 as index_id, p.person_id, p.event_id
FROM (SELECT Q.person_id, Q.event_id, Q.start_date, Q.end_date, Q.visit_occurrence_id, OP.observation_period_start_date as op_start_date, OP.observation_period_end_date as op_end_date
FROM (-- Begin Specimen Criteria
select C.person_id, C.specimen_id as event_id, C.specimen_date as start_date, (C.specimen_date + 1*INTERVAL'1 day') as end_date,
       C.specimen_concept_id as TARGET_CONCEPT_ID, CAST(NULL as bigint) as visit_occurrence_id,
       C.specimen_date as sort_date
from 
(
  select s.* , row_number() over (PARTITION BY s.person_id ORDER BY s.specimen_date, s.specimen_id) as ordinal
  FROM cdm.SPECIMEN s
where s.specimen_concept_id in (SELECT concept_id from  Codesets where codeset_id = 0)
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
WHERE (C.specimen_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.specimen_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.specimen_type_concept_id  in (38000183,38000199)
AND (C.quantity >= 0.0000 and C.quantity <= 99.0000)
AND C.unit_concept_id in (8688,9218)
AND C.anatomic_site_concept_id in (4002601,4001422,4001433)
AND C.disease_status_concept_id in (4066212)
AND C.specimen_source_id  like '%some sourceID%'
AND (EXTRACT(YEAR FROM C.specimen_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.specimen_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8532,8507)
AND C.ordinal = 1
-- End Specimen Criteria
) Q
JOIN cdm.OBSERVATION_PERIOD OP on Q.person_id = OP.person_id 
  and OP.observation_period_start_date <= Q.start_date and OP.observation_period_end_date >= Q.start_date
) P
INNER JOIN
(
  -- Begin Specimen Criteria
select C.person_id, C.specimen_id as event_id, C.specimen_date as start_date, (C.specimen_date + 1*INTERVAL'1 day') as end_date,
       C.specimen_concept_id as TARGET_CONCEPT_ID, CAST(NULL as bigint) as visit_occurrence_id,
       C.specimen_date as sort_date
from 
(
  select s.* 
  FROM cdm.SPECIMEN s
where s.specimen_concept_id in (SELECT concept_id from  Codesets where codeset_id = 0)
) C


-- End Specimen Criteria

) A on A.person_id = P.person_id 
GROUP BY p.person_id, p.event_id
HAVING COUNT(A.TARGET_CONCEPT_ID) >= 1
-- End Correlated Criteria

  ) CQ on E.person_id = CQ.person_id and E.event_id = CQ.event_id
  GROUP BY E.person_id, E.event_id
  HAVING COUNT(index_id) = 1
) G
-- End Criteria Group
) AC on AC.person_id = pe.person_id and AC.event_id = pe.event_id

) A on A.person_id = P.person_id  AND A.START_DATE >= P.OP_START_DATE AND A.START_DATE <= P.OP_END_DATE AND A.START_DATE >= (P.START_DATE + -30*INTERVAL'1 day') AND A.START_DATE <= (P.START_DATE + 30*INTERVAL'1 day')
GROUP BY p.person_id, p.event_id
HAVING COUNT(A.TARGET_CONCEPT_ID) >= 1
-- End Correlated Criteria

  ) CQ on E.person_id = CQ.person_id and E.event_id = CQ.event_id
  GROUP BY E.person_id, E.event_id
  HAVING COUNT(index_id) = 1
) G
-- End Criteria Group
) AC on AC.person_id = pe.person_id AND AC.event_id = pe.event_id
) Results
;
ANALYZE Inclusion_11
;

CREATE TEMP TABLE Inclusion_12

AS
SELECT
12 as inclusion_rule_id, person_id, event_id

FROM
(
  select pe.person_id, pe.event_id
  FROM qualified_events pe
  
JOIN (
-- Begin Criteria Group
select 0 as index_id, person_id, event_id
FROM
(
  select E.person_id, E.event_id 
  FROM qualified_events E
  INNER JOIN
  (
    -- Begin Correlated Criteria
SELECT 0 as index_id, p.person_id, p.event_id
FROM qualified_events P
INNER JOIN
(
  select PE.person_id, PE.event_id, PE.start_date, PE.end_date, PE.target_concept_id, PE.visit_occurrence_id, PE.sort_date FROM (
-- Begin Visit Occurrence Criteria
select C.person_id, C.visit_occurrence_id as event_id, C.visit_start_date as start_date, C.visit_end_date as end_date,
       C.visit_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.visit_start_date as sort_date
from 
(
  select vo.* , row_number() over (PARTITION BY vo.person_id ORDER BY vo.visit_start_date, vo.visit_occurrence_id) as ordinal
  FROM cdm.VISIT_OCCURRENCE vo
JOIN Codesets codesets on ((vo.visit_concept_id = codesets.concept_id and codesets.codeset_id = 0) AND (vo.visit_source_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
JOIN cdm.CARE_SITE CS on C.care_site_id = CS.care_site_id
LEFT JOIN cdm.PROVIDER PR on C.provider_id = PR.provider_id
WHERE (C.visit_start_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.visit_start_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND (C.visit_end_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.visit_end_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.visit_type_concept_id  in (38000183,38000199)
AND ((CAST(C.visit_end_date AS DATE) - CAST(C.visit_start_date AS DATE)) >= 0 and (CAST(C.visit_end_date AS DATE) - CAST(C.visit_start_date AS DATE)) <= 99)
AND (EXTRACT(YEAR FROM C.visit_start_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.visit_start_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8532,8507)
AND PR.specialty_concept_id in (38004446,38003627)
AND CS.place_of_service_concept_id in (42628591,42628592)
AND C.ordinal = 1
-- End Visit Occurrence Criteria

) PE
JOIN (
-- Begin Criteria Group
select 0 as index_id, person_id, event_id
FROM
(
  select E.person_id, E.event_id 
  FROM (SELECT Q.person_id, Q.event_id, Q.start_date, Q.end_date, Q.visit_occurrence_id, OP.observation_period_start_date as op_start_date, OP.observation_period_end_date as op_end_date
FROM (-- Begin Visit Occurrence Criteria
select C.person_id, C.visit_occurrence_id as event_id, C.visit_start_date as start_date, C.visit_end_date as end_date,
       C.visit_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.visit_start_date as sort_date
from 
(
  select vo.* , row_number() over (PARTITION BY vo.person_id ORDER BY vo.visit_start_date, vo.visit_occurrence_id) as ordinal
  FROM cdm.VISIT_OCCURRENCE vo
JOIN Codesets codesets on ((vo.visit_concept_id = codesets.concept_id and codesets.codeset_id = 0) AND (vo.visit_source_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
JOIN cdm.CARE_SITE CS on C.care_site_id = CS.care_site_id
LEFT JOIN cdm.PROVIDER PR on C.provider_id = PR.provider_id
WHERE (C.visit_start_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.visit_start_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND (C.visit_end_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.visit_end_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.visit_type_concept_id  in (38000183,38000199)
AND ((CAST(C.visit_end_date AS DATE) - CAST(C.visit_start_date AS DATE)) >= 0 and (CAST(C.visit_end_date AS DATE) - CAST(C.visit_start_date AS DATE)) <= 99)
AND (EXTRACT(YEAR FROM C.visit_start_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.visit_start_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8532,8507)
AND PR.specialty_concept_id in (38004446,38003627)
AND CS.place_of_service_concept_id in (42628591,42628592)
AND C.ordinal = 1
-- End Visit Occurrence Criteria
) Q
JOIN cdm.OBSERVATION_PERIOD OP on Q.person_id = OP.person_id 
  and OP.observation_period_start_date <= Q.start_date and OP.observation_period_end_date >= Q.start_date
) E
  INNER JOIN
  (
    -- Begin Correlated Criteria
SELECT 0 as index_id, p.person_id, p.event_id
FROM (SELECT Q.person_id, Q.event_id, Q.start_date, Q.end_date, Q.visit_occurrence_id, OP.observation_period_start_date as op_start_date, OP.observation_period_end_date as op_end_date
FROM (-- Begin Visit Occurrence Criteria
select C.person_id, C.visit_occurrence_id as event_id, C.visit_start_date as start_date, C.visit_end_date as end_date,
       C.visit_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.visit_start_date as sort_date
from 
(
  select vo.* , row_number() over (PARTITION BY vo.person_id ORDER BY vo.visit_start_date, vo.visit_occurrence_id) as ordinal
  FROM cdm.VISIT_OCCURRENCE vo
JOIN Codesets codesets on ((vo.visit_concept_id = codesets.concept_id and codesets.codeset_id = 0) AND (vo.visit_source_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C
JOIN cdm.PERSON P on C.person_id = P.person_id
JOIN cdm.CARE_SITE CS on C.care_site_id = CS.care_site_id
LEFT JOIN cdm.PROVIDER PR on C.provider_id = PR.provider_id
WHERE (C.visit_start_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.visit_start_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND (C.visit_end_date >= TO_DATE(TO_CHAR(1900,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD') and C.visit_end_date <= TO_DATE(TO_CHAR(2099,'0000')||'-'||TO_CHAR(1,'00')||'-'||TO_CHAR(1,'00'), 'YYYY-MM-DD'))
AND C.visit_type_concept_id  in (38000183,38000199)
AND ((CAST(C.visit_end_date AS DATE) - CAST(C.visit_start_date AS DATE)) >= 0 and (CAST(C.visit_end_date AS DATE) - CAST(C.visit_start_date AS DATE)) <= 99)
AND (EXTRACT(YEAR FROM C.visit_start_date) - P.year_of_birth >= 0 and EXTRACT(YEAR FROM C.visit_start_date) - P.year_of_birth <= 99)
AND P.gender_concept_id in (8532,8507)
AND PR.specialty_concept_id in (38004446,38003627)
AND CS.place_of_service_concept_id in (42628591,42628592)
AND C.ordinal = 1
-- End Visit Occurrence Criteria
) Q
JOIN cdm.OBSERVATION_PERIOD OP on Q.person_id = OP.person_id 
  and OP.observation_period_start_date <= Q.start_date and OP.observation_period_end_date >= Q.start_date
) P
INNER JOIN
(
  -- Begin Visit Occurrence Criteria
select C.person_id, C.visit_occurrence_id as event_id, C.visit_start_date as start_date, C.visit_end_date as end_date,
       C.visit_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.visit_start_date as sort_date
from 
(
  select vo.* 
  FROM cdm.VISIT_OCCURRENCE vo

) C


-- End Visit Occurrence Criteria

) A on A.person_id = P.person_id  AND A.visit_occurrence_id = P.visit_occurrence_id
GROUP BY p.person_id, p.event_id
HAVING COUNT(A.TARGET_CONCEPT_ID) >= 1
-- End Correlated Criteria

  ) CQ on E.person_id = CQ.person_id and E.event_id = CQ.event_id
  GROUP BY E.person_id, E.event_id
  HAVING COUNT(index_id) = 1
) G
-- End Criteria Group
) AC on AC.person_id = pe.person_id and AC.event_id = pe.event_id

) A on A.person_id = P.person_id  AND A.START_DATE >= P.OP_START_DATE AND A.START_DATE <= P.OP_END_DATE AND A.START_DATE >= (P.START_DATE + -30*INTERVAL'1 day') AND A.START_DATE <= (P.START_DATE + 30*INTERVAL'1 day')
GROUP BY p.person_id, p.event_id
HAVING COUNT(A.TARGET_CONCEPT_ID) >= 1
-- End Correlated Criteria

  ) CQ on E.person_id = CQ.person_id and E.event_id = CQ.event_id
  GROUP BY E.person_id, E.event_id
  HAVING COUNT(index_id) = 1
) G
-- End Criteria Group
) AC on AC.person_id = pe.person_id AND AC.event_id = pe.event_id
) Results
;
ANALYZE Inclusion_12
;

CREATE TEMP TABLE Inclusion_13

AS
SELECT
13 as inclusion_rule_id, person_id, event_id

FROM
(
  select pe.person_id, pe.event_id
  FROM qualified_events pe
  
JOIN (
-- Begin Criteria Group
select 0 as index_id, person_id, event_id
FROM
(
  select E.person_id, E.event_id 
  FROM qualified_events E
  INNER JOIN
  (
    -- Begin Correlated Criteria
SELECT 0 as index_id, p.person_id, p.event_id
FROM qualified_events P
INNER JOIN
(
  -- Begin Condition Occurrence Criteria
SELECT C.person_id, C.condition_occurrence_id as event_id, C.condition_start_date as start_date, COALESCE(C.condition_end_date, (C.condition_start_date + 1*INTERVAL'1 day')) as end_date,
       C.CONDITION_CONCEPT_ID as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.condition_start_date as sort_date
FROM 
(
  SELECT co.* 
  FROM cdm.CONDITION_OCCURRENCE co
  JOIN Codesets codesets on ((co.condition_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C


-- End Condition Occurrence Criteria

) A on A.person_id = P.person_id  AND A.START_DATE >= P.OP_START_DATE AND A.START_DATE <= P.OP_END_DATE AND A.START_DATE >= (P.START_DATE + -100*INTERVAL'1 day') AND A.START_DATE <= (P.START_DATE + 100*INTERVAL'1 day')
GROUP BY p.person_id, p.event_id
HAVING COUNT(A.TARGET_CONCEPT_ID) >= 1
-- End Correlated Criteria

UNION ALL
-- Begin Criteria Group
select 1 as index_id, person_id, event_id
FROM
(
  select E.person_id, E.event_id 
  FROM qualified_events E
  INNER JOIN
  (
    -- Begin Correlated Criteria
SELECT 0 as index_id, p.person_id, p.event_id
FROM qualified_events P
INNER JOIN
(
  -- Begin Procedure Occurrence Criteria
select C.person_id, C.procedure_occurrence_id as event_id, C.procedure_date as start_date, (C.procedure_date + 1*INTERVAL'1 day') as END_DATE,
       C.procedure_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.procedure_date as sort_date
from 
(
  select po.* 
  FROM cdm.PROCEDURE_OCCURRENCE po
JOIN Codesets codesets on ((po.procedure_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C


-- End Procedure Occurrence Criteria

) A on A.person_id = P.person_id  AND A.START_DATE >= P.OP_START_DATE AND A.START_DATE <= P.OP_END_DATE AND A.START_DATE >= (P.START_DATE + -60*INTERVAL'1 day') AND A.START_DATE <= (P.START_DATE + 60*INTERVAL'1 day')
GROUP BY p.person_id, p.event_id
HAVING COUNT(A.TARGET_CONCEPT_ID) >= 1
-- End Correlated Criteria

UNION ALL
-- Begin Correlated Criteria
SELECT 1 as index_id, p.person_id, p.event_id
FROM qualified_events P
INNER JOIN
(
  -- Begin Drug Exposure Criteria
select C.person_id, C.drug_exposure_id as event_id, C.drug_exposure_start_date as start_date,
       COALESCE(C.DRUG_EXPOSURE_END_DATE, (DRUG_EXPOSURE_START_DATE + C.DAYS_SUPPLY*INTERVAL'1 day'), (C.DRUG_EXPOSURE_START_DATE + 1*INTERVAL'1 day')) as end_date,
       C.drug_concept_id as TARGET_CONCEPT_ID, C.visit_occurrence_id,
       C.drug_exposure_start_date as sort_date
from 
(
  select de.* 
  FROM cdm.DRUG_EXPOSURE de
JOIN Codesets codesets on ((de.drug_concept_id = codesets.concept_id and codesets.codeset_id = 0))
) C


-- End Drug Exposure Criteria

) A on A.person_id = P.person_id  AND A.START_DATE >= P.OP_START_DATE AND A.START_DATE <= P.OP_END_DATE AND A.START_DATE >= (P.START_DATE + -30*INTERVAL'1 day') AND A.START_DATE <= (P.START_DATE + 30*INTERVAL'1 day')
GROUP BY p.person_id, p.event_id
HAVING COUNT(A.TARGET_CONCEPT_ID) >= 1
-- End Correlated Criteria

  ) CQ on E.person_id = CQ.person_id and E.event_id = CQ.event_id
  GROUP BY E.person_id, E.event_id
  HAVING COUNT(index_id) > 0
) G
-- End Criteria Group

  ) CQ on E.person_id = CQ.person_id and E.event_id = CQ.event_id
  GROUP BY E.person_id, E.event_id
  HAVING COUNT(index_id) = 2
) G
-- End Criteria Group
) AC on AC.person_id = pe.person_id AND AC.event_id = pe.event_id
) Results
;
ANALYZE Inclusion_13
;

CREATE TEMP TABLE inclusion_events

AS
SELECT
inclusion_rule_id, person_id, event_id

FROM
(select inclusion_rule_id, person_id, event_id from Inclusion_0
UNION ALL
select inclusion_rule_id, person_id, event_id from Inclusion_1
UNION ALL
select inclusion_rule_id, person_id, event_id from Inclusion_2
UNION ALL
select inclusion_rule_id, person_id, event_id from Inclusion_3
UNION ALL
select inclusion_rule_id, person_id, event_id from Inclusion_4
UNION ALL
select inclusion_rule_id, person_id, event_id from Inclusion_5
UNION ALL
select inclusion_rule_id, person_id, event_id from Inclusion_6
UNION ALL
select inclusion_rule_id, person_id, event_id from Inclusion_7
UNION ALL
select inclusion_rule_id, person_id, event_id from Inclusion_8
UNION ALL
select inclusion_rule_id, person_id, event_id from Inclusion_9
UNION ALL
select inclusion_rule_id, person_id, event_id from Inclusion_10
UNION ALL
select inclusion_rule_id, person_id, event_id from Inclusion_11
UNION ALL
select inclusion_rule_id, person_id, event_id from Inclusion_12
UNION ALL
select inclusion_rule_id, person_id, event_id from Inclusion_13) I;
ANALYZE inclusion_events
;
TRUNCATE TABLE Inclusion_0;
DROP TABLE Inclusion_0;

TRUNCATE TABLE Inclusion_1;
DROP TABLE Inclusion_1;

TRUNCATE TABLE Inclusion_2;
DROP TABLE Inclusion_2;

TRUNCATE TABLE Inclusion_3;
DROP TABLE Inclusion_3;

TRUNCATE TABLE Inclusion_4;
DROP TABLE Inclusion_4;

TRUNCATE TABLE Inclusion_5;
DROP TABLE Inclusion_5;

TRUNCATE TABLE Inclusion_6;
DROP TABLE Inclusion_6;

TRUNCATE TABLE Inclusion_7;
DROP TABLE Inclusion_7;

TRUNCATE TABLE Inclusion_8;
DROP TABLE Inclusion_8;

TRUNCATE TABLE Inclusion_9;
DROP TABLE Inclusion_9;

TRUNCATE TABLE Inclusion_10;
DROP TABLE Inclusion_10;

TRUNCATE TABLE Inclusion_11;
DROP TABLE Inclusion_11;

TRUNCATE TABLE Inclusion_12;
DROP TABLE Inclusion_12;

TRUNCATE TABLE Inclusion_13;
DROP TABLE Inclusion_13;


CREATE TEMP TABLE included_events

AS
WITH cteIncludedEvents(event_id, person_id, start_date, end_date, op_start_date, op_end_date, ordinal)  AS (
  SELECT event_id, person_id, start_date, end_date, op_start_date, op_end_date, row_number() over (partition by person_id order by start_date ASC) as ordinal
  from
  (
    select Q.event_id, Q.person_id, Q.start_date, Q.end_date, Q.op_start_date, Q.op_end_date, SUM(coalesce(POWER(cast(2 as bigint), I.inclusion_rule_id), 0)) as inclusion_rule_mask
    from qualified_events Q
    LEFT JOIN inclusion_events I on I.person_id = Q.person_id and I.event_id = Q.event_id
    GROUP BY Q.event_id, Q.person_id, Q.start_date, Q.end_date, Q.op_start_date, Q.op_end_date
  ) MG -- matching groups

  -- the matching group with all bits set ( POWER(2,# of inclusion rules) - 1 = inclusion_rule_mask
  WHERE (MG.inclusion_rule_mask = POWER(cast(2 as bigint),14)-1)

)
 SELECT
event_id, person_id, start_date, end_date, op_start_date, op_end_date

FROM
cteIncludedEvents Results
WHERE Results.ordinal = 1
;
ANALYZE included_events
;



-- generate cohort periods into #final_cohort
CREATE TEMP TABLE cohort_rows

AS
WITH cohort_ends (event_id, person_id, end_date)  AS (
	-- cohort exit dates
  -- By default, cohort exit at the event's op end date
select event_id, person_id, op_end_date as end_date from included_events
),
first_ends (person_id, start_date, end_date) as
(
	select F.person_id, F.start_date, F.end_date
	FROM (
	  select I.event_id, I.person_id, I.start_date, E.end_date, row_number() over (partition by I.person_id, I.event_id order by E.end_date) as ordinal 
	  from included_events I
	  join cohort_ends E on I.event_id = E.event_id and I.person_id = E.person_id and E.end_date >= I.start_date
	) F
	WHERE F.ordinal = 1
)
 SELECT
person_id, start_date, end_date

FROM
first_ends;
ANALYZE cohort_rows
;

CREATE TEMP TABLE final_cohort

AS
WITH cteEndDates (person_id, end_date)  AS (	
	SELECT
		person_id
		, (event_date + -1 * 0*INTERVAL'1 day')  as end_date
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
				, (end_date + 0*INTERVAL'1 day') as end_date
				, 1 AS event_type
				, NULL
			FROM cohort_rows
		) RAWDATA
	) e
	WHERE (2 * e.start_ordinal) - e.overall_ord = 0
),
cteEnds (person_id, start_date, end_date) AS
(
	SELECT
		 c.person_id
		, c.start_date
		, MIN(e.end_date) AS end_date
	FROM cohort_rows c
	JOIN cteEndDates e ON c.person_id = e.person_id AND e.end_date >= c.start_date
	GROUP BY c.person_id, c.start_date
)
 SELECT
person_id, min(start_date) as start_date, end_date

FROM
cteEnds
group by person_id, end_date
;
ANALYZE final_cohort
;

DELETE FROM allCriteriaTest.cohort where cohort_definition_id = 1;
INSERT INTO allCriteriaTest.cohort (cohort_definition_id, subject_id, cohort_start_date, cohort_end_date)
select 1 as cohort_definition_id, person_id, start_date, end_date 
FROM final_cohort CO
;


-- Find the event that is the 'best match' per person.  
-- the 'best match' is defined as the event that satisfies the most inclusion rules.
-- ties are solved by choosing the event that matches the earliest inclusion rule, and then earliest.

CREATE TEMP TABLE best_events

AS
SELECT
q.person_id, q.event_id

FROM
qualified_events Q
join (
	SELECT R.person_id, R.event_id, ROW_NUMBER() OVER (PARTITION BY R.person_id ORDER BY R.rule_count DESC,R.min_rule_id ASC, R.start_date ASC) AS rank_value
	FROM (
		SELECT Q.person_id, Q.event_id, COALESCE(COUNT(DISTINCT I.inclusion_rule_id), 0) AS rule_count, COALESCE(MIN(I.inclusion_rule_id), 0) AS min_rule_id, Q.start_date
		FROM qualified_events Q
		LEFT JOIN inclusion_events I ON q.person_id = i.person_id AND q.event_id = i.event_id
		GROUP BY Q.person_id, Q.event_id, Q.start_date
	) R
) ranked on Q.person_id = ranked.person_id and Q.event_id = ranked.event_id
WHERE ranked.rank_value = 1
;
ANALYZE best_events
;

-- modes of generation: (the same tables store the results for the different modes, identified by the mode_id column)
-- 0: all events
-- 1: best event


-- BEGIN: Inclusion Impact Analysis - event
-- calculte matching group counts
delete from allCriteriaTest.cohort_inclusion_result where cohort_definition_id = 1 and mode_id = 0;
insert into allCriteriaTest.cohort_inclusion_result (cohort_definition_id, inclusion_rule_mask, person_count, mode_id)
select 1 as cohort_definition_id, inclusion_rule_mask, COUNT(*) as person_count, 0 as mode_id
from
(
  select Q.person_id, Q.event_id, CAST(SUM(coalesce(POWER(cast(2 as bigint), I.inclusion_rule_id), 0)) AS bigint) as inclusion_rule_mask
  from qualified_events Q
  LEFT JOIN inclusion_events I on q.person_id = i.person_id and q.event_id = i.event_id
  GROUP BY Q.person_id, Q.event_id
) MG -- matching groups
group by inclusion_rule_mask
;

-- calculate gain counts 
delete from allCriteriaTest.cohort_inclusion_stats where cohort_definition_id = 1 and mode_id = 0;
insert into allCriteriaTest.cohort_inclusion_stats (cohort_definition_id, rule_sequence, person_count, gain_count, person_total, mode_id)
select ir.cohort_definition_id, ir.rule_sequence, coalesce(T.person_count, 0) as person_count, coalesce(SR.person_count, 0) gain_count, EventTotal.total, 0 as mode_id
from allCriteriaTest.cohort_inclusion ir
left join
(
  select i.inclusion_rule_id, COUNT(i.event_id) as person_count
  from qualified_events Q
  JOIN inclusion_events i on Q.person_id = I.person_id and Q.event_id = i.event_id
  group by i.inclusion_rule_id
) T on ir.rule_sequence = T.inclusion_rule_id
CROSS JOIN (select count(*) as total_rules from allCriteriaTest.cohort_inclusion where cohort_definition_id = 1) RuleTotal
CROSS JOIN (select COUNT(event_id) as total from qualified_events) EventTotal
LEFT JOIN allCriteriaTest.cohort_inclusion_result SR on SR.mode_id = 0 AND SR.cohort_definition_id = 1 AND (POWER(cast(2 as bigint),RuleTotal.total_rules) - POWER(cast(2 as bigint),ir.rule_sequence) - 1) = SR.inclusion_rule_mask -- POWER(2,rule count) - POWER(2,rule sequence) - 1 is the mask for 'all except this rule'
WHERE ir.cohort_definition_id = 1
;

-- calculate totals
delete from allCriteriaTest.cohort_summary_stats where cohort_definition_id = 1 and mode_id = 0;
insert into allCriteriaTest.cohort_summary_stats (cohort_definition_id, base_count, final_count, mode_id)
select 1 as cohort_definition_id, PC.total as person_count, coalesce(FC.total, 0) as final_count, 0 as mode_id
FROM
(select COUNT(event_id) as total from qualified_events) PC,
(select sum(sr.person_count) as total
  from allCriteriaTest.cohort_inclusion_result sr
  CROSS JOIN (select count(*) as total_rules from allCriteriaTest.cohort_inclusion where cohort_definition_id = 1) RuleTotal
  where sr.mode_id = 0 and sr.cohort_definition_id = 1 and sr.inclusion_rule_mask = POWER(cast(2 as bigint),RuleTotal.total_rules)-1
) FC
;

-- END: Inclusion Impact Analysis - event

-- BEGIN: Inclusion Impact Analysis - person
-- calculte matching group counts
delete from allCriteriaTest.cohort_inclusion_result where cohort_definition_id = 1 and mode_id = 1;
insert into allCriteriaTest.cohort_inclusion_result (cohort_definition_id, inclusion_rule_mask, person_count, mode_id)
select 1 as cohort_definition_id, inclusion_rule_mask, COUNT(*) as person_count, 1 as mode_id
from
(
  select Q.person_id, Q.event_id, CAST(SUM(coalesce(POWER(cast(2 as bigint), I.inclusion_rule_id), 0)) AS bigint) as inclusion_rule_mask
  from best_events Q
  LEFT JOIN inclusion_events I on q.person_id = i.person_id and q.event_id = i.event_id
  GROUP BY Q.person_id, Q.event_id
) MG -- matching groups
group by inclusion_rule_mask
;

-- calculate gain counts 
delete from allCriteriaTest.cohort_inclusion_stats where cohort_definition_id = 1 and mode_id = 1;
insert into allCriteriaTest.cohort_inclusion_stats (cohort_definition_id, rule_sequence, person_count, gain_count, person_total, mode_id)
select ir.cohort_definition_id, ir.rule_sequence, coalesce(T.person_count, 0) as person_count, coalesce(SR.person_count, 0) gain_count, EventTotal.total, 1 as mode_id
from allCriteriaTest.cohort_inclusion ir
left join
(
  select i.inclusion_rule_id, COUNT(i.event_id) as person_count
  from best_events Q
  JOIN inclusion_events i on Q.person_id = I.person_id and Q.event_id = i.event_id
  group by i.inclusion_rule_id
) T on ir.rule_sequence = T.inclusion_rule_id
CROSS JOIN (select count(*) as total_rules from allCriteriaTest.cohort_inclusion where cohort_definition_id = 1) RuleTotal
CROSS JOIN (select COUNT(event_id) as total from best_events) EventTotal
LEFT JOIN allCriteriaTest.cohort_inclusion_result SR on SR.mode_id = 1 AND SR.cohort_definition_id = 1 AND (POWER(cast(2 as bigint),RuleTotal.total_rules) - POWER(cast(2 as bigint),ir.rule_sequence) - 1) = SR.inclusion_rule_mask -- POWER(2,rule count) - POWER(2,rule sequence) - 1 is the mask for 'all except this rule'
WHERE ir.cohort_definition_id = 1
;

-- calculate totals
delete from allCriteriaTest.cohort_summary_stats where cohort_definition_id = 1 and mode_id = 1;
insert into allCriteriaTest.cohort_summary_stats (cohort_definition_id, base_count, final_count, mode_id)
select 1 as cohort_definition_id, PC.total as person_count, coalesce(FC.total, 0) as final_count, 1 as mode_id
FROM
(select COUNT(event_id) as total from best_events) PC,
(select sum(sr.person_count) as total
  from allCriteriaTest.cohort_inclusion_result sr
  CROSS JOIN (select count(*) as total_rules from allCriteriaTest.cohort_inclusion where cohort_definition_id = 1) RuleTotal
  where sr.mode_id = 1 and sr.cohort_definition_id = 1 and sr.inclusion_rule_mask = POWER(cast(2 as bigint),RuleTotal.total_rules)-1
) FC
;

-- END: Inclusion Impact Analysis - person

-- BEGIN: Censored Stats

-- END: Censored Stats

TRUNCATE TABLE best_events;
DROP TABLE best_events;





TRUNCATE TABLE cohort_rows;
DROP TABLE cohort_rows;

TRUNCATE TABLE final_cohort;
DROP TABLE final_cohort;

TRUNCATE TABLE inclusion_events;
DROP TABLE inclusion_events;

TRUNCATE TABLE qualified_events;
DROP TABLE qualified_events;

TRUNCATE TABLE included_events;
DROP TABLE included_events;

TRUNCATE TABLE Codesets;
DROP TABLE Codesets;
