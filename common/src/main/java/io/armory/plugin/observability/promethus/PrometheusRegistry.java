package io.armory.plugin.observability.promethus;

import io.armory.plugin.observability.model.PluginConfig;
import io.armory.plugin.observability.service.MeterFilterService;
import io.armory.plugin.observability.service.TagsService;
import io.micrometer.core.instrument.Clock;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrometheusRegistry extends PrometheusMeterRegistry {

  public PrometheusRegistry(
      PluginConfig pluginProperties,
      PrometheusCollectorRegistry collectorRegistry,
      TagsService tagsService,
      MeterFilterService meterFilterService,
      Clock clock) {

    // Create the Prometheus Registry
    this(new PrometheusRegistryConfig(pluginProperties), collectorRegistry, clock);

    log.info("Adding Default tags to Prometheus registry");
    // Add our default tags.
    this.config().commonTags(tagsService.getDefaultTags());

    // Add our meter filters
    meterFilterService
        .getMeterFilters()
        .forEach(meterFilter -> this.config().meterFilter(meterFilter));
  }

  public PrometheusRegistry(
      PrometheusConfig config, PrometheusCollectorRegistry collectorRegistry, Clock clock) {
    super(config, collectorRegistry, clock);
  }
}
