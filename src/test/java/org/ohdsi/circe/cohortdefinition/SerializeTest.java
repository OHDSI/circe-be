package org.ohdsi.circe.cohortdefinition;

import org.junit.Test;
import org.ohdsi.circe.BaseTest;
import org.ohdsi.analysis.Utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SerializeTest extends BaseTest {

  /**
   * Check serialization
   *
   */
  @Test
  public void serializeConditionOccurrence() {

    String json = Utils.serialize(new ConditionOccurrence());

    assertEquals("{\"ConditionOccurrence\":{}}", json);

    ConditionOccurrence co = Utils.deserialize("{\"ConditionOccurrence\":{}}", ConditionOccurrence.class);
    assertTrue(true);
  }
}
