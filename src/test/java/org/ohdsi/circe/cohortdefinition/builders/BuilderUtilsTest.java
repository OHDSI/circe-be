package org.ohdsi.circe.cohortdefinition.builders;


import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.apache.commons.lang.StringUtils;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.ohdsi.circe.cohortdefinition.TextFilter;

public class BuilderUtilsTest {

    @Test
    public void buildTextFilterClause_escapeQuotes() {

        TextFilter filter = createTextFilter("'", StringUtils.EMPTY);
        assertThat(
                BuilderUtils.buildTextFilterClause("", filter),
                Matchers.is(equalTo(("''''")))
        );
    }

    /**
     * Simply doubling quotes doesn’t work
     * ’ translates to  \’’
     * it can be used by malefactor.
     */
    @Test
    public void buildTextFilterClause_escapeAlreadyEscapedQuotes() {

        TextFilter filter = createTextFilter("\'", StringUtils.EMPTY);
        assertThat(
                BuilderUtils.buildTextFilterClause("", filter),
                Matchers.is(equalTo(("''''")))
        );
    }

    private TextFilter createTextFilter(String text, String op) {

        TextFilter filter = new TextFilter();
        filter.text = text;
        filter.op = op;
        return filter;
    }
}