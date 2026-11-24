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
 * A criteria group reusable imported into a cohort expression, the way a
 * concept set is. It holds the reusable's logic and the concept sets that
 * logic uses, numbered in the reusable's own id space.
 */
public class ReusableCriteriaGroup {

  /** Unique within the expression; what a ReusableRef points at. */
  @JsonProperty("Id")
  public Integer id;

  /** The reusable's id in the repository it was imported from. */
  @JsonProperty("RefId")
  public Integer refId;

  /** The version of the reusable this entry holds. */
  @JsonProperty("Version")
  public Integer version;

  @JsonProperty("Name")
  public String name;

  @JsonProperty("ConceptSets")
  public ConceptSet[] conceptSets;

  @JsonProperty("CriteriaGroup")
  public CriteriaGroup criteriaGroup;
}
