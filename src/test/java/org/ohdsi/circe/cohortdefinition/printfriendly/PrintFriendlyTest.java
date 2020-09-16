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
            "era length is &gt; 15",
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
}
