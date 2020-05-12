package org.ohdsi.circe.cohortdefinition.printfriendly;

import org.junit.Test;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.helper.ResourceHelper;

public class PrintFriendlyTest {

  @Test
  public void processExpression() {
    CohortExpression expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/printfriendly/allAttributes.json"));
    CohortPrintFriendly pf = new CohortPrintFriendly();
    String output = pf.generate(expression);
    System.out.println(output);
  }
}