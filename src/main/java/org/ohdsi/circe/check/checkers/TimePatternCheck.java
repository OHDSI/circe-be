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
            TimeWindowInfo mostCommonInfo = timeWindowInfoList.stream().filter(ti -> {
                long currFreq = freq.getOrDefault(startDays(ti.start), 0L);
                return currFreq == maxFreq;
            }).findFirst().orElse(null);
            if (Objects.nonNull(mostCommonInfo)) {
                timeWindowInfoList.forEach(info -> {
                    int start = startDays(info.start);
                    long currFreq = freq.getOrDefault(start, 0L);
                    if (maxFreq - currFreq > 0) {
                        reporter.add("%s time window differs from most common pattern prior '%s', shouldn't that be a valid pattern?",
                                info.getName(), formatTimeWindow(mostCommonInfo));
                    }
                });
            }
        }
    }

    private String formatTimeWindow(TimeWindowInfo ti) {
        String result = "";
        if (ti != null && ti.start != null && ti.start.start != null) {
            result += formatDays(ti.start.start) + " days " + formatCoeff(ti.start.start);
        }
        if (ti != null && ti.start != null && ti.start.end != null) {
            result += " and " + formatDays(ti.start.end) + " days " + formatCoeff(ti.start.end);
        }
        return result;
    }

    private String formatDays(Window.Endpoint endpoint) {
        return Objects.nonNull(endpoint.timeUnitValue) ? String.valueOf(endpoint.timeUnitValue) : "all";
    }

    private String formatCoeff(Window.Endpoint endpoint) {
        return endpoint.coeff < 0 ? "before " : "after ";
    }

    private Integer startDays(Window window) {
        return Objects.nonNull(window) && Objects.nonNull(window.start) ?
                (Objects.nonNull(window.start.timeUnitValue) ? window.start.timeUnitValue : 0) * window.start.coeff : 0;
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
