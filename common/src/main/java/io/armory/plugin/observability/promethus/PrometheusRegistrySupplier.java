package io.armory.plugin.observability.promethus;

import io.armory.plugin.observability.model.PluginConfig;
import io.armory.plugin.observability.model.PluginMetricsPrometheusConfig;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import java.util.function.Supplier;

/**
 * Supplier bean so that we don't create an actual bean of the prometheus registry. We do this so
 * that our composite registry is used and we don't confuse Spectator/Micrometer.
 */
public class PrometheusRegistrySupplier implements Supplier<MeterRegistry> {

  private final PluginMetricsPrometheusConfig prometheusConfig;
  private final CollectorRegistry collectorRegistry;
  private final Clock clock;

  public PrometheusRegistrySupplier(
      PluginConfig pluginConfig, CollectorRegistry collectorRegistry, Clock clock) {

    prometheusConfig = pluginConfig.getMetrics().getPrometheus();
    this.collectorRegistry = collectorRegistry;
    this.clock = clock;
  }

  @Override
  public PrometheusMeterRegistry get() {
    if (!prometheusConfig.isEnabled()) {
      return null;
    }
    var config = new PrometheusRegistryConfig(prometheusConfig);
    return new PrometheusMeterRegistry(config, collectorRegistry, clock);
  }
}
