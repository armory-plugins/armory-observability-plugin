package io.armory.plugin.observability.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginMetricsPrometheusConfig {
  @Builder.Default private boolean enabled = false;
  @Builder.Default private int stepInSeconds = 30;
  @Builder.Default private boolean descriptions = false;
}
