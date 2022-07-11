package org.ohdsi.circe.cohortdefinition.printfriendly;

import com.fasterxml.jackson.core.type.TypeReference;
import java.io.StringWriter;
import java.io.Writer;

import org.ohdsi.circe.cohortdefinition.CohortExpression;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.TemplateModel;
import java.util.HashMap;
import java.util.Map;
import org.ohdsi.analysis.Utils;
import org.ohdsi.circe.cohortdefinition.ConceptSet;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;

public class MarkdownRender
{
  private static Configuration cfg;
  private static DefaultObjectWrapper objectWrapper;
  static {
    try {

      cfg = new Configuration(Configuration.VERSION_2_3_30);
      cfg.setClassForTemplateLoading(MarkdownRender.class, "/resources/cohortdefinition/printfriendly");
      cfg.setAPIBuiltinEnabled(true);
      cfg.setBooleanFormat("true, false");
      DefaultObjectWrapperBuilder builder = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_27);
      builder.setExposeFields(true);
      objectWrapper = builder.build();  
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
 
  }

  public String renderCohort(CohortExpression expression) {
    try {
      TemplateModel dataModel = objectWrapper.wrap(expression);
      Writer out = new StringWriter();
      cfg.getTemplate("cohortExpression.ftl").process(dataModel, out);
      return out.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public String renderCohort(String expressionJson) {
    CohortExpression expression = CohortExpression.fromJson(expressionJson);
    return this.renderCohort(expression);
  }

  public String renderConceptSetList(ConceptSet[] conceptSetList) {
    try {
      Map<String, Object> root = new HashMap<>();
      root.put("conceptSets", conceptSetList);
      TemplateModel dataModel = objectWrapper.wrap(root);
      Writer out = new StringWriter();
      cfg.getTemplate("conceptSet.ftl").process(dataModel, out);
      return out.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public String renderConceptSetList(String conceptSetListJson) {
    ConceptSet[] conceptSetList = Utils.deserialize(conceptSetListJson, new TypeReference<ConceptSet[]>() {});
    return renderConceptSetList(conceptSetList);
  }

  public String renderConceptSet(ConceptSet conceptSet) {
    ConceptSet[] conceptSetList =  new ConceptSet[] {conceptSet};// wrap param in array
    return renderConceptSetList(conceptSetList);
  }

  public String renderConceptSet(String conceptSetJson) {
    ConceptSet conceptSet = Utils.deserialize(conceptSetJson, new TypeReference<ConceptSet>() {});
    return renderConceptSet(conceptSet);
  }
}