/*
 *  Copyright © 2017 Cask Data, Inc.
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
 * Tests implementation of {@link XmlParser}
 */
public class XmlParserTest {

  private static final String testXmlComplex = "<?xml version=\"1.0\"?>" +
    "<?xml-stylesheet href=\"catalog.xsl\" type=\"transformation/xsl\"?>\n" +
    "<!DOCTYPE catalog SYSTEM \"catalog.dtd\">\n" +
    "<catalog>\n" +
    "   <product description=\"Cardigan Sweater\" product_image=\"cardigan.jpg\">\n" +
    "      <catalog_item gender=\"Men's\">\n" +
    "         <item_number>QWZ5671</item_number>\n" +
    "         <price>39.95</price>\n" +
    "         <size description=\"Medium\">\n" +
    "            <color_swatch image=\"red_cardigan.jpg\">Red</color_swatch>\n" +
    "            <color_swatch image=\"burgundy_cardigan.jpg\">Burgundy</color_swatch>\n" +
    "         </size>\n" +
    "         <size description=\"Large\">\n" +
    "            <color_swatch image=\"red_cardigan.jpg\">Red</color_swatch>\n" +
    "            <color_swatch image=\"burgundy_cardigan.jpg\">Burgundy</color_swatch>\n" +
    "         </size>\n" +
    "      </catalog_item>\n" +
    "      <catalog_item gender=\"Women's\" test=\"10\">\n" +
    "         <item_number>RRX9856</item_number>\n" +
    "         <price>42.50</price>\n" +
    "         <size description=\"Small\">\n" +
    "            <color_swatch image=\"red_cardigan.jpg\">Red</color_swatch>\n" +
    "            <color_swatch image=\"navy_cardigan.jpg\">Navy</color_swatch>\n" +
    "            <color_swatch image=\"burgundy_cardigan.jpg\">Burgundy</color_swatch>\n" +
    "         </size>\n" +
    "         <size description=\"Medium\">\n" +
    "            <color_swatch image=\"red_cardigan.jpg\">Red</color_swatch>\n" +
    "            <color_swatch image=\"navy_cardigan.jpg\">Navy</color_swatch>\n" +
    "            <color_swatch image=\"burgundy_cardigan.jpg\">Burgundy</color_swatch>\n" +
    "            <color_swatch image=\"black_cardigan.jpg\">Black</color_swatch>\n" +
    "         </size>\n" +
    "         <size description=\"Large\">\n" +
    "            <color_swatch image=\"navy_cardigan.jpg\">Navy</color_swatch>\n" +
    "            <color_swatch image=\"black_cardigan.jpg\">Black</color_swatch>\n" +
    "         </size>\n" +
    "         <size description=\"Extra Large\">\n" +
    "            <color_swatch image=\"burgundy_cardigan.jpg\">Burgundy</color_swatch>\n" +
    "            <color_swatch image=\"black_cardigan.jpg\">Black</color_swatch>\n" +
    "         </size>\n" +
    "      </catalog_item>\n" +
    "   </product>\n" +
    "</catalog>";



  @Test
  public void testBasicXMLParser() throws Exception {
    TestRecipe recipe = new TestRecipe();
    recipe.add("parse-as-xml body");
    recipe.add("xpath body item /catalog/product/catalog_item/item_number");
    recipe.add("xpath-array body items /catalog/product/catalog_item/item_number");

    TestRows rows = new TestRows();
    rows.add(new Row("body", testXmlComplex));

    RecipePipeline pipeline = TestingRig.pipeline(XmlParser.class, recipe);
    List<Row> actuals = pipeline.execute(rows.toList());

    Assert.assertTrue(actuals.size() == 1);
    Assert.assertEquals(3, actuals.get(0).length());
    Assert.assertEquals("QWZ5671", actuals.get(0).getValue(1));
  }
}