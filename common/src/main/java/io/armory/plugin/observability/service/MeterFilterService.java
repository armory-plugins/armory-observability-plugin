package io.armory.plugin.observability.service;

import static io.micrometer.core.instrument.config.MeterFilter.deny;

import io.armory.plugin.observability.model.PluginConfig;
import io.armory.plugin.observability.model.PluginMetricsConfig;
import io.micrometer.core.instrument.config.MeterFilter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * https://micrometer.io/docs/concepts#_denyaccept_meters
 *
 * <p>A service that will provide our filters and transformers to configure / customize metrics to
 * be more efficient for metrics platforms that care about the number of unique MTS and or DPM.
 */
@Slf4j
public class MeterFilterService {

  /**
   * This may be too primitive but, it can be a place to start.
   *
   * <p>Denies metrics that contain the word "percentile" in their name.
   */
  protected static final MeterFilter BLOCK_METRICS_THAT_HAVE_PERCENTILE_IN_NAME =
      deny(id -> id.getName().toLowerCase().contains("percentile"));

  private final PluginMetricsConfig metricsConfig;

  public MeterFilterService(PluginConfig pluginConfig) {
    metricsConfig = pluginConfig.getMetrics();
  }

  public List<MeterFilter> getMeterFilters() {
    if (metricsConfig.isMeterRegistryFiltersDisabled()) {
      return List.of();
    }
    return List.of(BLOCK_METRICS_THAT_HAVE_PERCENTILE_IN_NAME);
  }
}
