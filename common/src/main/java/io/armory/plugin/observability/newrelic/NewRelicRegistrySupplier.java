package io.armory.plugin.observability.newrelic;

import io.armory.plugin.observability.model.PluginConfig;
import io.armory.plugin.observability.model.PluginMetricsNewRelicConfig;
import io.armory.plugin.observability.service.TagsService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.newrelic.NewRelicRegistry;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * A Supplier bean that can be registered with Spring without providing an actual registry
 * implementation to confuse Spectator/Micrometer. This Supplier configures a New Relic Micrometer
 * Registry Instance.
 */
public class NewRelicRegistrySupplier implements Supplier<MeterRegistry> {

  private final PluginMetricsNewRelicConfig newRelicConfig;
  private final TagsService tagsService;
  private static final double ONE_MINUTE_IN_SECONDS = 60d;

  public NewRelicRegistrySupplier(PluginConfig pluginConfig, TagsService tagsService) {

    newRelicConfig = pluginConfig.getMetrics().getNewrelic();
    this.tagsService = tagsService;
  }

  @Override
  public NewRelicRegistry get() {
    if (!newRelicConfig.isEnabled()) {
      return null;
    }

    var config = new NewRelicRegistryConfig(newRelicConfig);
    var registry = new NewRelicRegistry.NewRelicRegistryBuilder(config).build();

    registry.gauge(
        "metrics.dpm",
        tagsService.getDefaultTags(),
        registry,
        reg ->
            reg.getMeters().size() * (ONE_MINUTE_IN_SECONDS / newRelicConfig.getStepInSeconds()));

    registry.start(Executors.defaultThreadFactory());
    return registry;
  }
}
