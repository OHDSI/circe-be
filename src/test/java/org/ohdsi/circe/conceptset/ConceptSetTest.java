package org.ohdsi.circe.conceptset;

import java.io.IOException;
import org.junit.Test;
import org.ohdsi.analysis.Utils;
import org.ohdsi.circe.cohortdefinition.ConceptSet;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.ohdsi.circe.BaseTest;

public class ConceptSetTest extends BaseTest {

  @Test
  public void testEqualsSameExpression() throws IOException {

    // Arrange
    String csExpression = readResource("/conceptset/dupixentExpression.json");
    ConceptSetExpression firstExpression = Utils.deserialize(csExpression, ConceptSetExpression.class);
    ConceptSetExpression secondExpression = Utils.deserialize(csExpression, ConceptSetExpression.class);

    ConceptSet firstCs = new ConceptSet();
    firstCs.expression = firstExpression;
    firstCs.id = 1;

    ConceptSet secondCs = new ConceptSet();
    secondCs.expression = secondExpression;
    secondCs.id = 1;

    // Assert
    assertTrue(firstCs.equals(secondCs));
    assertTrue(firstCs.equals(firstCs)); // compare same reference
    assertEquals(firstCs.hashCode(), secondCs.hashCode()); // check hashcode
    // expression checks
    assertTrue(firstExpression.equals(firstExpression)); // self check
    assertTrue(firstExpression.equals(secondExpression));
    assertTrue(firstExpression.items[0].equals(secondExpression.items[0])); // compare first concept set item
    assertTrue(firstExpression.items[0].equals(firstExpression.items[0])); // compare first concept set item same reference
    assertEquals(firstExpression.hashCode(), secondExpression.hashCode());

    // concept Check
    assertTrue(firstExpression.items[0].concept.equals(firstExpression.items[0].concept));

  }

  @Test
  public void testEqualsDifferentExpressions() throws IOException {

    // Arrange
    String dupixentExpression = readResource("/conceptset/dupixentExpression.json");
    ConceptSetExpression firstExpression = ConceptSetExpression.fromJson(dupixentExpression);

    String dupilumabExpression = readResource("/conceptset/dupilumabExpression.json");
    ConceptSetExpression secondExpression =  ConceptSetExpression.fromJson(dupilumabExpression);

    ConceptSet firstCs = new ConceptSet();
    firstCs.expression = firstExpression;
    firstCs.id = 1;

    ConceptSet secondCs = new ConceptSet();
    secondCs.expression = secondExpression;
    secondCs.id = 2;

    // Assert
    assertFalse(firstCs.equals(secondCs));
    assertFalse(firstCs.equals(null)); // check null
    // reset ids to match, but still should be non-equal
    secondCs.id = firstCs.id;
    assertFalse(firstCs.equals(secondCs));

    // expression checks
    assertFalse(firstExpression.equals(secondExpression));
    assertFalse(firstExpression.equals(new Object())); // check different object type
    assertFalse(firstExpression.equals(null)); // check null
    assertNotEquals(firstExpression.hashCode(), secondExpression.hashCode());

    // conceptSet Item checks
    assertFalse(firstExpression.items[0].equals(secondExpression.items[0])); // compare first concept set item
    assertFalse(firstExpression.items[0].equals(new Object())); // check different objects
    assertFalse(firstExpression.items[0].equals(null)); // check null 

    // concept Check
    assertFalse(firstExpression.items[0].concept.equals(secondExpression.items[0].concept));
    assertFalse(firstExpression.items[0].concept.equals(new Object()));
    assertFalse(firstExpression.items[0].concept.equals(null));

  }

  @Test
  public void testEqualsDifferentClasses() throws IOException {

    // Arrange
    String csExpression = readResource("/conceptset/dupixentExpression.json");
    ConceptSetExpression expression = Utils.deserialize(csExpression, ConceptSetExpression.class);

    ConceptSet conceptSet = new ConceptSet();
    conceptSet.expression = expression;
    conceptSet.id = 1;

    // Assert
    assertFalse(conceptSet.equals(expression));
  }

  @Test
  public void testEqualsWithNullObject() throws IOException {

    // Arrange
    String csExpression = readResource("/conceptset/dupixentExpression.json");
    ConceptSetExpression expression = Utils.deserialize(csExpression, ConceptSetExpression.class);

    ConceptSet conceptSet = new ConceptSet();
    conceptSet.expression = expression;
    conceptSet.id = 1;

    expression = null;

    // Assert
    assertFalse(conceptSet.equals(expression));
  }

}
