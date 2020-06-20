package io.armory.plugin.observability.newrelic;

import io.armory.plugin.observability.model.PluginConfig;
import io.armory.plugin.observability.model.PluginMetricsNewRelicConfig;
import io.armory.plugin.observability.service.MeterFilterService;
import io.armory.plugin.observability.service.TagsService;
import io.micrometer.newrelic.NewRelicRegistry;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class NewRelicRegistrySupplier implements Supplier<NewRelicRegistry> {

  private final PluginMetricsNewRelicConfig newRelicConfig;
  private final TagsService tagsService;
  private final MeterFilterService meterFilterService;

  public NewRelicRegistrySupplier(
      PluginConfig pluginConfig, TagsService tagsService, MeterFilterService meterFilterService) {

    newRelicConfig = pluginConfig.getMetrics().getNewrelic();
    this.tagsService = tagsService;
    this.meterFilterService = meterFilterService;
  }

  @Override
  public NewRelicRegistry get() {
    if (!newRelicConfig.isEnabled()) {
      return null;
    }

    var config = new NewRelicRegistryConfig(newRelicConfig);
    var registry = new NewRelicRegistry.NewRelicRegistryBuilder(config).build();

    registry.config().commonTags(tagsService.getDefaultTags());
    meterFilterService
        .getMeterFilters()
        .forEach(meterFilter -> registry.config().meterFilter(meterFilter));

    registry.gauge(
        "metrics.dpm",
        registry,
        reg -> reg.getMeters().size() * (60d / newRelicConfig.getStepInSeconds()));

    registry.start(Executors.defaultThreadFactory());
    return registry;
  }
}
