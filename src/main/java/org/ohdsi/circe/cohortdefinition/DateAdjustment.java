/*
 * Copyright 2022 cknoll1.
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
package org.ohdsi.circe.cohortdefinition;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author cknoll1
 */
public class DateAdjustment {

  public enum DateType {
    @JsonProperty("START_DATE")
    START_DATE,
    @JsonProperty("END_DATE")
    END_DATE
  };

  @JsonProperty("StartWith")
  public DateType startWith = DateType.START_DATE;
  @JsonProperty("StartOffset")
  public int startOffset = 0;

  @JsonProperty("EndWith")
  public DateType endWith = DateType.END_DATE;
 @JsonProperty("EndOffset")
  public int endOffset = 0;

}
