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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import io.cdap.wrangler.api.parser.ColumnNameList;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.util.List;

/**
 * XPathValue extractor extracts the value field from JSON object created by
 * XPathExpression.
 */
@Plugin(type = Directive.TYPE)
@Name(XMLNodeValue.DIRECTIVE_NAME)
@Description(XMLNodeValue.DIRECTIVE_DESC)
public final class XMLNodeValue implements Directive {
  public static final String DIRECTIVE_NAME = "xml-node-value";
  public static final String DIRECTIVE_DESC = "Extracts node value from XML generated json";
  private ColumnNameList columns;

  /**
   *
   * @return
   */
  @Override
  public UsageDefinition define() {
    UsageDefinition.Builder builder = UsageDefinition.builder(DIRECTIVE_NAME);
    builder.define("columns", TokenType.COLUMN_NAME_LIST);
    return builder.build();
  }

  /**
   *
   * @param arguments
   * @throws DirectiveParseException
   */
  @Override
  public void initialize(Arguments arguments) throws DirectiveParseException {
    columns = arguments.value("columns");
  }

  /**
   *
   * @param column
   * @param rows
   */
  private void value(String column, List<Row> rows) {
    for (Row row : rows) {
      int idx = row.find(column);
      if (idx == -1) {
        return;
      }
      Object object = row.getValue(idx);
      if (object instanceof JsonObject) {
        JsonElement value = ((JsonObject) object).get("value");
        if (value != null) {
          row.addOrSet(column, value.getAsString());
        }
      }
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
    for (String column : columns.value()) {
      value(column, rows);
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
