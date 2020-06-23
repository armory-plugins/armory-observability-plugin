/*
 * Copyright 2020 Armory, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.armory.plugin.observability.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import io.armory.plugin.observability.model.PluginConfig;
import org.junit.Before;
import org.junit.Test;

public class MeterFilterServiceTest {

  MeterFilterService sut;

  @Before
  public void before() {
    sut = new MeterFilterService(new PluginConfig());
  }

  @Test
  public void test_that_getMeterFilters_returns_emtpy_list_because_it_is_not_implemented() {
    var filters = sut.getMeterFilters();
    assertNotNull(filters);
    assertEquals(0, filters.size());
  }
}
