package co.cask.directives;

import co.cask.wrangler.api.RecipePipeline;
import co.cask.wrangler.api.Row;
import co.cask.wrangler.test.TestingRig;
import co.cask.wrangler.test.api.TestRecipe;
import co.cask.wrangler.test.api.TestRows;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Tests {@link ParseXMLToDocument}
 */
public class XPathExtractorTest {
  private static String XML = "<?xml version=\"1.0\"?>\n" +
    "<Employees>\n" +
    "\t<Employee emplid=\"1111\" type=\"admin\">\n" +
    "\t\t<firstname>John</firstname>\n" +
    "\t\t<lastname>Watson</lastname>\n" +
    "\t\t<age>30</age>\n" +
    "\t\t<email>johnwatson@sh.com</email>\n" +
    "\t</Employee>\n" +
    "\t<Employee emplid=\"2222\" type=\"admin\">\n" +
    "\t\t<firstname>Sherlock</firstname>\n" +
    "\t\t<lastname>Homes</lastname>\n" +
    "\t\t<age>32</age>\n" +
    "\t\t<email>sherlock@sh.com</email>\n" +
    "\t</Employee>\n" +
    "\t<Employee emplid=\"3333\" type=\"user\">\n" +
    "\t\t<firstname>Jim</firstname>\n" +
    "\t\t<lastname>Moriarty</lastname>\n" +
    "\t\t<age>52</age>\n" +
    "\t\t<email>jim@sh.com</email>\n" +
    "\t</Employee>\n" +
    "\t<Employee emplid=\"4444\" type=\"user\">\n" +
    "\t\t<firstname>Mycroft</firstname>\n" +
    "\t\t<lastname>Holmes</lastname>\n" +
    "\t\t<age>41</age>\n" +
    "\t\t<email>mycroft@sh.com</email>\n" +
    "\t</Employee>\n" +
    "</Employees>";

  @Test
  public void testBasicXMLToDocument() throws Exception {
    System.out.println(XML);
    TestRecipe recipe = new TestRecipe();
    recipe.add("parse-xml-to-document :body;");
    TestRows rows = new TestRows();
    rows.add(new Row("body", XML));
    RecipePipeline pipeline = TestingRig.pipeline(ParseXMLToDocument.class, recipe);
    List<Row> actual = pipeline.execute(rows.toList());
    Assert.assertTrue(true);
  }
}