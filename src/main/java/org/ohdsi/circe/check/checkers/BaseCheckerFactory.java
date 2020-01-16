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
 *   Authors: Sergey Suvorov
 *
 */

package org.ohdsi.circe.check.checkers;

import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;
import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.DemographicCriteria;

import java.util.function.Consumer;

public abstract class BaseCheckerFactory {
    protected String groupName;

    protected WarningReporter reporter;

    protected BaseCheckerFactory(WarningReporter reporter, String groupName) {
        this.groupName = groupName;
        this.reporter = reporter;
    }

    protected abstract Consumer<Criteria> getCheck(Criteria criteria);

    protected abstract Consumer<DemographicCriteria> getCheck(DemographicCriteria criteria);

    public void check(Criteria criteria) {
        getCheck(criteria).accept(criteria);
    }

    public void check(DemographicCriteria criteria) {
        getCheck(criteria).accept(criteria);
    }

    public void check(CorelatedCriteria criteria) {
        getCheck(criteria.criteria).accept(criteria.criteria);
    }
}
