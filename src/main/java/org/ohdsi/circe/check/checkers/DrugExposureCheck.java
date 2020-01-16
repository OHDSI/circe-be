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

package org.ohdsi.circe.check.checkers;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.DrugExposure;

import java.util.Objects;

import static org.ohdsi.circe.check.operations.Operations.match;

public class DrugExposureCheck extends BaseCriteriaCheck {

    private static final String EMPTY_VALUE_ERROR = "Drug exposure of %s contains empty %s";

    @Override
    protected void checkCriteria(Criteria criteria, String groupName, WarningReporter reporter) {

        WarningReporterHelper helper = new WarningReporterHelper(reporter, EMPTY_VALUE_ERROR, groupName);
        match(criteria)
                .isA(DrugExposure.class)
                .then(c -> match((DrugExposure)c)
                        .when(drugExposure -> Objects.nonNull(drugExposure.lotNumber) && StringUtils.isBlank(drugExposure.lotNumber.text))
                        .then(helper.addWarning("lot number")));
    }
}
