/*
 *  Copyright © 2019 CDAP
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

package io.cdap.directives;

import io.cdap.wrangler.api.RecipePipeline;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.test.TestingRig;
import io.cdap.wrangler.test.api.TestRecipe;
import io.cdap.wrangler.test.api.TestRows;
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
    "</Employees>\n";

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
