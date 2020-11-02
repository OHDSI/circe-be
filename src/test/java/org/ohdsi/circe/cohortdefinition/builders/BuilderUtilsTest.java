package org.ohdsi.circe.cohortdefinition.builders;


import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.ohdsi.circe.cohortdefinition.TextFilter;

public class BuilderUtilsTest {

    @Test
    public void buildTextFilterClause_escapeQuotes() {

        String sqlInjection = "%';drop table tb; ";
        TextFilter filter = createTextFilter(sqlInjection, StringUtils.EMPTY);
        assertThat(
                BuilderUtils.buildTextFilterClause("field", filter),
                Matchers.is(equalTo(("field  like '%'';drop table tb; '")))
        );
    }

    /**
     * Simply doubling quotes doesn’t work
     * ’ translates to  \’’
     * it can be used by malefactor.
     */
    @Test
    public void buildTextFilterClause_escapeAlreadyEscapedQuotes() {

        String sqlInjection = "%\\';drop table tb; ";
        TextFilter filter = createTextFilter(sqlInjection, StringUtils.EMPTY);
        assertThat(
                BuilderUtils.buildTextFilterClause("field", filter),
                Matchers.is(equalTo(("field  like '%'';drop table tb; '")))
        );

        sqlInjection = "%\\\\';drop table tb; ";
        filter = createTextFilter(sqlInjection, StringUtils.EMPTY);
        assertThat(
                BuilderUtils.buildTextFilterClause("field", filter),
                Matchers.is(equalTo(("field  like '%'';drop table tb; '")))
        );
    }

    private TextFilter createTextFilter(String text, String op) {

        TextFilter filter = new TextFilter();
        filter.text = text;
        filter.op = op;
        return filter;
    }

}