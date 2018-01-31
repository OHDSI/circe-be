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
package org.ohdsi.circe.vocabulary;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * A Class that encapsulates the elements of a Concept Set Expression.
 */
public class ConceptSetExpression {
	
  private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
	
  public static class ConceptSetItem
  {
    public Concept concept;
    public boolean isExcluded;
    public boolean includeDescendants;
    public boolean includeMapped;
  }

  public ConceptSetItem[] items;
  
	public ConceptSetExpression fromJson(String json) {
		try {
			ConceptSetExpression expression = JSON_MAPPER.readValue(json, ConceptSetExpression.class);
			return expression;
		} catch (Exception e) {
			throw new RuntimeException("Error parsing conceptset expression", e);
		}
	}
}
