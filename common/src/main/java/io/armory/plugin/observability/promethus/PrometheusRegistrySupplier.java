package io.armory.plugin.observability.promethus;

import io.armory.plugin.observability.model.PluginConfig;
import io.armory.plugin.observability.model.PluginMetricsPrometheusConfig;
import io.armory.plugin.observability.registry.RegistrySupplier;
import io.armory.plugin.observability.service.MeterFilterService;
import io.armory.plugin.observability.service.TagsService;
import io.micrometer.core.instrument.Clock;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;

/**
 * Supplier bean so that we don't create an actual bean of the prometheus registry. We do this so
 * that our composite registry is used and we don't confuse Spectator/Micrometer.
 */
public class PrometheusRegistrySupplier implements RegistrySupplier {

  private final PluginMetricsPrometheusConfig prometheusConfig;
  private final CollectorRegistry collectorRegistry;
  private final TagsService tagsService;
  private final MeterFilterService meterFilterService;
  private final Clock clock;

  public PrometheusRegistrySupplier(
      PluginConfig pluginConfig,
      CollectorRegistry collectorRegistry,
      TagsService tagsService,
      MeterFilterService meterFilterService,
      Clock clock) {

    prometheusConfig = pluginConfig.getMetrics().getPrometheus();
    this.collectorRegistry = collectorRegistry;
    this.tagsService = tagsService;
    this.meterFilterService = meterFilterService;
    this.clock = clock;
  }

  @Override
  public PrometheusMeterRegistry get() {
    if (!prometheusConfig.isEnabled()) {
      return null;
    }
    var config = new PrometheusRegistryConfig(prometheusConfig);
    var registry = new PrometheusMeterRegistry(config, collectorRegistry, clock);

    registry.config().commonTags(tagsService.getDefaultTags());

    // Add our meter filters
    meterFilterService
        .getMeterFilters()
        .forEach(meterFilter -> registry.config().meterFilter(meterFilter));

    return registry;
  }
}
