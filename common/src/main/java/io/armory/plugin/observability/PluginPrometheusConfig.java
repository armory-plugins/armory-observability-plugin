package io.armory.plugin.observability;

import lombok.Data;

@Data
public class PluginPrometheusConfig {
    int stepInSeconds = 30;
    boolean descriptions = false;
    int scrapePort = 8009;
    String path = "/prometheus";
}
