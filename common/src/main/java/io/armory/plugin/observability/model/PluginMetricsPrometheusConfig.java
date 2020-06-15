package io.armory.plugin.observability.model;

import lombok.Data;

@Data
public class PluginMetricsPrometheusConfig {
    private int stepInSeconds = 30;
    private boolean descriptions = false;
    private int scrapePort = 8009;
    private String path = "/prometheus";
}
