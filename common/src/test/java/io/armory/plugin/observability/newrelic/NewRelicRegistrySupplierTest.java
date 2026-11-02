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

import io.armory.plugin.observability.model.PluginConfig;
import io.armory.plugin.observability.model.PluginMetricsConfig;
import io.armory.plugin.observability.model.PluginMetricsNewRelicConfig;
import io.armory.plugin.observability.service.TagsService;
import io.micrometer.core.ipc.http.HttpSender;
import io.micrometer.core.ipc.http.HttpUrlConnectionSender;
import com.newrelic.telemetry.micrometer.NewRelicRegistry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NewRelicRegistrySupplierTest {

  PluginMetricsNewRelicConfig config;

  @Mock TagsService tagsService;
  @Mock HttpUrlConnectionSender sender;

  NewRelicRegistrySupplier sut;

  @BeforeEach
  public void before() throws IOException {
    var pluginConfig = new PluginConfig();
    var metricsConfig = new PluginMetricsConfig();
    pluginConfig.setMetrics(metricsConfig);
    config = PluginMetricsNewRelicConfig.builder().apiKey("foo").build();
    metricsConfig.setNewrelic(config);
    sut = new NewRelicRegistrySupplier(pluginConfig, tagsService);
    lenient().when(sender.post(anyString())).thenCallRealMethod();
    lenient().when(sender.newRequest(anyString())).thenCallRealMethod();
    sut.sender = sender;
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

  @Test
  public void verifySendOfMetrics() throws Exception {
    config.setEnabled(true);
    config.setStepInSeconds(1);

    ArgumentCaptor<HttpSender.Request> captor = ArgumentCaptor.forClass(HttpSender.Request.class);
    when(sender.send(captor.capture())).thenReturn(new HttpSender.Response(202, "Success"));
    NewRelicRegistry registry = (NewRelicRegistry) sut.get().getMeterRegistry();
    registry.start(r -> new Thread(r));
    registry.counter("testCounter").increment();
    Thread.sleep(2*1000l);
    registry.stop();
    assertTrue(decompressByteData(captor.getValue().getEntity()).contains("testCounter"), "Received " + decompressByteData(captor.getValue().getEntity()));
  }

  private String decompressByteData(byte[] result) throws IOException {
    GZIPInputStream inputStream = new GZIPInputStream(new ByteArrayInputStream(result));
    String results = new String(inputStream.readAllBytes());
    inputStream.close();
    return results;
  }

}
