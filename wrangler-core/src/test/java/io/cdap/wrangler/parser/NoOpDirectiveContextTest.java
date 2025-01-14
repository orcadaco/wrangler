/*
 * Copyright © 2017-2019 Cask Data, Inc.
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

package io.cdap.wrangler.parser;

import io.cdap.wrangler.TestingRig;
import io.cdap.wrangler.api.Executor;
import io.cdap.wrangler.api.RecipeParser;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Tests {@link NoOpDirectiveContext}
 */
public class NoOpDirectiveContextTest {

  @Test
  public void testNoFilteringHappening() throws Exception {
    String[] recipe = new String[] {
      "parse-as-csv body , true",
      "drop body",
      "drop Cabin",
      "drop Embarked",
      "fill-null-or-empty Age 0",
      "filter-row-if-true Fare < 8.06"
    };

    RecipeParser parse = TestingRig.parse(recipe);
    List<Executor> directives = parse.parse();
    Assert.assertEquals(6, directives.size());
  }

}
