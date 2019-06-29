/*
 * Copyright © 2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.directives;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ErrorRowException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import static io.cdap.directives.XPathExpression.DIRECTIVE_NAME;

/**
 * XPathExpression is a language for finding information in an XML file.
 * You can say that XPathExpression is (sort of) SQL for XML files. XPathExpression
 * is used to navigate through elements and attributes in an XML
 * document.
 *
 * XPathExpression comes with powerful expressions that can be used to parse
 * an xml document and retrieve relevant information.
 */
@Plugin(type = Directive.TYPE)
@Name(DIRECTIVE_NAME)
@Description("Select nodes from XML document using XPath expression")
public final class XPathExpression implements Directive {
  public static final String DIRECTIVE_NAME = "xpath-expression";
  private static final String XSL_TO_JSON_RESOURCE = "xml2json.xsl";
  private Text xpath;
  private ColumnName source;
  private ColumnName target;
  private XPath xPath;
  private Transformer transformer;

  /**
   *
   * @return
   */
  @Override
  public UsageDefinition define() {
    UsageDefinition.Builder builder = UsageDefinition.builder(DIRECTIVE_NAME);
    builder.define("source", TokenType.COLUMN_NAME);
    builder.define("xpath", TokenType.TEXT);
    builder.define("target", TokenType.COLUMN_NAME);
    return builder.build();
  }

  /**
   *
   * @return
   * @throws Exception
   */
  private String readXSLFromResource() throws Exception {
    String content = null;
    try (InputStream is = getClass().getClassLoader().getResource(XSL_TO_JSON_RESOURCE).openStream()) {
      StringBuilder builder = new StringBuilder();
      byte[] bytes = new byte[8192];
      int len = is.read(bytes);
      while (len >= 0) {
        builder.append(new String(bytes, 0, len));
        len = is.read(bytes);
      }
      content = builder.toString();
    } catch (Throwable e) {
      throw new DirectiveParseException("Internal error, XSL resource file missing. " + e.getMessage());
    }
    return content;
  }

  /**
   *
   * @param arguments
   * @throws DirectiveParseException
   */
  @Override
  public void initialize(Arguments arguments) throws DirectiveParseException {
    source = arguments.value("source");
    xpath = arguments.value("xpath");
    target = arguments.value("target");

    String content = null;
    try {
      content = readXSLFromResource();
      if (content != null) {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        StreamSource styleSource = new StreamSource(new StringReader(content));
        try {
          transformer = tFactory.newTransformer(styleSource);
        } catch (TransformerConfigurationException e) {
          throw new DirectiveParseException("Failed transforming XSL " + e.getMessage());
        }
      }
    } catch (Throwable e) {
      throw new DirectiveParseException("Internal error, there was problem reading XSL template. " + e.getMessage());
    }
  }

  /**
   *
   * @param rows
   * @param context
   * @return
   * @throws DirectiveExecutionException
   * @throws ErrorRowException
   */
  @Override
  public List<Row> execute(List<Row> rows, ExecutorContext context)
    throws DirectiveExecutionException, ErrorRowException {
    for (Row row : rows) {
      int idx = row.find(source.value());
      if (idx != -1) {
        Object object = row.getValue(idx);
        if (object instanceof Document) {
          Document value = (Document) object;
          try {
            xPath = XPathFactory.newInstance().newXPath();
            xPath.setNamespaceContext(new XMLNamespaceResolver(value, true));
            NodeList results = (NodeList) xPath.evaluate(xpath.value(), value, XPathConstants.NODESET);
            if (results.getLength() < 2 && results.item(0) != null) {
              row.addOrSet(target.value(), results.item(0).getNextSibling().getNodeValue());
            } else {
              JsonArray array = new JsonArray();
              for (int i = 0; i < results.getLength(); ++i) {
                Node node = results.item(i);
                DOMSource source = new DOMSource(node);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Result result = new StreamResult(baos);
                transformer.transform(source, result);
                array.add(new JsonParser().parse(baos.toString()));
              }
              row.addOrSet(target.value(), array);
            }
          } catch (XPathExpressionException | TransformerConfigurationException e) {
            throw new DirectiveExecutionException(e.getMessage());
          } catch (TransformerException e) {
            throw new DirectiveExecutionException(e.getMessage());
          }
        } else {
          throw new DirectiveExecutionException(
            String.format("XML document is not parsed, please use 'parse-xml' directive.")
          );
        }
      }
    }
    return rows;
  }

  /**
   *
   */
  @Override
  public void destroy() {
    // nothing.
  }
}
