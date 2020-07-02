package org.ohdsi.circe.conceptset;

import java.io.IOException;
import org.junit.Test;
import org.ohdsi.analysis.Utils;
import org.ohdsi.circe.BaseTest;
import org.ohdsi.circe.cohortdefinition.ConceptSet;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConceptSetTest extends BaseTest {

    @Test
    public void testEqualsSameExpression() throws IOException {

        //Arrange
        String csExpression = readResource("/conceptset/dupixentExpression.json");
        ConceptSetExpression firstExpression = Utils.deserialize(csExpression, ConceptSetExpression.class);
        ConceptSetExpression secondExpression = Utils.deserialize(csExpression, ConceptSetExpression.class);

        ConceptSet firstCs = new ConceptSet();
        firstCs.expression = firstExpression;
        firstCs.id = 1;

        ConceptSet secondCs = new ConceptSet();
        secondCs.expression = secondExpression;
        secondCs.id = 1;

        //Assert
        assertTrue(firstCs.equals(secondCs));
    }
    
    @Test
    public void testEqualsDifferentExpressions() throws IOException {

        //Arrange
        String dupixentExpression = readResource("/conceptset/dupixentExpression.json");
        ConceptSetExpression firstExpression = Utils.deserialize(dupixentExpression, ConceptSetExpression.class);

        String dupilumabExpression = readResource("/conceptset/dupilumabExpression.json");
        ConceptSetExpression secondExpression = Utils.deserialize(dupilumabExpression, ConceptSetExpression.class);

        ConceptSet firstCs = new ConceptSet();
        firstCs.expression = firstExpression;
        firstCs.id = 1;

        ConceptSet secondCs = new ConceptSet();
        secondCs.expression = secondExpression;
        secondCs.id = 1;

        //Assert
        assertFalse(firstCs.equals(secondCs));
    }
    
    @Test
    public void testEqualsDifferentClasses() throws IOException {

        //Arrange
        String csExpression = readResource("/conceptset/dupixentExpression.json");
        ConceptSetExpression expression = Utils.deserialize(csExpression, ConceptSetExpression.class);

        ConceptSet conceptSet = new ConceptSet();
        conceptSet.expression = expression;
        conceptSet.id = 1;
        
        //Assert
        assertFalse(conceptSet.equals(expression));       
    }

    @Test
    public void testEqualsWithNullObject() throws IOException {

        //Arrange
        String csExpression = readResource("/conceptset/dupixentExpression.json");
        ConceptSetExpression expression = Utils.deserialize(csExpression, ConceptSetExpression.class);

        ConceptSet conceptSet = new ConceptSet();
        conceptSet.expression = expression;
        conceptSet.id = 1;
        
        expression = null;

        //Assert
        assertFalse(conceptSet.equals(expression));
    }

}
