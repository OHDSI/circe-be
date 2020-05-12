package org.ohdsi.circe.cohortdefinition.printfriendly;

import java.io.StringWriter;
import java.io.Writer;

import org.ohdsi.circe.cohortdefinition.CohortExpression;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.TemplateModel;

public class CohortPrintFriendly
{
  private static Configuration cfg;
  private static DefaultObjectWrapper objectWrapper;
  static {
    try {

      cfg = new Configuration(Configuration.VERSION_2_3_30);
      cfg.setClassForTemplateLoading(CohortPrintFriendly.class, "/resources/cohortDefinition/printfriendly");
      cfg.setAPIBuiltinEnabled(true);
      cfg.setBooleanFormat("true, false");
      cfg.setSharedVariable("CohortExpression", CohortExpression.class);
      DefaultObjectWrapperBuilder builder = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_27);
//      builder.setExposeFields(true);
      objectWrapper = builder.build();  
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
 
  }

  public String generate(CohortExpression expression) {
    try {
      TemplateModel dataModel = objectWrapper.wrap(expression);
      Writer out = new StringWriter();
      cfg.getTemplate("cohortExpression.ftl").process(dataModel, out);
      return out.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}