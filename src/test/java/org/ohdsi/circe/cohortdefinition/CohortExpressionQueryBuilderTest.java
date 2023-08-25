package org.ohdsi.circe.cohortdefinition;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ohdsi.circe.Utils;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;

@RunWith(MockitoJUnitRunner.class)
public class CohortExpressionQueryBuilderTest {

    private CohortExpressionQueryBuilder cohortExpressionQueryBuilder = new CohortExpressionQueryBuilder();

    @Test
    public void getCodesetQuery() {

        ConceptSet conceptSets[] = {
                createConceptSet(1, "name1"),
                createConceptSet(2, "name2")
        };

        String codesetQuery = Utils.normalizeLineEnds(cohortExpressionQueryBuilder.getCodesetQuery(conceptSets));
        assertThat(codesetQuery, containsString("CREATE TABLE #Codesets (\n" +
                "  codeset_id int NOT NULL,\n" +
                "  concept_id bigint NOT NULL\n" +
                ")\n;"));
        assertThat(codesetQuery, containsString("INSERT INTO"));
        assertThat(codesetQuery, containsString("SELECT 1"));
        assertThat(codesetQuery, containsString("UNION ALL"));
        assertThat(codesetQuery, containsString("SELECT 2"));
    }

    @Test
    public void getCodesetQueryEmptyConceptSets() {

        String codesetQuery = Utils.normalizeLineEnds(cohortExpressionQueryBuilder.getCodesetQuery(new ConceptSet[]{}));

        assertThat(codesetQuery, equalTo("CREATE TABLE #Codesets (\n" +
                "  codeset_id int NOT NULL,\n" +
                "  concept_id bigint NOT NULL\n" +
                ")\n;\n\n\n\nUPDATE STATISTICS #Codesets;\n"));
    }

    @Test
    public void getCodesetQueryNullConceptSets() {

        String codesetQuery = Utils.normalizeLineEnds(cohortExpressionQueryBuilder.getCodesetQuery(null));

        assertThat(codesetQuery, equalTo("CREATE TABLE #Codesets (\n" +
                "  codeset_id int NOT NULL,\n" +
                "  concept_id bigint NOT NULL\n" +
                ")\n;\n\n\n\nUPDATE STATISTICS #Codesets;\n"));
    }

    private ConceptSet createConceptSet(int id, String name) {

        ConceptSet conceptSet1 = new ConceptSet();
        conceptSet1.id = id;
        conceptSet1.name = name;
        conceptSet1.expression = new ConceptSetExpression();
        conceptSet1.expression.items = new ConceptSetExpression.ConceptSetItem[0];

        return conceptSet1;
    }

}