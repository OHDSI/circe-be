package org.ohdsi.circe.cohortdefinition.builders;

import static org.junit.Assert.*;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class BuilderUtilsTest {

    @Test
    public void wrapDateConstantForPartitionOrderByExpression_emptyArguments() {

        assertNull(BuilderUtils.wrapDateConstantForPartitionOrderByExpression(null, null));
        assertNull(BuilderUtils.wrapDateConstantForPartitionOrderByExpression("id", null));
        assertEquals(StringUtils.EMPTY, BuilderUtils.wrapDateConstantForPartitionOrderByExpression(null, StringUtils.EMPTY));
        assertEquals(StringUtils.EMPTY, BuilderUtils.wrapDateConstantForPartitionOrderByExpression("id", StringUtils.EMPTY));
    }

    @Test
    public void wrapDateConstantForPartitionOrderByExpression() {
        assertEquals("DATEADD(dd, id-id, constant)", BuilderUtils.wrapDateConstantForPartitionOrderByExpression("id", "constant"));
    }
}