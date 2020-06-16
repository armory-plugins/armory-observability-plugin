package io.armory.plugin.observability.promethus;

import io.armory.plugin.observability.model.PluginConfig;
import io.armory.plugin.observability.model.PluginMetricsPrometheusConfig;
import io.micrometer.prometheus.PrometheusConfig;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class PrometheusRegistryConfig implements PrometheusConfig {

  private final PluginMetricsPrometheusConfig prometheusConfig;

  PrometheusRegistryConfig(PluginConfig pluginProperties) {
    prometheusConfig = pluginProperties.getMetrics().getPrometheus();
  }

  @Override
  public String get(String key) {
    return null;
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
