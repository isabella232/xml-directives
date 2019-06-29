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
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * XPathExpression is a language for finding information in an XML file.
 * You can say that XPathExpression is (sort of) SQL for XML files. XPathExpression
 * is used to navigate through elements and attributes in an XML
 * document.
 *
 * XPathExpression comes with powerful expressions that can be used to parse
 * an xml document and retrieve relevant information.
 *
 * This directive <code>XMLParser</code> converts an XML
 * string into a XML Document.
 */
@Plugin(type = Directive.TYPE)
@Name(XMLParser.DIRECTIVE_NAME)
@Description(XMLParser.DIRECTIVE_DESC)
public final class XMLParser implements Directive {
  public static final String DIRECTIVE_NAME = "parse-as-xml";
  public static final String DIRECTIVE_DESC = "Parses string XML into a XML document";
  private ColumnName column;
  private DocumentBuilder builder;

  /**
   *
   * @return
   */
  @Override
  public UsageDefinition define() {
    UsageDefinition.Builder builder = UsageDefinition.builder(DIRECTIVE_NAME);
    builder.define("column", TokenType.COLUMN_NAME);
    return builder.build();
  }

  /**
   *
   * @param arguments
   * @throws DirectiveParseException
   */
  @Override
  public void initialize(Arguments arguments) throws DirectiveParseException {
    column = arguments.value("column");
    DocumentBuilderFactory documentFactory =  DocumentBuilderFactory.newInstance();
    documentFactory.setNamespaceAware(true);
    try {
      builder = documentFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new DirectiveParseException(
        String.format("Unable to create a XML document factory. %s", e.getMessage())
      );
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
      int idx = row.find(column.value());
      if (idx != -1) {
        Object object = row.getValue(idx);
        String value = null;
        if (object instanceof String) {
          value = (String) object;
        } else if (object instanceof byte[]) {
          value = new String((byte[]) object, StandardCharsets.UTF_8);
        } else {
          throw new DirectiveExecutionException(
            String.format("Unknown type to be converted to XML Document. Should be of type string or byte array.")
          );
        }
        if (value != null) {
          try {
            if (value.charAt(value.length() - 1) == '\u0000') {
              value = value.substring(0, value.length() - 1);
            }
            Document xmlDocument = builder.parse(
              new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8))
            );
            row.addOrSet(column.value(), xmlDocument);
          } catch (SAXException | IOException e) {
            throw new DirectiveExecutionException(
              String.format("Unable to parse XML. %s", e.getMessage())
            );
          }
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
