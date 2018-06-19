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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.ohdsi.circe.check.WarningSeverity;
import org.ohdsi.circe.check.utils.CriteriaNameHelper;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;
import org.ohdsi.circe.cohortdefinition.Window;

public class TimePatternCheck extends BaseCorelatedCriteriaCheck {

    private List<TimeWindowInfo> timeWindowInfoList = new ArrayList<>();

    @Override
    protected WarningSeverity defineSeverity() {

        return WarningSeverity.INFO;
    }

    @Override
    protected void checkCriteria(CorelatedCriteria criteria, String groupName, WarningReporter reporter) {

        String name = CriteriaNameHelper.getCriteriaName(criteria.criteria) + " criteria at " + groupName;
        timeWindowInfoList.add(new TimeWindowInfo(name, criteria.startWindow, criteria.endWindow));
    }

    @Override
    protected void afterCheck(WarningReporter reporter, CohortExpression expression) {

        List<Integer> startDays = timeWindowInfoList.stream().map(info -> startDays(info.start)).collect(Collectors.toList());
        Map<Integer, Long> freq = startDays.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        long maxFreq = freq.values().stream().mapToLong(v -> v).max().orElse(0);
        if (maxFreq > 1) {
            int mostCommon = freq.entrySet().stream().filter(en -> Objects.equals(en.getValue(), maxFreq)).map(Map.Entry::getKey).findFirst().orElse(0);
            timeWindowInfoList.stream().forEach(info -> {
                int start = startDays(info.start);
                long currFreq = freq.getOrDefault(start, 0L);
                if (maxFreq - currFreq > 0) {
                    reporter.add("%s time window breaks common pattern", info.getName());
                }
            });
        }
    }

    private Integer startDays(Window window) {
        return Objects.nonNull(window) && Objects.nonNull(window.start) ?
                (Objects.nonNull(window.start.days) ? window.start.days : 0) * window.start.coeff : 0;
    }

    class TimeWindowInfo {
        private String name;
        private Window start;
        private Window end;

        public TimeWindowInfo(String name, Window start, Window end) {

            this.name = name;
            this.start = start;
            this.end = end;
        }

        public String getName() {

            return name;
        }

        public Window getStart() {

            return start;
        }

        public Window getEnd() {

            return end;
        }
    }
}
