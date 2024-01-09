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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author cknoll1
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Window {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Endpoint {
    @JsonProperty("Days")
    public Integer days;

    @JsonProperty("Coeff")
    public int coeff;
    @JsonProperty("TimeUnitValue")
    public Integer timeUnitValue;

    @JsonProperty("TimeUnit")
    public String timeUnit = IntervalUnit.DAY.getName();
	}
  
  @JsonProperty("Start")
  public Endpoint start;  

  @JsonProperty("End")
  public Endpoint end;  

	@JsonProperty("UseIndexEnd")
	public Boolean useIndexEnd;

	@JsonProperty("UseEventEnd")
	public Boolean useEventEnd;
	
}
