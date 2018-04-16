/*
 *   Copyright 2017 Observational Health Data Sciences and Informatics
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *   Authors: Vitaly Koulakov
 *
 */

package org.ohdsi.circe.check;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.ohdsi.circe.cohortdefinition.ConceptSet;

public class ConceptSetWarning extends BaseWarning implements Warning {

    private String template;
    private ConceptSet conceptSet;

    public ConceptSetWarning(WarningSeverity severity, String template, ConceptSet conceptSet) {

        super(severity);
        this.template = template;
        this.conceptSet = conceptSet;
    }

    @JsonIgnore
    public ConceptSet getConceptSet() {

        return conceptSet;
    }

    @JsonProperty("conceptSetId")
    public Integer getConceptSetId(){
        return Objects.nonNull(conceptSet) ? conceptSet.id : 0;
    }

    @Override
    public String toMessage() {

        return String.format(template, conceptSet.name);
    }
}
