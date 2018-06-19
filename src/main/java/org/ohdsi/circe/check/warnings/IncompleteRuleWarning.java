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

package org.ohdsi.circe.check.warnings;

import org.ohdsi.circe.check.WarningSeverity;

public class IncompleteRuleWarning extends BaseWarning {

    private static final String INCOMPLETE_ERROR = "Inclusion criteria %s is opened but no criteria entered within";

    private final String ruleName;

    public IncompleteRuleWarning(WarningSeverity severity, String ruleName) {

        super(severity);
        this.ruleName = ruleName;
    }

    public String getRuleName() {

        return ruleName;
    }

    @Override
    public String toMessage() {

        return String.format(INCOMPLETE_ERROR, ruleName);
    }
}
