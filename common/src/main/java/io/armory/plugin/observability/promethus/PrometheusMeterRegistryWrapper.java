package io.armory.plugin.observability.promethus;

import io.armory.plugin.observability.model.PluginConfig;
import io.micrometer.core.instrument.Clock;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

public class PrometheusMeterRegistryWrapper extends PrometheusMeterRegistry {

  public PrometheusMeterRegistryWrapper(
      PluginConfig pluginProperties,
      PrometheusCollectorRegistryWrapper collectorRegistry,
      Clock clock) {
    this(new PrometheusConfigWrapper(pluginProperties), collectorRegistry, clock);
  }

  public PrometheusMeterRegistryWrapper(
      PrometheusConfig config, PrometheusCollectorRegistryWrapper collectorRegistry, Clock clock) {
    super(config, collectorRegistry, clock);
  }
}
