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

package co.cask.directives;

import co.cask.cdap.api.annotation.Description;
import co.cask.cdap.api.annotation.Name;
import co.cask.cdap.api.annotation.Plugin;
import co.cask.wrangler.api.Arguments;
import co.cask.wrangler.api.Directive;
import co.cask.wrangler.api.DirectiveExecutionException;
import co.cask.wrangler.api.DirectiveParseException;
import co.cask.wrangler.api.ErrorRowException;
import co.cask.wrangler.api.ExecutorContext;
import co.cask.wrangler.api.Row;
import co.cask.wrangler.api.parser.ColumnName;
import co.cask.wrangler.api.parser.Text;
import co.cask.wrangler.api.parser.TokenType;
import co.cask.wrangler.api.parser.UsageDefinition;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * XPath is a language for finding information in an XML file.
 * You can say that XPath is (sort of) SQL for XML files. XPath
 * is used to navigate through elements and attributes in an XML
 * document.
 *
 * XPath comes with powerful expressions that can be used to parse
 * an xml document and retrieve relevant information.
 */
@Plugin(type = Directive.Type)
@Name(XPathExtractor.DIRECTIVE_NAME)
@Description(XPathExtractor.DIRECTIVE_DESC)
public final class XPathExtractor implements Directive {
  public static final String DIRECTIVE_NAME = "extract-xpath";
  public static final String DIRECTIVE_DESC = "Extracts XPath from XML Document.";
  private String xpath;
  private String source;
  private String target;
  private XPath xPath;
  private XPathExpression xPathExpression;
  private DocumentBuilder builder;

  @Override
  public UsageDefinition define() {
    UsageDefinition.Builder builder = UsageDefinition.builder(DIRECTIVE_NAME);
    builder.define("xpath", TokenType.TEXT);
    builder.define("source", TokenType.COLUMN_NAME);
    builder.define("target", TokenType.COLUMN_NAME);
    return builder.build();
  }

  @Override
  public void initialize(Arguments arguments) throws DirectiveParseException {
    xPath = XPathFactory.newInstance().newXPath();
    xpath = ((Text) arguments.value("xpath")).value();
    try {
      xPathExpression = xPath.compile(xpath);
    } catch (XPathExpressionException e) {
      throw new DirectiveParseException(
        String.format("XPath '%s' is not valid xpath expression. %s", xpath, e.getMessage())
      );
    }
    source = ((ColumnName) arguments.value("source")).value();
    target = ((ColumnName) arguments.value("target")).value();
    DocumentBuilderFactory builderFactory =  DocumentBuilderFactory.newInstance();
    try {
      builder = builderFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new DirectiveParseException(
        String.format("Unable to create a XML document factory. %s", e.getMessage())
      );
    }
  }

  @Override
  public List<Row> execute(List<Row> rows, ExecutorContext context)
    throws DirectiveExecutionException, ErrorRowException {
    for (Row row : rows) {
      int idx = row.find(source);
      if (idx != -1) {
        Object object = row.getValue(idx);
        if (object instanceof Document) {
          Document value = (Document) object;
          try {
            row.addOrSet(target, xPathExpression.evaluate(value));
          } catch (XPathExpressionException e) {
            // Nothing to be done here.
          }
        } else if (object instanceof String) {
          String value = (String) object;
          try {
            Document xmlDocument = builder.parse(new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)));
            try {
              row.addOrSet(target, xPathExpression.evaluate(xmlDocument));
            } catch (XPathExpressionException e) {
              // Nothing to be done here.
            }
          } catch (SAXException | IOException e) {
            throw new DirectiveExecutionException(
              String.format("Unable to parse XML document.", e.getMessage())
            );
          }
        }
      }
    }
    return rows;
  }

  @Override
  public void destroy() {
    // nothing.
  }
}
