/*
 *
 * Copyright 2017 Observational Health Data Sciences and Informatics
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
 * Authors: Christopher Knoll
 *
 */
package org.ohdsi.circe.cohortdefinition;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.circe.cohortdefinition.builders.CriteriaColumn;

/**
 *
 * @author cknoll1
 */
public class Occurrence {
	public static final int EXACTLY = 0;
	public static final int AT_MOST = 1;
	public static final int AT_LEAST = 2;
	
  @JsonProperty("Type")
  public int type;

  @JsonProperty("Count")
  public int count;
  
  @JsonProperty("IsDistinct")
  public boolean isDistinct;
  
  @JsonProperty("CountColumn")
  public CriteriaColumn countColumn;
}
