package org.ohdsi.circe.cohortdefinition.printfriendly;

import java.io.File;
import java.io.FileWriter;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.junit.Ignore;
import org.junit.Test;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.helper.ResourceHelper;

public class PrintFriendlyTest {
	private final String OUTPUT_PATH = "C:\\Documents\\OHDSI\\Circe\\printFriendly\\";
	
	@Test
  @Ignore
	public void processExpression() {
		CohortExpression expression = CohortExpression.fromJson(ResourceHelper.GetResourceAsString("/printfriendly/allAttributes.json"));
		MarkdownRender pf = new MarkdownRender();
		String markdown = pf.generate(expression);
		System.out.println("Markdown:");
		System.out.println("=====================================");
		System.out.println(markdown);
		
		Parser parser = Parser.builder().build();
		Node document = parser.parse(markdown);
		HtmlRenderer renderer = HtmlRenderer.builder().build();
		String html = renderer.render(document);  // "<p>This is <em>Sparta</em></p>\n"		
		System.out.println("HTML:");
		System.out.println("=====================================");
		System.out.println(html);
		
		try {
			FileWriter mdWriter = new FileWriter(new File(OUTPUT_PATH + "sampleOutput.md"),false);
			mdWriter.write(markdown);
			mdWriter.close();

			FileWriter htmlWriter = new FileWriter(new File(OUTPUT_PATH + "sampleOutput.html"),false);
			htmlWriter.write(html);
			htmlWriter.close();
			
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
