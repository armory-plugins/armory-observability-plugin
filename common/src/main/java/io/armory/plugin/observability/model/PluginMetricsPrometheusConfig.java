package io.armory.plugin.observability.model;

import lombok.Data;

@Data
public class PluginMetricsPrometheusConfig {
  private boolean enabled = false;
  private int stepInSeconds = 30;
  private boolean descriptions = false;
}
