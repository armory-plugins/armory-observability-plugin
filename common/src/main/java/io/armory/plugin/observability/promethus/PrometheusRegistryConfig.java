package io.armory.plugin.observability.promethus;

import io.armory.plugin.observability.model.PluginMetricsPrometheusConfig;
import io.micrometer.prometheus.PrometheusConfig;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Prometheus config wrapper that sources its config from the Spring Context Plugin Configuration.
 */
public class PrometheusRegistryConfig implements PrometheusConfig {

  private final PluginMetricsPrometheusConfig prometheusConfig;

  PrometheusRegistryConfig(PluginMetricsPrometheusConfig prometheusConfig) {
    this.prometheusConfig = prometheusConfig;
  }

  @Override
  public String get(String key) {
    return null; // NOOP, source config from the PluginConfig that is injected
  }

  @Override
  public boolean descriptions() {
    return prometheusConfig.isDescriptions();
  }

  @Override
  public Duration step() {
    return Duration.of(prometheusConfig.getStepInSeconds(), ChronoUnit.SECONDS);
  }
}
