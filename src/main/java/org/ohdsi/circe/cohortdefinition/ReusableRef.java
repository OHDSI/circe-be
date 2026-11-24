/*
 *
 * Copyright 2026 Observational Health Data Sciences and Informatics
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.ohdsi.circe.cohortdefinition;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A use of a reusable imported into a cohort expression: points at an entry
 * of the expression's ReusableCriteria (from a criterion) or
 * ReusableCriteriaGroups (from a criteria group) by that entry's Id.
 *
 * The logic it stands for is also written out where it is used, so query
 * builders read the criterion or group as usual and ignore this reference.
 */
public class ReusableRef {

  @JsonProperty("Id")
  public Integer id;
}
