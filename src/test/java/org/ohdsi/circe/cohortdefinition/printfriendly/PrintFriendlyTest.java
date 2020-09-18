package org.ohdsi.circe.cohortdefinition.printfriendly;

import java.io.File;
import java.io.FileWriter;
import static java.lang.String.format;
import java.util.regex.Pattern;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.Assert.assertThat;
import org.junit.Ignore;
import org.junit.Test;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.helper.ResourceHelper;

public class PrintFriendlyTest {

  private final String OUTPUT_PATH = "C:\\Documents\\OHDSI\\Circe\\printFriendly\\";

  private final MarkdownRender pf = new MarkdownRender();

  private Pattern buildPattern(String regex) {
    return Pattern.compile(regex, Pattern.DOTALL);
  }

  @Test
  @Ignore
  public void processExpression() {
    CohortExpression expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/printfriendly/allAttributes.json"));
    String markdown = pf.generate(expression);
    System.out.println("Markdown:");
    System.out.println("=====================================");
    System.out.println(markdown);

    Parser parser = Parser.builder().build();
    Node document = parser.parse(markdown);
    HtmlRenderer renderer = HtmlRenderer.builder().build();
    String html = renderer.render(document);  // "<p>This is <em>Sparta</em></p>\n"		
    System.out.println("HTML:");
    System.out.println("=====================================");
    System.out.println(html);

    try {
      FileWriter mdWriter = new FileWriter(new File(OUTPUT_PATH + "sampleOutput.md"), false);
      mdWriter.write(markdown);
      mdWriter.close();

      FileWriter htmlWriter = new FileWriter(new File(OUTPUT_PATH + "sampleOutput.html"), false);
      htmlWriter.write(html);
      htmlWriter.close();

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // example regex assert
  //assertThat(markdown, matchesPattern(buildPattern("1\\. condition era of \"Concept Set 1\" for the first time in the person's history,")));
  @Test
  public void conditionEraTest() {
    CohortExpression expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/printfriendly/conditionEra.json"));
    String markdown = pf.generate(expression);
    assertThat(markdown, stringContainsInOrder(
            // concept set name and first in history attribute
            "1. condition era of \"Concept Set 1\" for the first time in the person's history,",
            // age/gender criteria
            "who are male &lt; 30 years old at era start and &lt;= 40 years old at era end;",
            // age at start and age at end
            "starting before January 1, 2010 and ending before December 31, 2014;",
            // era length
            "era length is &gt; 15 days",
            // occurrence count
            "containing between 1 and 5 occurrences",
            // nested criteria
            "having no condition eras of \"Concept Set 2\"",
            "starting between 90 days before and 30 days after \"Concept Set 1\" start date and ending between 7 days after and 90 days after \"Concept Set 1\" start date",
            // inclusion rules
            "#### 1. Inclusion Rule 1",
            "having at least 1 condition era of \"Concept Set 3\" for the first time in the person's history",
            "starting between 90 days before and 0 days before \"cohort entry\" start date"
    ));
  }

  @Test
  public void conditionOccurrenceTest() {
    CohortExpression expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/printfriendly/conditionOccurrence.json"));
    String markdown = pf.generate(expression);
    assertThat(markdown, stringContainsInOrder(
            // concept set name and first in history attribute
            "1. condition occurrence of \"Concept Set 1\" (including \"Concept Set 2\" source concepts) for the first time in the person's history,",
            // age/gender criteria
            "who are male or female, &gt;= 18 years old;",
            // age at start and age at end
            "starting before January 1, 2010 and ending after June 1, 2016;",
            // condition type
            "a condition type that is not: \"admission note\" or \"ancillary report\"",
            // stop reason
            "with a stop reason containing \"some stop reason\"",
            //provider specialty
            "a provider specialty that is: \"rheumatology\"",
            // visit
            "a visit occurrence that is: \"emergency room visit\" or \"inpatient visit\"",
            // nested criteria
            "with any of the following criteria:",
            "1. with the following event criteria: who are male &gt;= 18 years old",
            "2. having at least 1 condition occurrence of \"Concept Set 1\", who are female &lt; 30 years old",
            "starting  1 days after \"Concept Set 1\" start date",
            // inclusion rules
            "#### 1. Inclusion Rule 1",
            "having at least 1 condition occurrence of \"Concept Set 3\" for the first time in the person's history ",
            "starting between all days before and 1 days after \"cohort entry\" start date"
    ));
  }
  
  @Test
  public void deathTest() {
    CohortExpression expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/printfriendly/death.json"));
    String markdown = pf.generate(expression);
    assertThat(markdown, stringContainsInOrder(
            // concept set name and first in history attribute
            "1. death of \"Concept Set 1\" (including \"Concept Set 2\" source concepts),",
            // age/gender criteria
            "who are female &lt; 18 years old;",
            // age at start and age at end
            "starting on or after January 1, 2010",
            // nested criteria
            "having no death of \"Concept Set 3\"",
            "starting anytime prior to \"Concept Set 1\" start date",
            // inclusion rules
            "#### 1. Inclusion Rule 1",
            "having at least 1 death of \"Concept Set 3\", who are &gt; 12 years old"
    ));
  }

  @Test
  public void deviceExposureTest() {
    CohortExpression expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/printfriendly/deviceExposure.json"));
    String markdown = pf.generate(expression);
    assertThat(markdown, stringContainsInOrder(
            // concept set name and first in history attribute
            "1. device exposures of \"Concept Set 1\" (including \"Concept Set 2\" source concepts),",
            // occurrence start/end dates
            "starting before January 1, 2010 and ending after December 31, 2010;",
            // device type
            "a device type that is: \"admission note\" or \"ancillary report\";",
            //quantity
            "quantity &lt; 8;",
            // provider specialty
            "a provider specialty that is: \"rheumatology\" or \"rheumatology\";",
            // visit
            "a visit occurrence that is: \"emergency room visit\" or \"inpatient visit\";",
            // nested criteria
            "having at least 1 device exposure of \"Concept Set 2\" for the first time in the person's history,",
            "who are female or male, between 12 and 18 years old",
            "starting between all days before and 1 days after \"Concept Set 1\" start date",
            // entry restriction
            "Restrict entry events to having at least 1 device exposure of \"Concept Set 3\" for the first time in the person's history",
            "starting anytime prior to \"cohort entry\" start date",
            // inclusion rules
            "#### 1. Inclusion Rule 1",
            "having at least 1 device exposure of \"Concept Set 3\" for the first time in the person's history",
            "starting between 30 days before and 30 days after \"cohort entry\" start date"
    ));
  }

  @Test
  public void doseEraTest() {
    CohortExpression expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/printfriendly/doseEra.json"));
    String markdown = pf.generate(expression);
    assertThat(markdown, stringContainsInOrder(
            // concept set name and first in history attribute
            "1. dose era of \"Concept Set 1\" for the first time in the person's history,",
            // age/gender criteria
            "who are female or male, &gt; 18 years old at era start and &lt; 30 years old at era end;",
            // age at start and age at end
            "starting before January 1, 2010 and ending after January 1, 2011;",
            // unit
            "unit is: \"per gram\" or \"per deciliter\";",
            // era length
            "with era length &gt; 10 days;",
            // dose value
            "with dose value between 15 and 45;",
            // nested criteria
            "with any of the following criteria:",
            "1. having at least 1 dose era of \"Concept Set 2\" for the first time in the person's history",
            "starting between 30 days before and 0 days after \"Concept Set 1\" start date",
            "2. having at least 1 dose era of \"Concept Set 3\"",
            "starting in the 30 days prior to \"Concept Set 1\" start date",
            // inital event restriction
            "Restrict entry events to with all of the following criteria:",
            "1. having at least 1 dose era of \"Concept Set 2\" for the first time in the person's history",
            "starting between 60 days before and 0 days after \"cohort entry\" start date",
            "2. having at least 1 dose era of \"Concept Set 3\"",
            "starting in the 60 days prior to \"cohort entry\" start date",
            // inclusion rules
            "#### 1. Inclusion Rule 1",
            "with all of the following criteria:",
            "1. having at least 1 dose era of \"Concept Set 3\" for the first time in the person's history",
            "starting anytime on or before \"cohort entry\" start date",
            "2. having no dose eras of \"Concept Set 2\", who are &gt; 18 years old",
            "starting anytime prior to \"cohort entry\" start date"
    ));
  }

  @Test
  public void drugEraTest() {
    CohortExpression expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/printfriendly/drugEra.json"));
    String markdown = pf.generate(expression);
    assertThat(markdown, stringContainsInOrder(
            // concept set name and first in history attribute
            "1. drug era of \"Concept Set 1\" for the first time in the person's history,",
            // age/gender criteria
            "who are female or male, &gt;= 18 years old at era start and &lt;= 64 years old at era end;",
            // start date/end date
            "starting before February 1, 2014 and ending after April 1, 2014;",
            // era length
            "with era length &gt; 90 days;",
            // occurrence count 
            "with occurrence count between 4 and 6;",
            // nested criteria
            "with all of the following criteria:",
            "1. having at least 1 drug era of \"Concept Set 2\" for the first time in the person's history",
            "starting anytime prior to \"Concept Set 1\" start date",
            "2. having at least 1 drug era of \"Concept Set 3\", starting on or after January 1, 2010",
            // inclusion rules
            "#### 1. Inclusion Rule 1",
            "having at least 1 drug era of \"Concept Set 3\" for the first time in the person's history",
            "starting between 0 days before and all days after \"cohort entry\" start date"
    ));
  }

  @Test
  public void drugExposureTest() {
    CohortExpression expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/printfriendly/drugExposure.json"));
    String markdown = pf.generate(expression);
    assertThat(markdown, stringContainsInOrder(
            // concept set name and first in history attribute
            "1. drug exposure of \"Concept Set 1\" (including \"Concept Set 2\" source concepts) for the first time in the person's history,",
            // age/gender criteria
            "who are female or male, &gt; 18 years old;",
            // start date/end date
            "starting after January 1, 2010 and ending before January 1, 2016;",
            // drug type
            "a drug type that is: \"admission note\" or \"ancillary report\";",
            // refills 
            "with refills = 2;",
            // quantity 
            "with quantity &gt;= 15;",
            // days supply 
            "with days supply &lt; 30 days;",
            // effective drug dose 
            "with effective drug dose &lt; 15;",
            // dose unit 
            "dose unit: \"per 24 hours\";",
            // route 
            "with route: \"nasal\" or \"oral\";",
            // lot number
            "lot number containing \"12345\";",
            // stop reason
            "with a stop reason starting with \"some reason\";",
            // provider specialty
            "a provider specialty that is: \"general practice\" or \"urology\";",
            // visit
            "a visit occurrence that is: \"emergency room visit\" or \"inpatient visit\";",
            // nested criteria
            "with all of the following criteria:",
            "1. having at least 1 drug exposure of \"Concept Set 2\"",
            "starting anytime prior to \"Concept Set 1\" start date",
            "2. having at least 1 drug exposure of \"Concept Set 3\"",
            "starting between 14 days before and 0 days before \"Concept Set 1\" start date"
    ));
  }

  @Test
  public void measurementTest() {
    CohortExpression expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/printfriendly/measurement.json"));
    String markdown = pf.generate(expression);
    assertThat(markdown, stringContainsInOrder(
            // concept set name and first in history attribute
            "1. measurement of \"Concept Set 1\" (including \"Concept Set 2\" source concepts) for the first time in the person's history,",
            // age/gender criteria
            "who are female or male, &gt; 18 years old;",
            // start date/end date
            "starting on or after January 1, 2016;",
            // measurement type
            "a measurement type that is: \"admission note\" or \"ancillary report\";",
            // operator 
            "with operator: \"=\" or \"<=\";",
            // value as number 
            "numeric value between 5 and 10;",
            // unit 
            "unit: \"per billion\";",
            // value as concept 
            "with value as concept: \"good\" or \"significant change\";",
            // low range 
            "low range &gt; 10;",
            // high range 
            "high range &gt; 20;",
            // low ratio
            "low range-to-value ratio &gt; 1.2",
            // high ratio
            "high range-to-value ratio &gt; 0.9;",
            // abnormal result
            "with an abormal result (measurement value falls outside the low and high range)",
            // provider specialty
            "a provider specialty that is: \"gastroenterology\" or \"urology\";",
            // visit
            "a visit occurrence that is: \"emergency room visit\" or \"inpatient visit\";",
            // nested criteria
            "with all of the following criteria:",
            "having at least 1 measurement of \"Concept Set 2\" for the first time in the person's history",
            "starting anytime on or before \"Concept Set 1\" start date",
            "2. having at least 1 measurement of \"Concept Set 3\" ",
            "starting between 0 days before and all days after \"Concept Set 1\" start date"
    ));
  }

  @Test
  public void observationTest() {
    CohortExpression expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/printfriendly/observation.json"));
    String markdown = pf.generate(expression);
    assertThat(markdown, stringContainsInOrder(
            // concept set name and first in history attribute
            "1. observation of \"Concept Set 1\" for the first time in the person's history,",
            // age/gender criteria
            "who are female or male, &gt; 18 years old;",
            // start date/end date
            "starting on or after October 1, 2015;",
            // observation type
            "an observation type that is: \"condition procedure\" or \"discharge summary\";",
            // value as number 
            "numeric value &lt; 30;",
            // unit 
            "unit: \"per hundred\";",
            // value as concept 
            "with value as concept: \"positive\" or \"good\";",
            // value as string 
            "with value as string ending with \"obs value suffix\";",
            // qualifier 
            "with qualifier: \"total charge\";",
            // provider specialty
            "a provider specialty that is: \"health profession\" or \"psychologist\";",
            // visit
            "a visit occurrence that is: \"emergency room visit\" or \"inpatient visit\";",
            // nested criteria
            "having no observation of \"Concept Set 2\" for the first time in the person's history",
            "starting anytime prior to \"Concept Set 1\" start date"
    ));
  }

  @Test
  public void observationPeriodTest() {
    CohortExpression expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/printfriendly/observationPeriod_1.json"));
    String markdown = pf.generate(expression);
    assertThat(markdown, stringContainsInOrder(
            // concept set name and first in history attribute
            "1. observation period (first obsrvation period in person's history),",
            // age/gender criteria
            "who are &gt; 18 years old at era start and &lt; 32 years old at era end;",
            // start date/end date
            "starting before January 1, 2014 and ending after December 31, 2014;",
            // user-defined start/end
            "a user defiend start date of January 1, 2014 and end date of December 31, 2014;",
            // period type 
            "period type is: \"observation recorded from ehr\" or \"problem list from ehr\";",
            // era length 
            "with a length &gt; 400 days;",
            // nested criteria
            "having exactly 1 observation period",
            "starting  1 days after \"observation period\" end date"
    ));
  }

  @Test
  public void procedureOccurrenceTest() {
    CohortExpression expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/printfriendly/procedureOccurrence.json"));
    String markdown = pf.generate(expression);
    assertThat(markdown, stringContainsInOrder(
            // concept set name and first in history attribute
            "1. procedure occurrence of \"Concept Set 1\" (including \"Concept Set 2\" source concepts) for the first time in the person's history,",
            // age/gender criteria
            "who are female or male, &gt; 18 years old;",
            // start date/end date
            "starting on or Before January 1, 2014;",
            // procedure type 
            "a procedure type that is: \"admission note\" or \"ancillary report\";",
            // modifier 
            "with modifier: \"lateral meniscus structure\" or \"structure of base of lung\";",
            //quantity
            "with quantity &lt; 10;",
            // provider specialty
            "a provider specialty that is: \"gastroenterology\" or \"urology\";",
            // visit
            "a visit occurrence that is: \"emergency room visit\" or \"inpatient visit\";",
            // nested criteria
            "having at least 1 procedure occurrence of \"Concept Set 3\"",
            "starting anytime prior to \"Concept Set 1\" start date"
    ));
  }

  @Test
  public void specimenTest() {
    CohortExpression expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/printfriendly/specimen.json"));
    String markdown = pf.generate(expression);
    assertThat(markdown, stringContainsInOrder(
            // concept set name and first in history attribute
            "1. specimen of \"Concept Set 1\" for the first time in the person's history,",
            // age/gender criteria
            "who are female or male, &gt; 18 years old;",
            // start date/end date
            "starting before January 1, 2010;",
            // specimen type 
            "a specimen type that is: \"admission note\" or \"ancillary report\";",
            //quantity
            "with quantity &lt; 10;",
            // unit
            "with unit: \"per 24 hours\";",
            // anatomic site
            "with anatomic site: \"lateral meniscus structure\" or \"structure of base of lung\"",
            // disease status
            "with disease status: \"abnormal\";",
            // sourceID
            "with source ID starting with \"source Id Prefix\";",
            // nested criteria
            "having at least 1 specimen of \"Concept Set 2\"",
            "starting anytime prior to \"Concept Set 1\" start date"
    ));
  }

  @Test
  public void visitTest() {
    CohortExpression expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/printfriendly/visit.json"));
    String markdown = pf.generate(expression);
    assertThat(markdown, stringContainsInOrder(
            // concept set name and first in history attribute
            "1. visit occurrence of \"Concept Set 1\" (including \"Concept Set 2\" source concepts) for the first time in the person's history,",
            // age/gender criteria
            "who are female or male, between 18 and 64 years old;",
            // start date/end date
            "starting before January 1, 2010 and ending after January 7, 2010;",
            // visit type 
            "a visit type that is: \"admission note\" or \"ancillary report\";",
            //provider specialty
            "a provider specialty that is: \"general practice\" or \"general surgery\";",
            // visit type
            "a visit type that is: \"admission note\" or \"ancillary report\";",
            // visit length
            "with length &gt; 12 days",
            // nested criteria
            "having at least 1 visit occurrence of \"Concept Set 2\"",
            "starting anytime on or before \"Concept Set 1\" start date"
    ));
  }

  @Test
  public void dateOffsetTest() {
    CohortExpression expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/printfriendly/dateOffset.json"));
    String markdown = pf.generate(expression);
    assertThat(markdown, stringContainsInOrder(
            "The cohort end date will be offset from index event's end date plus 7 days."
    ));
    
  }

  @Test
  public void customEraExitTest() {
    CohortExpression expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/printfriendly/customEraExit.json"));
    String markdown = pf.generate(expression);
    assertThat(markdown, stringContainsInOrder(
            "The cohort end date will be based on a continuous exposure to \"Concept Set 1\":",
            "allowing 14 days between exposures, adding 0 days after exposure ends, and forcing drug exposure days suply to: 7 days."
    ));
    
  }

}
