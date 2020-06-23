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

package io.armory.plugin.observability.newrelic;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.MockitoAnnotations.initMocks;

import io.armory.plugin.observability.model.PluginConfig;
import io.armory.plugin.observability.model.PluginMetricsConfig;
import io.armory.plugin.observability.model.PluginMetricsNewRelicConfig;
import io.armory.plugin.observability.service.TagsService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class NewRelicRegistrySupplierTest {

  PluginMetricsNewRelicConfig config;

  @Mock TagsService tagsService;

  NewRelicRegistrySupplier sut;

  @Before
  public void before() {
    initMocks(this);
    var pluginConfig = new PluginConfig();
    var metricsConfig = new PluginMetricsConfig();
    pluginConfig.setMetrics(metricsConfig);
    config = PluginMetricsNewRelicConfig.builder().apiKey("foo").build();
    metricsConfig.setNewrelic(config);
    sut = new NewRelicRegistrySupplier(pluginConfig, tagsService);
  }

  @Test
  public void test_that_get_returns_null_if_newrelic_is_not_enabled() {
    config.setEnabled(false);
    assertNull(sut.get());
  }

  @Test
  public void test_that_get_returns_a_newrelic_registry_if_newrelic_is_enabled() {
    config.setEnabled(true);
    var actual = sut.get();
    assertNotNull(actual);
  }
}
