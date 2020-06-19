package io.armory.plugin.observability.model;

import lombok.Data;

@Data
public class PluginMetricsNewRelicConfig {
  // New Relic Specific Settings
  private boolean enabled = false;
  private String apiKey = null;
  private String uri = "https://metric-api.newrelic.com/";
  private boolean enableAuditMode = false;

  // Push Registry Settings
  private int stepInSeconds = 30;
  private int numThreads = 2;
  private int batchSize = 10000;
}
