package org.ohdsi.circe.check.checkers;

import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.ohdsi.circe.check.Checker;
import org.ohdsi.circe.check.Warning;
import org.ohdsi.circe.cohortdefinition.CohortExpression;

public class UnusedConceptsCheckTest {


    private static final String EXPRESSION = "{\n" +
            "  \"cdmVersionRange\": \">=5.0.0\",\n" +
            "  \"PrimaryCriteria\": {\n" +
            "    \"CriteriaList\": [\n" +
            "      {\n" +
            "        \"ConditionOccurrence\": {\n" +
            "          \"ConditionTypeExclude\": false\n" +
            "        }\n" +
            "      }\n" +
            "    ],\n" +
            "    \"ObservationWindow\": {\n" +
            "      \"PriorDays\": 0,\n" +
            "      \"PostDays\": 0\n" +
            "    },\n" +
            "    \"PrimaryCriteriaLimit\": {\n" +
            "      \"Type\": \"First\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"AdditionalCriteria\": {\n" +
            "    \"Type\": \"ALL\",\n" +
            "    \"CriteriaList\": [\n" +
            "      {\n" +
            "        \"Criteria\": {\n" +
            "          \"ConditionOccurrence\": {\n" +
            "            \"ConditionTypeExclude\": false\n" +
            "          }\n" +
            "        },\n" +
            "        \"StartWindow\": {\n" +
            "          \"Start\": {\n" +
            "            \"Coeff\": -1\n" +
            "          },\n" +
            "          \"End\": {\n" +
            "            \"Coeff\": 1\n" +
            "          },\n" +
            "          \"UseIndexEnd\": false,\n" +
            "          \"UseEventEnd\": false\n" +
            "        },\n" +
            "        \"RestrictVisit\": false,\n" +
            "        \"IgnoreObservationPeriod\": false,\n" +
            "        \"Occurrence\": {\n" +
            "          \"Type\": 2,\n" +
            "          \"Count\": 1,\n" +
            "          \"IsDistinct\": false\n" +
            "        }\n" +
            "      }\n" +
            "    ],\n" +
            "    \"DemographicCriteriaList\": [],\n" +
            "    \"Groups\": [\n" +
            "      {\n" +
            "        \"Type\": \"ALL\",\n" +
            "        \"CriteriaList\": [\n" +
            "          {\n" +
            "            \"Criteria\": {\n" +
            "              \"DrugEra\": {}\n" +
            "            },\n" +
            "            \"StartWindow\": {\n" +
            "              \"Start\": {\n" +
            "                \"Coeff\": -1\n" +
            "              },\n" +
            "              \"End\": {\n" +
            "                \"Coeff\": 1\n" +
            "              },\n" +
            "              \"UseIndexEnd\": false,\n" +
            "              \"UseEventEnd\": false\n" +
            "            },\n" +
            "            \"RestrictVisit\": false,\n" +
            "            \"IgnoreObservationPeriod\": false,\n" +
            "            \"Occurrence\": {\n" +
            "              \"Type\": 2,\n" +
            "              \"Count\": 1,\n" +
            "              \"IsDistinct\": false\n" +
            "            }\n" +
            "          },\n" +
            "          {\n" +
            "            \"Criteria\": {\n" +
            "              \"DrugEra\": {}\n" +
            "            },\n" +
            "            \"StartWindow\": {\n" +
            "              \"Start\": {\n" +
            "                \"Coeff\": -1\n" +
            "              },\n" +
            "              \"End\": {\n" +
            "                \"Coeff\": 1\n" +
            "              },\n" +
            "              \"UseIndexEnd\": false,\n" +
            "              \"UseEventEnd\": false\n" +
            "            },\n" +
            "            \"RestrictVisit\": false,\n" +
            "            \"IgnoreObservationPeriod\": false,\n" +
            "            \"Occurrence\": {\n" +
            "              \"Type\": 2,\n" +
            "              \"Count\": 1,\n" +
            "              \"IsDistinct\": false\n" +
            "            }\n" +
            "          }\n" +
            "        ],\n" +
            "        \"DemographicCriteriaList\": [],\n" +
            "        \"Groups\": [\n" +
            "          {\n" +
            "            \"Type\": \"ANY\",\n" +
            "            \"CriteriaList\": [\n" +
            "              {\n" +
            "                \"Criteria\": {\n" +
            "                  \"ConditionOccurrence\": {\n" +
            "                    \"CorrelatedCriteria\": {\n" +
            "                      \"Type\": \"ALL\",\n" +
            "                      \"CriteriaList\": [\n" +
            "                        {\n" +
            "                          \"Criteria\": {\n" +
            "                            \"DrugEra\": {\n" +
            "                              \"CodesetId\": 1\n" +
            "                            }\n" +
            "                          },\n" +
            "                          \"StartWindow\": {\n" +
            "                            \"Start\": {\n" +
            "                              \"Coeff\": -1\n" +
            "                            },\n" +
            "                            \"End\": {\n" +
            "                              \"Coeff\": 1\n" +
            "                            },\n" +
            "                            \"UseIndexEnd\": false,\n" +
            "                            \"UseEventEnd\": false\n" +
            "                          },\n" +
            "                          \"RestrictVisit\": false,\n" +
            "                          \"IgnoreObservationPeriod\": false,\n" +
            "                          \"Occurrence\": {\n" +
            "                            \"Type\": 2,\n" +
            "                            \"Count\": 1,\n" +
            "                            \"IsDistinct\": false\n" +
            "                          }\n" +
            "                        }\n" +
            "                      ],\n" +
            "                      \"DemographicCriteriaList\": [],\n" +
            "                      \"Groups\": []\n" +
            "                    },\n" +
            "                    \"CodesetId\": 0,\n" +
            "                    \"ConditionTypeExclude\": false\n" +
            "                  }\n" +
            "                },\n" +
            "                \"StartWindow\": {\n" +
            "                  \"Start\": {\n" +
            "                    \"Coeff\": -1\n" +
            "                  },\n" +
            "                  \"End\": {\n" +
            "                    \"Coeff\": 1\n" +
            "                  },\n" +
            "                  \"UseIndexEnd\": false,\n" +
            "                  \"UseEventEnd\": false\n" +
            "                },\n" +
            "                \"RestrictVisit\": false,\n" +
            "                \"IgnoreObservationPeriod\": false,\n" +
            "                \"Occurrence\": {\n" +
            "                  \"Type\": 2,\n" +
            "                  \"Count\": 1,\n" +
            "                  \"IsDistinct\": false\n" +
            "                }\n" +
            "              }\n" +
            "            ],\n" +
            "            \"DemographicCriteriaList\": [],\n" +
            "            \"Groups\": []\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  \"ConceptSets\": [\n" +
            "    {\n" +
            "      \"id\": 0,\n" +
            "      \"name\": \"Empty Concept Set\",\n" +
            "      \"expression\": {\n" +
            "        \"items\": []\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": 1,\n" +
            "      \"name\": \"Empty Concept Set 2\",\n" +
            "      \"expression\": {\n" +
            "        \"items\": []\n" +
            "      }\n" +
            "    }\n" +
            "  ],\n" +
            "  \"QualifiedLimit\": {\n" +
            "    \"Type\": \"First\"\n" +
            "  },\n" +
            "  \"ExpressionLimit\": {\n" +
            "    \"Type\": \"First\"\n" +
            "  },\n" +
            "  \"InclusionRules\": [],\n" +
            "  \"CensoringCriteria\": [],\n" +
            "  \"CollapseSettings\": {\n" +
            "    \"CollapseType\": \"ERA\",\n" +
            "    \"EraPad\": 0\n" +
            "  },\n" +
            "  \"CensorWindow\": {}\n" +
            "}";

    private ObjectMapper objectMapper = new ObjectMapper();

    private UnusedConceptsCheck unusedConceptsCheck = new UnusedConceptsCheck();


    @Test
    public void check_usageInTheChildGroups() throws IOException {
        CohortExpression cohortExpression = objectMapper.readValue(EXPRESSION, CohortExpression.class);
        List<Warning> warnings = unusedConceptsCheck.check(cohortExpression);
        assertEquals(Collections.emptyList(), warnings);
    }
}