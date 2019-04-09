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
 * XPath is a language for finding information in an XML file.
 * You can say that XPath is (sort of) SQL for XML files. XPath
 * is used to navigate through elements and attributes in an XML
 * document.
 *
 * XPath comes with powerful expressions that can be used to parse
 * an xml document and retrieve relevant information.
 *
 * This directive <code>ParseXMLToDocument</code> converts an XML
 * string into a XML Document.
 */
@Plugin(type = Directive.TYPE)
@Name(ParseXMLToDocument.DIRECTIVE_NAME)
@Description(ParseXMLToDocument.DIRECTIVE_DESC)
public final class ParseXMLToDocument implements Directive {
  public static final String DIRECTIVE_NAME = "parse-xml-to-document";
  public static final String DIRECTIVE_DESC = "Parses XML into XML Document.";
  private String column;
  private DocumentBuilder builder;

  @Override
  public UsageDefinition define() {
    UsageDefinition.Builder builder = UsageDefinition.builder(DIRECTIVE_NAME);
    builder.define("column", TokenType.COLUMN_NAME);
    return builder.build();
  }

  @Override
  public void initialize(Arguments arguments) throws DirectiveParseException {
    column = ((ColumnName) arguments.value("column")).value();
    DocumentBuilderFactory builderFactory =  DocumentBuilderFactory.newInstance();
    builderFactory.setNamespaceAware(true);
    builderFactory.setIgnoringComments(true);
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
      int idx = row.find(column);
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
            Document xmlDocument = builder.parse(new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)));
            row.addOrSet(column, xmlDocument);
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

  @Override
  public void destroy() {
    // nothing.
  }
}
