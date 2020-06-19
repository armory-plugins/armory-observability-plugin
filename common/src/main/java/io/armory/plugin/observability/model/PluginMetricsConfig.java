package io.armory.plugin.observability.model;

import java.util.Map;
import lombok.Data;

@Data
public class PluginMetricsConfig {
  private Map<String, String> additionalTags = Map.of();

  private PluginMetricsPrometheusConfig prometheus = new PluginMetricsPrometheusConfig();
  private PluginMetricsNewRelicConfig newrelic = new PluginMetricsNewRelicConfig();

  private boolean meterRegistryFiltersDisabled;
  private boolean defaultTagsDisabled;
}
