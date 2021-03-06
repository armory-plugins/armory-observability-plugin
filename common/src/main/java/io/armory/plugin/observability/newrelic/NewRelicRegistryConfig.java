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

import io.armory.plugin.observability.model.PluginMetricsNewRelicConfig;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.pf4j.PluginRuntimeException;

/**
 * New Relic config wrapper that sources its config from the Spring Context Plugin Configuration.
 */
public class NewRelicRegistryConfig implements io.micrometer.NewRelicRegistryConfig {

  private final PluginMetricsNewRelicConfig newRelicConfig;

  public NewRelicRegistryConfig(PluginMetricsNewRelicConfig newRelicConfig) {
    this.newRelicConfig = newRelicConfig;
  }

  @Override
  public String get(String key) {
    return null; // NOOP, source config from the PluginConfig that is injected
  }

  @Override
  public String apiKey() {
    return Optional.ofNullable(newRelicConfig.getApiKey())
        .orElseThrow(
            () ->
                new PluginRuntimeException(
                    "The New Relic API key is a required plugin config property"));
  }

  @Override
  public String uri() {
    return newRelicConfig.getUri();
  }

  @Override
  public String serviceName() {
    return null;
  }

  @Override
  public boolean enableAuditMode() {
    return newRelicConfig.isEnableAuditMode();
  }

  @Override
  public Duration step() {
    return Duration.of(newRelicConfig.getStepInSeconds(), ChronoUnit.SECONDS);
  }

  @Override
  public int numThreads() {
    return newRelicConfig.getNumThreads();
  }

  @Override
  public int batchSize() {
    return newRelicConfig.getBatchSize();
  }
}
