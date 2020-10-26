/*
 * Copyright 2020 cknoll1.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ohdsi.circe.cohortdefinition.builders;

/**
 *
 * @author cknoll1
 */
public enum CriteriaColumn {
  DAYS_SUPPLY("days_supply"),
  DOMAIN_CONCEPT("domain_concept_id"),
  DOMAIN_SOURCE_CONCEPT("domain_source_concept_id"),
  END_DATE("end_date"),
  ERA_OCCURRENCES("occurrence_count"),
  GAP_DAYS("gap_days"),
  QUANTITY("quantity"),
  RANGE_HIGH("range_high"),
  RANGE_LOW("range_low"),
  REFILLS("refills"),
  START_DATE("start_date"),
  UNIT("unit_concept_id"),
  VALUE_AS_NUMBER("value_as_number");

  private final String columnName;

  CriteriaColumn(String columnName) {
    this.columnName = columnName;
  }

  public String columnName() {
    return this.columnName;
  }
}
