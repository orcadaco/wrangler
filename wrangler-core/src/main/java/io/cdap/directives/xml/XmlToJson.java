/*
 *  Copyright © 2017-2019 Cask Data, Inc.
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

package io.cdap.directives.xml;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.directives.parser.JsParser;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Optional;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.annotations.Categories;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Numeric;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;
import org.json.JSONException;
import org.json.XML;

import java.util.List;

/**
 * A XML to Json Parser Stage.
 */
@Plugin(type = Directive.TYPE)
@Name("parse-xml-to-json")
@Categories(categories = { "xml"})
@Description("Parses a XML document to JSON representation.")
public class XmlToJson implements Directive {
  public static final String NAME = "parse-xml-to-json";
  // Column within the input row that needs to be parsed as Json
  private String col;
  private int depth;
  private final Gson gson = new Gson();

  @Override
  public UsageDefinition define() {
    UsageDefinition.Builder builder = UsageDefinition.builder(NAME);
    builder.define("column", TokenType.COLUMN_NAME);
    builder.define("depth", TokenType.NUMERIC, Optional.TRUE);
    return builder.build();
  }

  @Override
  public void initialize(Arguments args) throws DirectiveParseException {
    this.col = ((ColumnName) args.value("column")).value();
    if (args.contains("depth")) {
      this.depth = ((Numeric) args.value("depth")).value().intValue();
    } else {
      this.depth = Integer.MAX_VALUE;
    }
  }

  @Override
  public void destroy() {
    // no-op
  }

  @Override
  public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
    for (Row row : rows) {
      int idx = row.find(col);
      if (idx != -1) {
        Object object = row.getValue(idx);
        if (object == null) {
          throw new DirectiveExecutionException(toString() + " : Did not find '" + col + "' in the row.");
        }

        try {
          if (object instanceof String) {
            JsonObject element = gson.fromJson(XML.toJSONObject((String) object).toString(),
                                               JsonElement.class).getAsJsonObject();
            JsParser.jsonFlatten(element, col, 1, depth, row);
            row.remove(idx);
          } else {
            throw new DirectiveExecutionException(
              String.format("%s : Invalid type '%s' of column '%s'. Should be of type String.", toString(),
                            col, object != null ? object.getClass().getName() : "null")
            );
          }
        } catch (JSONException e) {
          throw new DirectiveExecutionException(toString() + " : " + e.getMessage());
        }
      }
    }
    return rows;
  }

}
