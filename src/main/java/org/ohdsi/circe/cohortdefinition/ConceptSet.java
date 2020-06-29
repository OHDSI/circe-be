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

import java.util.Objects;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;

/**
 *
 * @author cknoll1
 */
public class ConceptSet {
  
  public int id;
  public String name;
  public ConceptSetExpression expression;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConceptSet)) {
            return false;
        }
        ConceptSet other = (ConceptSet) o;
        return Objects.equals(id, other.id) && Objects.equals(name, other.name) && Objects.equals(expression, other.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, expression);
    }
}
