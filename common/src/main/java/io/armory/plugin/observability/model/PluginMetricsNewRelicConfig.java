package io.armory.plugin.observability.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginMetricsNewRelicConfig {
  // New Relic Specific Settings
  @Builder.Default private boolean enabled = false;
  @Builder.Default private String apiKey = null;
  @Builder.Default private String uri = "https://metric-api.newrelic.com/";
  @Builder.Default private boolean enableAuditMode = false;

  // Push Registry Settings
  @Builder.Default private int stepInSeconds = 30;
  @Builder.Default private int numThreads = 2;
  @Builder.Default private int batchSize = 10000;
}
