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

import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.ohdsi.circe.cohortdefinition.*;

/**
 *
 * @author cknoll1
 */
public class CriteriaColumn_5_0_0_Test {
  @Rule
  public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void invalidConditionEra() {
      exceptionRule.expect(IllegalArgumentException.class);
      CriteriaSqlBuilder<ConditionEra> builder = new ConditionEraSqlBuilder<>();
      BuilderOptions options = new BuilderOptions();
      options.additionalColumns=Arrays.asList(CriteriaColumn.DAYS_SUPPLY);
      builder.getCriteriaSql(new ConditionEra(), options);
    }

    @Test
    public void invalidConditionOccurrence() {
      exceptionRule.expect(IllegalArgumentException.class);
      CriteriaSqlBuilder<ConditionOccurrence> builder = new ConditionOccurrenceSqlBuilder<>();
      BuilderOptions options = new BuilderOptions();
      options.additionalColumns=Arrays.asList(CriteriaColumn.DAYS_SUPPLY);
      builder.getCriteriaSql(new ConditionOccurrence(), options);
    }

    @Test
    public void invalidDeath() {
      exceptionRule.expect(IllegalArgumentException.class);
      CriteriaSqlBuilder<Death> builder = new DeathSqlBuilder<>();
      BuilderOptions options = new BuilderOptions();
      options.additionalColumns=Arrays.asList(CriteriaColumn.DAYS_SUPPLY);
      builder.getCriteriaSql(new Death(), options);
    }

    @Test
    public void invalidDeviceExposure() {
      exceptionRule.expect(IllegalArgumentException.class);
      CriteriaSqlBuilder<DeviceExposure> builder = new DeviceExposureSqlBuilder<>();
      BuilderOptions options = new BuilderOptions();
      options.additionalColumns=Arrays.asList(CriteriaColumn.DAYS_SUPPLY);
      builder.getCriteriaSql(new DeviceExposure(), options);
    }

    @Test
    public void invalidDoseEra() {
      exceptionRule.expect(IllegalArgumentException.class);
      CriteriaSqlBuilder<DoseEra> builder = new DoseEraSqlBuilder<>();
      BuilderOptions options = new BuilderOptions();
      options.additionalColumns=Arrays.asList(CriteriaColumn.DAYS_SUPPLY);
      builder.getCriteriaSql(new DoseEra(), options);
    }

    @Test
    public void invalidDrugEra() {
      exceptionRule.expect(IllegalArgumentException.class);
      CriteriaSqlBuilder<DrugEra> builder = new DrugEraSqlBuilder<>();
      BuilderOptions options = new BuilderOptions();
      options.additionalColumns=Arrays.asList(CriteriaColumn.DAYS_SUPPLY);
      builder.getCriteriaSql(new DrugEra(), options);
    }

    @Test
    public void invalidDrugExposure() {
      exceptionRule.expect(IllegalArgumentException.class);
      CriteriaSqlBuilder<DrugExposure> builder = new DrugExposureSqlBuilder<>();
      BuilderOptions options = new BuilderOptions();
      options.additionalColumns=Arrays.asList(CriteriaColumn.VALUE_AS_NUMBER);
      builder.getCriteriaSql(new DrugExposure(), options);
    }

    @Test
    public void invalidMeasurement() {
      exceptionRule.expect(IllegalArgumentException.class);
      CriteriaSqlBuilder<Measurement> builder = new MeasurementSqlBuilder<>();
      BuilderOptions options = new BuilderOptions();
      options.additionalColumns=Arrays.asList(CriteriaColumn.DAYS_SUPPLY);
      builder.getCriteriaSql(new Measurement(), options);
    }

    @Test
    public void invalidObservation() {
      exceptionRule.expect(IllegalArgumentException.class);
      CriteriaSqlBuilder<Observation> builder = new ObservationSqlBuilder<>();
      BuilderOptions options = new BuilderOptions();
      options.additionalColumns=Arrays.asList(CriteriaColumn.DAYS_SUPPLY);
      builder.getCriteriaSql(new Observation(), options);
    }

    @Test
    public void invalidObservationPeriod() {
      exceptionRule.expect(IllegalArgumentException.class);
      CriteriaSqlBuilder<ObservationPeriod> builder = new ObservationPeriodSqlBuilder<>();
      BuilderOptions options = new BuilderOptions();
      options.additionalColumns=Arrays.asList(CriteriaColumn.DAYS_SUPPLY);
      builder.getCriteriaSql(new ObservationPeriod(), options);
    }

    @Test
    public void invalidProcedureOccurrence() {
      exceptionRule.expect(IllegalArgumentException.class);
      CriteriaSqlBuilder<ProcedureOccurrence> builder = new ProcedureOccurrenceSqlBuilder<>();
      BuilderOptions options = new BuilderOptions();
      options.additionalColumns=Arrays.asList(CriteriaColumn.DAYS_SUPPLY);
      builder.getCriteriaSql(new ProcedureOccurrence(), options);
    }

    @Test
    public void invalidSpecimen() {
      exceptionRule.expect(IllegalArgumentException.class);
      CriteriaSqlBuilder<Specimen> builder = new SpecimenSqlBuilder<>();
      BuilderOptions options = new BuilderOptions();
      options.additionalColumns=Arrays.asList(CriteriaColumn.DAYS_SUPPLY);
      builder.getCriteriaSql(new Specimen(), options);
    }

    @Test
    public void invalidVisitOccurrence() {
      exceptionRule.expect(IllegalArgumentException.class);
      CriteriaSqlBuilder<VisitOccurrence> builder = new VisitOccurrenceSqlBuilder<>();
      BuilderOptions options = new BuilderOptions();
      options.additionalColumns=Arrays.asList(CriteriaColumn.DAYS_SUPPLY);
      builder.getCriteriaSql(new VisitOccurrence(), options);
    }

    @Test
    public void invalidVisitDetail() {
      exceptionRule.expect(IllegalArgumentException.class);
      CriteriaSqlBuilder<VisitDetail> builder = new VisitDetailSqlBuilder<>();
      BuilderOptions options = new BuilderOptions();
      options.additionalColumns=Arrays.asList(CriteriaColumn.DAYS_SUPPLY);
      builder.getCriteriaSql(new VisitDetail(), options);
    }
}
