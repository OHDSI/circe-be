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
 * Authors: Pavel Grafkin
 *
 */
package org.ohdsi.circe.cohortdefinition;

import java.util.Map;

import org.ohdsi.circe.cohortdefinition.builders.ColumnFieldData;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WindowedCriteria {
  @JsonProperty("Criteria")
  public Criteria criteria;
  
  @JsonProperty("StartWindow")
  public Window startWindow;  

  @JsonProperty("EndWindow")
  public Window endWindow;
  
  @JsonProperty("RestrictVisit")
  public boolean restrictVisit = false;

  @JsonProperty("IgnoreObservationPeriod")
  public boolean ignoreObservationPeriod = false;
    
  // Map contain all distinct field for UNION ALL
  public Map<String, ColumnFieldData> mapDistinctField;
}
