package io.armory.plugin.observability.newrelic;

import io.armory.plugin.observability.model.PluginMetricsNewRelicConfig;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.pf4j.PluginRuntimeException;

public class NewRelicRegistryConfig implements io.micrometer.NewRelicRegistryConfig {

  private final PluginMetricsNewRelicConfig newRelicConfig;

  public NewRelicRegistryConfig(PluginMetricsNewRelicConfig newRelicConfig) {
    this.newRelicConfig = newRelicConfig;
  }

  @Override
  public String get(String key) {
    return null;
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
