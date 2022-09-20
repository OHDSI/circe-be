CREATE TABLE @schemaName.cohort (
  COHORT_DEFINITION_ID int NOT NULL,
	SUBJECT_ID bigint NOT NULL,
	cohort_start_date date NOT NULL,
	cohort_end_date date NOT NULL
);

CREATE TABLE @schemaName.cohort_inclusion (
  cohort_definition_id int NOT NULL,
  rule_sequence int NOT NULL,
  name varchar(255) NULL,
  description varchar(1000) NULL
);

CREATE TABLE @schemaName.cohort_inclusion_result (
  cohort_definition_id int NOT NULL,
  mode_id int NOT NULL,
  inclusion_rule_mask bigint NOT NULL,
  person_count bigint NOT NULL
);

CREATE TABLE @schemaName.cohort_inclusion_stats (
  cohort_definition_id int NOT NULL,
  rule_sequence int NOT NULL,
  mode_id int NOT NULL,
  person_count bigint NOT NULL,
  gain_count bigint NOT NULL,
  person_total bigint NOT NULL
);

CREATE TABLE @schemaName.cohort_summary_stats (
  cohort_definition_id int NOT NULL,
  mode_id int NOT NULL,
  base_count bigint NOT NULL,
  final_count bigint NOT NULL
);

CREATE TABLE @schemaName.cohort_censor_stats (
  cohort_definition_id int NOT NULL,
  lost_count BIGINT NOT NULL
);

CREATE TABLE @schemaName.codesets (
  codeset_id int NOT NULL,
  concept_id int NOT NULL
);
