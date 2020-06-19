package io.armory.plugin.observability.promethus;

import io.armory.plugin.observability.model.PluginConfig;
import io.armory.plugin.observability.model.PluginMetricsPrometheusConfig;
import io.armory.plugin.observability.service.MeterFilterService;
import io.armory.plugin.observability.service.TagsService;
import io.micrometer.core.instrument.Clock;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import java.util.function.Supplier;

/**
 * Supplier bean so that we don't create an actual bean of the prometheus registry. We do this so
 * that our composite registry is used.
 */
public class PrometheusRegistrySupplier implements Supplier<PrometheusMeterRegistry> {

  private final PluginMetricsPrometheusConfig prometheusConfig;
  private final PrometheusCollectorRegistry collectorRegistry;
  private final TagsService tagsService;
  private final MeterFilterService meterFilterService;
  private final Clock clock;

  public PrometheusRegistrySupplier(
      PluginConfig pluginConfig,
      PrometheusCollectorRegistry collectorRegistry,
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
