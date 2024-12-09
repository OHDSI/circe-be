/*
 * Copyright 2020 cknoll1.
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
package org.ohdsi.circe.cohortdefinition.builders;

import org.ohdsi.circe.cohortdefinition.IntervalUnit;
import org.ohdsi.circe.cohortdefinition.Occurrence;
import org.ohdsi.circe.cohortdefinition.Window;

/**
 *
 * @author cknoll1
 */
public class CriteriaUtils {

  public static Window getPrior365Window() {
    Window prior365Window = new Window();  
    // index starts between 365d before
    Window.Endpoint startPoint = new Window.Endpoint();
    startPoint.coeff = -1;
    startPoint.days = 365;
    startPoint.timeUnitValue = null;
    prior365Window.start = startPoint;
    // ... and 0 days before
    Window.Endpoint endPoint = new Window.Endpoint();
    endPoint.coeff = -1;
    endPoint.days = 0;
    endPoint.timeUnitValue = 0;
    prior365Window.end = endPoint;
    return prior365Window;
  }

  public static Window getPrior365WindowTimeUnitInterval(String timeUnit) {
    Window prior365Window = new Window();
    // index starts between 365d before
    Window.Endpoint startPoint = new Window.Endpoint();
    startPoint.coeff = -1;
    startPoint.timeUnit = timeUnit;
    startPoint.timeUnitValue = getTimeUnitValue(timeUnit, 365);
    prior365Window.start = startPoint;
    // ... and 0 days before
    Window.Endpoint endPoint = new Window.Endpoint();
    endPoint.coeff = -1;
    endPoint.days = getTimeUnitValue(timeUnit, 0);
    prior365Window.end = endPoint;
    return prior365Window;
  }

  private static int getTimeUnitValue(String timeUnit, Integer timeUnitValueInDay) {
    int timeUnitValue = timeUnitValueInDay;
    if (IntervalUnit.HOUR.getName().equals(timeUnit)) {
      timeUnitValue = timeUnitValueInDay * 24;
    }
    if (IntervalUnit.MINUTE.getName().equals(timeUnit)) {
      timeUnitValue = timeUnitValueInDay * 24 * 60;
    }
    if (IntervalUnit.SECOND.getName().equals(timeUnit)) {
      timeUnitValue = timeUnitValueInDay * 24 * 60 * 60;
    }
    return timeUnitValue;
  }


  public static Window getAnyTimeWindow() {
    Window anytimeWindow = new Window();  
    // index starts between 365d before
    Window.Endpoint startPoint = new Window.Endpoint();
    startPoint.coeff = -1;
    anytimeWindow.start = startPoint;
    // ... and 0 days before
    Window.Endpoint endPoint = new Window.Endpoint();
    endPoint.coeff = 1;
    anytimeWindow.end = endPoint;
    return anytimeWindow;    
  }
  
  public static Occurrence getAtLeast1Occurrence() {
    Occurrence atLeast1 = new Occurrence();
    atLeast1.type = Occurrence.AT_LEAST;
    atLeast1.count = 1;
    
    return atLeast1;
  }

  public static Occurrence getAtExactly0Occurrence() {
    Occurrence exactly0 = new Occurrence();
    exactly0.type = Occurrence.EXACTLY;
    exactly0.count = 0;
    
    return exactly0;
  }
  
    public static Occurrence getAtExactly1Occurrence() {
    Occurrence exactly1 = new Occurrence();
    exactly1.type = Occurrence.EXACTLY;
    exactly1.count = 1;
    
    return exactly1;
  }

  public static Occurrence getDistinctCount(CriteriaColumn countCol, int type, int count) {
    Occurrence o = new Occurrence();
    o.type=type;
    o.count = count;
    o.isDistinct = true;
    o.countColumn = countCol;
    return o;
  }
  public static final String EVENT_TABLE_TEMPLATE = "(SELECT ROW_NUMBER() OVER (partition by E.subject_id order by E.cohort_start_date) AS event_id, " +
            "E.subject_id AS person_id, E.cohort_start_date AS start_date, E.cohort_end_date AS end_date, " +
            "OP.observation_period_start_date AS op_start_date, OP.observation_period_end_date AS op_end_date\n" +
            "FROM %s E\n" +
            "JOIN %s.observation_period OP ON E.subject_id = OP.person_id\n" +
            "  AND E.cohort_start_date >= OP.observation_period_start_date AND E.cohort_start_date <= OP.observation_period_end_date\n" +
            "WHERE E.cohort_definition_id = %d)";

}
